package pack.web.param

import pack.web.Validator.requireNotBlank

class GetTransactionRq(id: String?) : Rq {
    val id: String = requireNotBlank(id, "transaction_id")

    override val locks: List<String>
        get() = listOf()
}
