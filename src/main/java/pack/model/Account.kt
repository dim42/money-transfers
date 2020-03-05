package pack.model

import io.vertx.core.shareddata.Shareable
import java.math.BigDecimal

class Account // TODO: +name, currency, state, created 
(val id: String, var balance: BigDecimal = BigDecimal.ZERO) : Shareable {

    fun debit(amount: BigDecimal?) {
        balance = balance.subtract(amount)
    }

    fun credit(amount: BigDecimal) {
        balance = balance.add(amount)
    }

    fun greater(account: Account): Boolean {
        return id > account.id
    }
}
