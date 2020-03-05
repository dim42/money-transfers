package pack.web.param

import pack.web.Validator.requireNotBlank

class GetAccountRq(id: String?) : Rq {
    val id: String = requireNotBlank(id, "account_id")

    override val locks: List<String>
        get() = listOf()
}
