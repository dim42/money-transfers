package pack.web.param;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toList;

public class TransferRq implements Rq {
    // To provide idempotence of (duplicated) requests
    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty("source_acc_id")
    private String fromId;
    @JsonProperty("target_acc_id")
    private String toId;
    private BigDecimal amount;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
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
        return Stream.of(fromId, toId).sorted().collect(toList());
    }
}
