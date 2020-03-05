package pack.model;

import io.vertx.core.shareddata.Shareable;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

public class Account implements Shareable {

    private final String id;
    private BigDecimal balance = ZERO;
    // TODO: name, currency, state, created 

    public Account(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance != null ? balance : ZERO;
    }

    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance == null ? amount : balance.add(amount);
    }

    public boolean greater(Account account) {
        return id.compareTo(account.id) > 0;
    }
}
