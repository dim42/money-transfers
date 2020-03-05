package pack.web

import java.lang.String.format
import java.math.BigDecimal
import java.math.RoundingMode

object Validator {
    fun requireNotBlank(string: String?, field: String): String {
        require(!(string == null || string.isBlank())) { format("%s param should be filled", field) }
        return string
    }

    fun checkWithScale(amount: BigDecimal?): BigDecimal {
        require(amount != null) { "amount param should be filled" }
        require(amount > BigDecimal.ZERO) { "Amount should be positive" }
        return amount.setScale(2, RoundingMode.HALF_UP)
    }
}
