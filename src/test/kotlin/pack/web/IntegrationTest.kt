package pack.web

import io.restassured.RestAssured
import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import java.lang.String.format
import java.time.LocalDate.now
import java.util.*

@RunWith(VertxUnitRunner::class)
class IntegrationTest {
    companion object {
        private val log = LoggerFactory.getLogger(IntegrationTest::class.java)
    }

    private var vertx: Vertx? = null

    @Before
    fun setUp(context: TestContext) {
        vertx = Vertx.vertx()
        val port = 8081
        val options = DeploymentOptions()
                .setConfig(JsonObject().put("http.port", port))
        vertx!!.deployVerticle(AppController::class.java.name, options, context.asyncAssertSuccess())
        RestAssured.port = port
    }

    @After
    fun tearDown(context: TestContext) {
        vertx!!.close(context.asyncAssertSuccess())
    }

    @Test
    fun testCreateAndGetAccount_andDuplicateRequestRejected() {
        val requestId = UUID.randomUUID()
        val response = given()
                .body("{\"request_id\":\"$requestId\"}").contentType(ContentType.JSON).accept(ContentType.JSON)
                .post("/api/1.0/account")
                .thenReturn()

        val fields = response.`as`<Map<String, Any>>(MutableMap::class.java)
        assertThat(response.statusCode(), equalTo(200))
        assertThat(fields["result"] as String, equalTo("Ok"))
        val accId = fields["id"] as String?
        assertNotNull(accId)

        get("/api/1.0/account/$accId")
                .then().assertThat().statusCode(200).body(equalTo("{\"id\":\"$accId\",\"balance\":0,\"result\":\"Ok\"}"))
        get("/api/1.0/account/" + "abc123")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Account (abc123) is not found"))

        given()
                .body("{\"request_id\":\"$requestId\"}")
                .post("/api/1.0/account")
                .then().assertThat().statusCode(409).body("result", equalTo("Rejected")).body("error", containsString("DuplicatedRequestException"))
    }

    @Test
    fun testPayment_forOneAccountAndDuplicateRequestRejected() {
        val accId = createAccount()
        val requestId = UUID.randomUUID()

        val response = given()
                .body("{\"request_id\":\"$requestId\", \"target_acc_id\":\"$accId\", \"amount\":75.62}")
                .post("/api/1.0/payment")
                .thenReturn()

        val fields = response.`as`<Map<String, Any>>(MutableMap::class.java)
        assertThat(response.statusCode(), equalTo(200))
        val transaction = fields["id"] as String?
        assertNotNull(transaction)
        assertThat(fields["result"] as String, equalTo("Ok"))
        assertNotNull(fields["date_time"])

        get("/api/1.0/account/$accId")
                .then().assertThat().statusCode(200).body(equalTo("{\"id\":\"$accId\",\"balance\":75.62,\"result\":\"Ok\"}"))
        get("/api/1.0/transaction/$transaction")
                .then().assertThat().statusCode(200)
                .body("id", equalTo(transaction))
                .body("source_acc_id", nullValue())
                .body("target_acc_id", equalTo(accId))
                .body("amount", equalTo(75.62f))
                .body("date_time", containsString(now().year.toString()))
        get("/api/1.0/transaction/" + "tx22")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Transaction (tx22) is not found"))

        given()
                .body("{\"request_id\":\"$requestId\", \"target_acc_id\":\"$accId\", \"amount\":75.62}")
                .post("/api/1.0/payment")
                .then().assertThat().statusCode(409).body("result", equalTo("Rejected")).body("error", containsString("DuplicatedRequestException"))
    }

    @Test
    fun testTransfer_andDuplicateRequestRejected() {
        val fromAccId = createAccount()
        val toAccId = createAccount()
        createPayment(fromAccId, "100")
        createPayment(toAccId, "50")
        val requestId = UUID.randomUUID()

        val response = given()
                .body("{\"request_id\":\"$requestId\", \"source_acc_id\":\"$fromAccId\", \"target_acc_id\":\"$toAccId\", \"amount\":21.42}")
                .post("/api/1.0/transfer")
                .thenReturn()

        val fields = response.`as`<Map<String, Any>>(MutableMap::class.java)
        assertThat(response.statusCode(), equalTo(200))
        val transaction = fields["id"] as String?
        assertNotNull(transaction)
        assertThat(fields["result"] as String, equalTo("Ok"))
        assertNotNull(fields["date_time"])

        get("/api/1.0/transaction/$transaction")
                .then().assertThat().statusCode(200)
                .body("id", equalTo(transaction))
                .body("source_acc_id", equalTo(fromAccId))
                .body("target_acc_id", equalTo(toAccId))
                .body("amount", equalTo(21.42f))
                .body("date_time", containsString(now().year.toString()))
        get("/api/1.0/account/$fromAccId")
                .then().assertThat().body(equalTo("{\"id\":\"$fromAccId\",\"balance\":78.58,\"result\":\"Ok\"}"))
        get("/api/1.0/account/$toAccId")
                .then().assertThat().body(equalTo("{\"id\":\"$toAccId\",\"balance\":71.42,\"result\":\"Ok\"}"))

        given()
                .body("{\"request_id\":\"$requestId\", \"source_acc_id\":\"$fromAccId\", \"target_acc_id\":\"$toAccId\", \"amount\":21.42}")
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(409).body("result", equalTo("Rejected")).body("error", containsString("DuplicatedRequestException"))
    }

    @Test
    fun testTransfer_toTheSameAccount() {
        val fromAccId = createAccount()
        createPayment(fromAccId, "100")

        given()
                .body("{\"request_id\":\"${UUID.randomUUID()}\", \"source_acc_id\":\"$fromAccId\", \"target_acc_id\":\"$fromAccId\", \"amount\":21.42}")
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("Source and target accounts should be different"))
    }

    @Test
    fun testTransfer_insufficientBalance() {
        val fromAccId = createAccount()
        val toAccId = createAccount()
        createPayment(fromAccId, "100")
        createPayment(toAccId, "50")

        given()
                .body("{\"request_id\":\"${UUID.randomUUID()}\", \"source_acc_id\":\"$fromAccId\", \"target_acc_id\":\"$toAccId\", \"amount\":101}")
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Insufficient balance"))
    }

    @Test
    fun testTransfer_noSourceAccount() {
        val toAccId = createAccount()

        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":101}", UUID.randomUUID(), "abc123", toAccId))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Account (abc123) is not found"))
    }

    @Test
    fun testTransfer_noSourceParam() {
        given()
                .body(format("{\"request_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":101}", UUID.randomUUID(), "abc123"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("source_acc_id param should be filled"))
    }

    @Test
    fun testTransfer_noTargetAccount() {
        val fromAccId = createAccount()
        createPayment(fromAccId, "100")

        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":21.42}", UUID.randomUUID(), fromAccId, "abc124"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Account (abc124) is not found"))
    }

    @Test
    fun testTransfer_noTargetParam() {
        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"amount\":101}", UUID.randomUUID(), "abc123"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("target_acc_id param should be filled"))
    }

    @Test
    fun testTransfer_negativeAmount() {
        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":-34.56}", UUID.randomUUID(), "abc123", "abc124"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("Amount should be positive"))
    }

    @Test
    fun testTransfer_noAmountParam() {
        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\"}", UUID.randomUUID(), "abc123", "abc124"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("amount param should be filled"))
    }

    @Test
    fun testTransfer_noRequestId() {
        given()
                .body(format("{ \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":21.42}", "abc123", "abc124"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("request_id param should be filled"))
    }

    private fun createAccount(): String? {
        val response = given()
                .body("{\"request_id\":\"${UUID.randomUUID()}\"}")
                .post("/api/1.0/account")
                .thenReturn()

        assertThat(response.statusCode(), equalTo(200))
        val fields = response.`as`<Map<String, Any>>(MutableMap::class.java)
        val accId = fields["id"] as String?
        log.debug("accId: {}", accId)
        return accId
    }

    private fun createPayment(fromAccId: String?, amount: String) {
        given()
                .body("{\"request_id\":\"${UUID.randomUUID()}\", \"target_acc_id\":\"$fromAccId\", \"amount\":$amount}")
                .post("/api/1.0/payment")
                .then().assertThat().statusCode(200)
    }
}
