package pack.web;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.time.LocalDate.now;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class IntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTest.class);

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        int port = 8081;
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(AppController.class.getName(), options, context.asyncAssertSuccess());
        RestAssured.port = port;
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testCreateAndGetAccount_andDuplicateRequestRejected() {
        UUID requestId = UUID.randomUUID();

        Response response = given()
                .body(format("{\"request_id\":\"%s\"}", requestId)).contentType(JSON).accept(JSON)
                .post("/api/1.0/account")
                .thenReturn();
        Map<String, Object> fields = response.as(Map.class);
        assertThat(response.statusCode(), equalTo(200));
        assertThat(fields.get("result"), equalTo("Ok"));
        String accId = (String) fields.get("id");
        assertNotNull(accId);

        get("/api/1.0/account/" + accId)
                .then().assertThat().statusCode(200).body(equalTo(format("{\"id\":\"%s\",\"balance\":0,\"result\":\"Ok\"}", accId)));
        get("/api/1.0/account/" + "abc123")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Account (abc123) is not found"));

        given()
                .body(format("{\"request_id\":\"%s\"}", requestId))
                .post("/api/1.0/account")
                .then().assertThat().statusCode(409).body("result", equalTo("Rejected")).body("error", containsString("DuplicatedRequestException"));
    }

    @Test
    public void testPayment_forOneAccountAndDuplicateRequestRejected() {
        String accId = createAccount();

        UUID requestId = UUID.randomUUID();
        Response response = given()
                .body(format("{\"request_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":75.62}", requestId, accId))
                .post("/api/1.0/payment")
                .thenReturn();
        Map<String, Object> fields = response.as(Map.class);
        assertThat(response.statusCode(), equalTo(200));
        String transaction = (String) fields.get("id");
        assertNotNull(transaction);
        assertThat(fields.get("result"), equalTo("Ok"));
        assertNotNull(fields.get("date_time"));

        get("/api/1.0/account/" + accId)
                .then().assertThat().statusCode(200).body(equalTo(format("{\"id\":\"%s\",\"balance\":75.62,\"result\":\"Ok\"}", accId)));
        get("/api/1.0/transaction/" + transaction)
                .then().assertThat().statusCode(200)
                .body("id", equalTo(transaction))
                .body("source_acc_id", nullValue())
                .body("target_acc_id", equalTo(accId))
                .body("amount", equalTo(75.62f))
                .body("date_time", containsString(valueOf(now().getYear())));
        get("/api/1.0/transaction/" + "tx22")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Transaction (tx22) is not found"));

        given()
                .body(format("{\"request_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":75.62}", requestId, accId))
                .post("/api/1.0/payment")
                .then().assertThat().statusCode(409).body("result", equalTo("Rejected")).body("error", containsString("DuplicatedRequestException"));
    }

    @Test
    public void testTransfer_andDuplicateRequestRejected() {
        String fromAccId = createAccount();
        String toAccId = createAccount();
        createPayment(fromAccId, "100");
        createPayment(toAccId, "50");

        UUID requestId = UUID.randomUUID();
        Response response = given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":21.42}", requestId, fromAccId, toAccId))
                .post("/api/1.0/transfer")
                .thenReturn();
        Map<String, Object> fields = response.as(Map.class);
        assertThat(response.statusCode(), equalTo(200));
        String transaction = (String) fields.get("id");
        assertNotNull(transaction);
        assertThat(fields.get("result"), equalTo("Ok"));
        assertNotNull(fields.get("date_time"));

        get("/api/1.0/transaction/" + transaction)
                .then().assertThat().statusCode(200)
                .body("id", equalTo(transaction))
                .body("source_acc_id", equalTo(fromAccId))
                .body("target_acc_id", equalTo(toAccId))
                .body("amount", equalTo(21.42f))
                .body("date_time", containsString(valueOf(now().getYear())));
        get("/api/1.0/account/" + fromAccId)
                .then().assertThat().body(equalTo(format("{\"id\":\"%s\",\"balance\":78.58,\"result\":\"Ok\"}", fromAccId)));
        get("/api/1.0/account/" + toAccId)
                .then().assertThat().body(equalTo(format("{\"id\":\"%s\",\"balance\":71.42,\"result\":\"Ok\"}", toAccId)));

        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":21.42}", requestId, fromAccId, toAccId))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(409).body("result", equalTo("Rejected")).body("error", containsString("DuplicatedRequestException"));
    }

    @Test
    public void testTransfer_toTheSameAccount() {
        String fromAccId = createAccount();
        createPayment(fromAccId, "100");

        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":21.42}", UUID.randomUUID(), fromAccId, fromAccId))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("Source and target accounts should be different"));
    }

    @Test
    public void testTransfer_insufficientBalance() {
        String fromAccId = createAccount();
        String toAccId = createAccount();
        createPayment(fromAccId, "100");
        createPayment(toAccId, "50");

        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":101}", UUID.randomUUID(), fromAccId, toAccId))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Insufficient balance"));
    }

    @Test
    public void testTransfer_noSourceAccount() {
        String toAccId = createAccount();

        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":101}", UUID.randomUUID(), "abc123", toAccId))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Account (abc123) is not found"));
    }

    @Test
    public void testTransfer_noSourceParam() {
        given()
                .body(format("{\"request_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":101}", UUID.randomUUID(), "abc123"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("source_acc_id param should be filled"));
    }

    @Test
    public void testTransfer_noTargetAccount() {
        String fromAccId = createAccount();
        createPayment(fromAccId, "100");

        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":21.42}", UUID.randomUUID(), fromAccId, "abc124"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(500).body("result", equalTo("Error")).body("error", containsString("Account (abc124) is not found"));
    }

    @Test
    public void testTransfer_noTargetParam() {
        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"amount\":101}", UUID.randomUUID(), "abc123"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("target_acc_id param should be filled"));
    }

    @Test
    public void testTransfer_negativeAmount() {
        given()
                .body(format("{\"request_id\":\"%s\", \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":-34.56}", UUID.randomUUID(), "abc123", "abc124"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("Amount should be positive"));
    }

    @Test
    public void testTransfer_noRequestId() {
        given()
                .body(format("{ \"source_acc_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":21.42}", "abc123", "abc124"))
                .post("/api/1.0/transfer")
                .then().assertThat().statusCode(422).body("result", equalTo("Error")).body("error", containsString("request_id param should be filled"));
    }

    private String createAccount() {
        Response response = given()
                .body(format("{\"request_id\":\"%s\"}", UUID.randomUUID()))
                .post("/api/1.0/account")
                .thenReturn();
        assertThat(response.statusCode(), equalTo(200));
        Map<String, Object> fields = response.as(Map.class);
        String accId = (String) fields.get("id");
        log.debug("accId: {}", accId);
        return accId;
    }

    private void createPayment(String fromAccId, String amount) {
        String format = format("{\"request_id\":\"%s\", \"target_acc_id\":\"%s\", \"amount\":%s}", UUID.randomUUID(), fromAccId, amount);
        System.out.println(format);
        given()
                .body(format)
                .post("/api/1.0/payment")
                .then().assertThat().statusCode(200);
    }
}
