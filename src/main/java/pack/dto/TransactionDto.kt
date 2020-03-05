package pack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {
    private final String id;
    private final String fromId;
    private final String toAcct;
    private final BigDecimal amount;
    private final LocalDateTime time;

    public TransactionDto(String id, String fromId, String toAcct, BigDecimal amount, LocalDateTime time) {
        this.id = id;
        this.fromId = fromId;
        this.toAcct = toAcct;
        this.amount = amount;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    @JsonProperty("source_acc_id")
    public String getFromId() {
        return fromId;
    }

    @JsonProperty("target_acc_id")
    public String getToAcct() {
        return toAcct;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @JsonProperty("date_time")
    public String getTime() {
        return time.toString();
    }
}
