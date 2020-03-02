package pack.web;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.dao.DuplicatedRequestException;
import pack.dto.AccountDto;
import pack.dto.PaymentDto;
import pack.dto.TransactionDto;
import pack.dto.TransferDto;
import pack.service.AccountService;
import pack.service.PaymentService;
import pack.web.param.*;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.*;
import static pack.web.Validator.validate;

public class RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private static final int UNPROCESSABLE_ENTITY = 422;

    private static final int LOCK_TIMEOUT = 100;

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final SharedData sharedData;

    public RequestHandler(AccountService accountService, PaymentService paymentService, SharedData sharedData) {
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.sharedData = sharedData;
    }

    public void handleCreateAccount(RoutingContext context) {
        try {
            JsonObject json = context.getBodyAsJson();
            AccountRq rq = json.mapTo(AccountRq.class);
            validate(rq);
            processAndRespond(this::createAccount, rq, context);
        } catch (Exception e) {
            writeErrorResponse(context, e);
        }
    }

    private JsonObject createAccount(Rq rq) {
        AccountRq data = (AccountRq) rq;
        AccountDto account = accountService.createAccount(data.getRequestId());
        return JsonObject.mapFrom(account);
    }

    public void handleGetAccount(RoutingContext context) {
        try {
            HttpServerRequest request = context.request();
            GetAccountRq rq = new GetAccountRq(request.getParam("id"));
            validate(rq);
            processAndRespond(this::getAccount, rq, context);
        } catch (Exception e) {
            writeErrorResponse(context, e);
        }
    }

    private JsonObject getAccount(Rq rq) {
        GetAccountRq data = (GetAccountRq) rq;
        AccountDto account = accountService.getAccount(data.getId());
        return JsonObject.mapFrom(account);
    }

    public void handlePayment(RoutingContext context) {
        try {
            JsonObject json = context.getBodyAsJson();
            PaymentRq rq = json.mapTo(PaymentRq.class);
            validate(rq);
            processAndRespond(this::createPayment, rq, context);
        } catch (Exception e) {
            writeErrorResponse(context, e);
        }
    }

    private JsonObject createPayment(Rq rq) {
        PaymentRq data = (PaymentRq) rq;
        PaymentDto account = paymentService.createPayment(data.getRequestId(), data.getToId(), data.getAmount());
        return JsonObject.mapFrom(account);
    }

    public void handleTransfer(RoutingContext context) {
        try {
            JsonObject json = context.getBodyAsJson();
            TransferRq rq = json.mapTo(TransferRq.class);
            validate(rq);
            processAndRespond(this::createTransfer, rq, context);
        } catch (Exception e) {
            writeErrorResponse(context, e);
        }
    }

    private JsonObject createTransfer(Rq rq) {
        TransferRq data = (TransferRq) rq;
        TransferDto transfer = paymentService.transfer(data.getRequestId(), data.getFromId(), data.getToId(), data.getAmount());
        return JsonObject.mapFrom(transfer);
    }

    public void handleGetTransaction(RoutingContext context) {
        try {
            HttpServerRequest request = context.request();
            GetTransactionRq rq = new GetTransactionRq(request.getParam("id"));
            validate(rq);
            processAndRespond(this::getTransaction, rq, context);
        } catch (Exception e) {
            writeErrorResponse(context, e);
        }
    }

    private JsonObject getTransaction(Rq rq) {
        GetTransactionRq data = (GetTransactionRq) rq;
        TransactionDto transaction = paymentService.getTransaction(data.getId());
        return JsonObject.mapFrom(transaction);
    }

    private void processAndRespond(Function<Rq, JsonObject> operation, Rq rq, RoutingContext context) {
        applyWithLocks(operation, () -> rq, context, rq.getLocks(), 0);
    }

    private void applyWithLocks(Function<Rq, JsonObject> operation, Supplier<Rq> params, RoutingContext context, List<String> locks, int lockId) {
        if (lockId >= locks.size()) {
            applyAndRespond(operation, params, context);
            return;
        }
        int nextLockId = lockId + 1;
        sharedData.getLockWithTimeout(locks.get(lockId), LOCK_TIMEOUT, lockRes -> {
            if (lockRes.succeeded()) {
                Lock result = lockRes.result();
                try {
                    applyWithLocks(operation, params, context, locks, nextLockId);
                } finally {
                    result.release();
                }
            } else {
                log.warn("Failed to get lock");
            }
        });
    }

    private void applyAndRespond(Function<Rq, JsonObject> operation, Supplier<Rq> params, RoutingContext context) {
        try {
            JsonObject result = operation.apply(params.get());
            writeResponse(context, result.put("result", Result.Ok), HTTP_OK);
        } catch (DuplicatedRequestException e) {
            log.error("Duplicated request error", e);
            JsonObject json = new JsonObject().put("result", Result.Rejected).put("error", e + (e.getCause() != null ? ", cause:" + e.getCause() : ""));
            writeResponse(context, json, HTTP_CONFLICT);
        } catch (Exception e) {
            log.error("Request error", e);
            JsonObject json = new JsonObject().put("result", Result.Error).put("error", e + (e.getCause() != null ? ", cause:" + e.getCause() : ""));
            writeResponse(context, json, HTTP_INTERNAL_ERROR);
        }
    }

    private void writeErrorResponse(RoutingContext context, Exception e) {
        log.error("Unprocessable error", e);
        JsonObject json = new JsonObject().put("result", Result.Error).put("error", e + (e.getCause() != null ? ", cause:" + e.getCause() : ""));
        writeResponse(context, json, UNPROCESSABLE_ENTITY);
    }

    private void writeResponse(RoutingContext context, JsonObject json, int statusCode) {
        context.response().setChunked(true)
                .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .setStatusCode(statusCode)
                .end(json.encode());
    }
}
