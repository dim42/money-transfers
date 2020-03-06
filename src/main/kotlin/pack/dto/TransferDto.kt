package pack.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

class TransferDto(val id: String, private val time: LocalDateTime) {

    @JsonProperty("date_time")
    fun getTime(): String {
        return time.toString()
    }
}
