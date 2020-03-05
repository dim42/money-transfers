package pack.web.param

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import pack.web.Validator.requireNotBlank

class AccountRq @JsonCreator constructor(@JsonProperty("request_id") requestId: String?) : Rq {
    val requestId: String = requireNotBlank(requestId, "request_id")

    override val locks: List<String>
        get() = listOf(requestId)
}
