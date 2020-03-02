package pack;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.web.AppController;

public class Runner {

    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {
        log.info("Starting");
        Vertx.vertx().deployVerticle(new AppController());
    }
}
