package pack.web.param

import com.fasterxml.jackson.annotation.JsonProperty
import pack.web.Validator.checkWithScale
import pack.web.Validator.requireNotBlank
import java.math.BigDecimal
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class TransferRq( // To provide idempotence of (duplicated) requests
        @JsonProperty("request_id") requestId: String?,
        @JsonProperty("source_acc_id") fromId: String?,
        @JsonProperty("target_acc_id") toId: String?,
        @JsonProperty("amount") amount: BigDecimal?) : Rq {
    val requestId: String = requireNotBlank(requestId, "request_id")
    val fromId: String = requireNotBlank(fromId, "source_acc_id")
    val toId: String = requireNotBlank(toId, "target_acc_id")
    val amount: BigDecimal = checkWithScale(amount)

    init {
        require(fromId != toId) { "Source and target accounts should be different" }
    }

    override val locks: List<String>
        get() = Stream.of(fromId, toId).sorted().collect(toList())
}
