package pack

import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import pack.web.AppController

object Runner {
    private val log = LoggerFactory.getLogger(Runner::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        log.info("Starting")
        Vertx.vertx().deployVerticle(AppController())
    }
}
