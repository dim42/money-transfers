package pack.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import pack.dao.Repository;
import pack.dao.RepositoryImpl;
import pack.service.AccountService;
import pack.service.PaymentService;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

public class AppController extends AbstractVerticle {

    private static final String HOST = "localhost";
    private static final int PORT = 8081;
    private static final String ROOT_PATH = "/api/";
    private static final String API_VERSION = "1.0";

    public static void main(String[] args) {
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setPreferNativeTransport(true);
        Vertx.vertx(vertxOptions).deployVerticle(new AppController());
    }

    @Override
    public void start() {
        Repository repository = new RepositoryImpl(vertx.sharedData());
        AccountService accountService = new AccountService(repository);
        PaymentService paymentService = new PaymentService(repository);
        RequestHandler handler = new RequestHandler(accountService, paymentService, vertx.sharedData());

        Router restAPI = Router.router(vertx);
        restAPI.route().handler(BodyHandler.create());
        restAPI.route().consumes(APPLICATION_JSON.toString());
        restAPI.route().produces(APPLICATION_JSON.toString());

        restAPI.post("/account").handler(handler::handleCreateAccount);
        restAPI.get("/account/:id").handler(handler::handleGetAccount);
        restAPI.post("/payment").handler(handler::handlePayment);
        restAPI.post("/transfer").handler(handler::handleTransfer);
        restAPI.get("/transaction/:id").handler(handler::handleGetTransaction);

        Router mainRouter = Router.router(vertx);
        mainRouter.mountSubRouter(ROOT_PATH + API_VERSION, restAPI);
        vertx.createHttpServer().requestHandler(mainRouter).listen(PORT, HOST);
    }
}
