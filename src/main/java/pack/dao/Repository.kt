package pack.dao

import pack.model.Account
import pack.model.Request
import pack.model.Transaction
import java.math.BigDecimal
import java.time.LocalDateTime

interface Repository {
    fun createRequest(id: String): Request
    fun createAccount(id: String): Account
    fun getAccount(id: String): Account
    fun createTransaction(id: String, fromId: String?, toId: String, amount: BigDecimal, dateTime: LocalDateTime): Transaction
    fun getTransaction(id: String): Transaction
}
