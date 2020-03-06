package pack.web

import java.math.BigDecimal
import java.math.RoundingMode

object Validator {
    fun requireNotBlank(string: String?, field: String): String {
        require(!(string == null || string.isBlank())) { "$field param should be filled" }
        return string
    }

    fun checkAndScale(amount: BigDecimal?): BigDecimal {
        require(amount != null) { "amount param should be filled" }
        require(amount > BigDecimal.ZERO) { "Amount should be positive" }
        return amount.setScale(2, RoundingMode.HALF_UP)
    }
}
