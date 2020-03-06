package pack.web

import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.vertx.core.AsyncResult
import io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.Lock
import io.vertx.core.shareddata.SharedData
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import pack.dao.DuplicatedRequestException
import pack.service.AccountService
import pack.service.PaymentService
import pack.web.Result.*
import pack.web.param.*
import java.net.HttpURLConnection.*
import java.util.function.Function
import java.util.function.Supplier

class RequestHandler(private val accountService: AccountService, private val paymentService: PaymentService, private val sharedData: SharedData) {

    companion object {
        private val log = LoggerFactory.getLogger(RequestHandler::class.java)
        private const val UNPROCESSABLE_ENTITY = 422
        private const val LOCK_TIMEOUT = 100
    }

    fun handleCreateAccount(context: RoutingContext) {
        try {
            val json = context.bodyAsJson
            val rq = json.mapTo(AccountRq::class.java)
            processAndRespond(Function(this::createAccount), rq, context)
        } catch (e: Exception) {
            writeErrorResponse(context, e)
        }
    }

    private fun createAccount(rq: Rq): JsonObject {
        val data = rq as AccountRq
        val account = accountService.createAccount(data.requestId)
        return JsonObject.mapFrom(account)
    }

    fun handleGetAccount(context: RoutingContext) {
        try {
            val request = context.request()
            val rq = GetAccountRq(request.getParam("id"))
            processAndRespond(Function(this::getAccount), rq, context)
        } catch (e: Exception) {
            writeErrorResponse(context, e)
        }
    }

    private fun getAccount(rq: Rq): JsonObject {
        val data = rq as GetAccountRq
        val account = accountService.getAccount(data.id)
        return JsonObject.mapFrom(account)
    }

    fun handlePayment(context: RoutingContext) {
        try {
            val json = context.bodyAsJson
            val rq = json.mapTo(PaymentRq::class.java)
            processAndRespond(Function(this::createPayment), rq, context)
        } catch (e: Exception) {
            writeErrorResponse(context, e)
        }
    }

    private fun createPayment(rq: Rq): JsonObject {
        val data = rq as PaymentRq
        val account = paymentService.createPayment(data.requestId, data.toId, data.amount)
        return JsonObject.mapFrom(account)
    }

    fun handleTransfer(context: RoutingContext) {
        try {
            val json = context.bodyAsJson
            val rq = json.mapTo(TransferRq::class.java)
            processAndRespond(Function(this::createTransfer), rq, context)
        } catch (e: Exception) {
            writeErrorResponse(context, e)
        }
    }

    private fun createTransfer(rq: Rq): JsonObject {
        val data = rq as TransferRq
        val transfer = paymentService.transfer(data.requestId, data.fromId, data.toId, data.amount)
        return JsonObject.mapFrom(transfer)
    }

    fun handleGetTransaction(context: RoutingContext) {
        try {
            val request = context.request()
            val rq = GetTransactionRq(request.getParam("id"))
            processAndRespond(Function(this::getTransaction), rq, context)
        } catch (e: Exception) {
            writeErrorResponse(context, e)
        }
    }

    private fun getTransaction(rq: Rq): JsonObject {
        val data = rq as GetTransactionRq
        val transaction = paymentService.getTransaction(data.id)
        return JsonObject.mapFrom(transaction)
    }

    private fun processAndRespond(operation: Function<Rq, JsonObject>, rq: Rq, context: RoutingContext) {
        applyWithLocks(operation, Supplier { rq }, context, rq.locks, 0)
    }

    private fun applyWithLocks(operation: Function<Rq, JsonObject>, params: Supplier<Rq>, context: RoutingContext, locks: List<String>, lockId: Int) {
        if (lockId >= locks.size) {
            applyAndRespond(operation, params, context)
            return
        }
        sharedData.getLockWithTimeout(locks[lockId], LOCK_TIMEOUT.toLong()) { lockRes: AsyncResult<Lock> ->
            if (lockRes.succeeded()) {
                val result = lockRes.result()
                try {
                    applyWithLocks(operation, params, context, locks, lockId + 1)
                } finally {
                    result.release()
                }
            } else {
                log.warn("Failed to get lock")
            }
        }
    }

    private fun applyAndRespond(operation: Function<Rq, JsonObject>, params: Supplier<Rq>, context: RoutingContext) {
        try {
            val result = operation.apply(params.get())
            writeResponse(context, result.put("result", Ok), HTTP_OK)
        } catch (e: DuplicatedRequestException) {
            log.error("Duplicated request error", e)
            val json = JsonObject().put("result", Rejected).put("error", e.toString() + if (e.cause != null) ", cause:" + e.cause else "")
            writeResponse(context, json, HTTP_CONFLICT)
        } catch (e: Exception) {
            log.error("Request error", e)
            val json = JsonObject().put("result", Error).put("error", e.toString() + if (e.cause != null) ", cause:" + e.cause else "")
            writeResponse(context, json, HTTP_INTERNAL_ERROR)
        }
    }

    private fun writeErrorResponse(context: RoutingContext, e: Exception) {
        log.error("Unprocessable error", e)
        val json = JsonObject().put("result", Error).put("error", e.toString() + if (e.cause != null) ", cause:" + e.cause else "")
        writeResponse(context, json, UNPROCESSABLE_ENTITY)
    }

    private fun writeResponse(context: RoutingContext, json: JsonObject, statusCode: Int) {
        context.response().setChunked(true)
                .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .setStatusCode(statusCode)
                .end(json.encode())
    }
}
