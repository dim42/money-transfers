package pack.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

class TransactionDto(val id: String, @get:JsonProperty("source_acc_id") val fromId: String?, @get:JsonProperty("target_acc_id") val toAcct: String, val amount: BigDecimal, private val time: LocalDateTime) {

    @JsonProperty("date_time")
    fun getTime(): String {
        return time.toString()
    }
}
