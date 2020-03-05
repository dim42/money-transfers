package pack.model

import io.vertx.core.shareddata.Shareable
import java.math.BigDecimal
import java.time.LocalDateTime

class Transaction // TODO: +state
(val id: String, val fromId: String?, val toAcct: String, val amount: BigDecimal, val time: LocalDateTime) : Shareable 
