package pack.model;

import io.vertx.core.shareddata.Shareable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction implements Shareable {
    private final String id;
    private final String fromId;
    private final String toAcct;
    private final BigDecimal amount;
    private final LocalDateTime time;
    // TODO: state

    public Transaction(String id, String fromId, String toAcct, BigDecimal amount, LocalDateTime time) {
        this.id = id;
        this.fromId = fromId;
        this.toAcct = toAcct;
        this.amount = amount;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToAcct() {
        return toAcct;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
