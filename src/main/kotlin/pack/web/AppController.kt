package pack.web

import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import pack.dao.Repository
import pack.dao.RepositoryImpl
import pack.service.AccountService
import pack.service.PaymentService

class AppController : AbstractVerticle() {
    override fun start() {
        val repository: Repository = RepositoryImpl(vertx.sharedData())
        val accountService = AccountService(repository)
        val paymentService = PaymentService(repository)

        val handler = RequestHandler(accountService, paymentService, vertx.sharedData())
        val restAPI = Router.router(vertx)
        restAPI.route().handler(BodyHandler.create())
        restAPI.route().consumes(APPLICATION_JSON.toString())
        restAPI.route().produces(APPLICATION_JSON.toString())

        restAPI.post("/account").handler(handler::handleCreateAccount)
        restAPI["/account/:id"].handler(handler::handleGetAccount)
        restAPI.post("/payment").handler(handler::handlePayment)
        restAPI.post("/transfer").handler(handler::handleTransfer)
        restAPI["/transaction/:id"].handler(handler::handleGetTransaction)

        val mainRouter = Router.router(vertx)
        mainRouter.mountSubRouter(ROOT_PATH + API_VERSION, restAPI)
        vertx.createHttpServer().requestHandler(mainRouter).listen(PORT, HOST)
    }

    companion object {
        private const val HOST = "localhost"
        private const val PORT = 8081
        private const val ROOT_PATH = "/api/"
        private const val API_VERSION = "1.0"

        @JvmStatic
        fun main(args: Array<String>) {
            val vertxOptions = VertxOptions()
            vertxOptions.preferNativeTransport = true
            Vertx.vertx(vertxOptions).deployVerticle(AppController())
        }
    }
}
