package pack.model

import io.vertx.core.shareddata.Shareable
import java.time.LocalDateTime

class Request(val id: String, val dateTime: LocalDateTime = LocalDateTime.now()) : Shareable {
    private var refId: String? = null
    private var description: String? = null
    fun setRefId(id: String?) {
        refId = id
    }

    fun setDescription(description: String?) {
        this.description = description
    }
}
