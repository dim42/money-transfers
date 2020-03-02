package pack.web.param;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

public class PaymentRq implements Rq {
    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty("target_acc_id")
    private String toId;
    private BigDecimal amount;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, HALF_UP);
    }

    @Override
    public List<String> getLocks() {
        return List.of(toId);
    }
}
