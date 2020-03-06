package pack.web.param

import com.fasterxml.jackson.annotation.JsonProperty
import pack.web.Validator.checkAndScale
import pack.web.Validator.requireNotBlank
import java.math.BigDecimal

class PaymentRq(@JsonProperty("request_id") requestId: String?,
                @JsonProperty("target_acc_id") toId: String?,
                @JsonProperty("amount") amount: BigDecimal?) : Rq {
    val requestId: String = requireNotBlank(requestId, "request_id")
    val toId: String = requireNotBlank(toId, "target_acc_id")
    val amount: BigDecimal = checkAndScale(amount)

    override val locks: List<String>
        get() = listOf(toId)
}
