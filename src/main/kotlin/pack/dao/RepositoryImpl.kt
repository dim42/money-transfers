package pack.dao

import io.vertx.core.shareddata.SharedData
import pack.model.Account
import pack.model.Request
import pack.model.Transaction
import java.math.BigDecimal
import java.time.LocalDateTime

class RepositoryImpl(private val data: SharedData) : Repository {

    companion object {
        private const val REQUESTS_MAP = "requestMap"
        private const val ACCOUNTS_MAP = "accountsMap"
        private const val TRANSACTIONS_MAP = "transactionsMap"
    }

    override fun createRequest(id: String): Request {
        val map = data.getLocalMap<String, Request>(REQUESTS_MAP)
        return map.compute(id) { _: String?, prev: Request? ->
            if (prev != null) {
                throw DuplicatedRequestException("Request ($id) already exists")
            }
            Request(id)
        }!!
    }

    override fun createAccount(id: String): Account {
        val map = data.getLocalMap<String, Account>(ACCOUNTS_MAP)
        return map.compute(id) { _: String?, prev: Account? ->
            if (prev != null) {
                throw DuplicatedRequestException("Account ($id) already exists")
            }
            Account(id)
        }!!
    }

    override fun getAccount(id: String): Account {
        val map = data.getLocalMap<String, Account>(ACCOUNTS_MAP)
        return map[id] ?: throw RuntimeException("Account ($id) is not found")
    }

    override fun createTransaction(id: String, fromId: String?, toId: String, amount: BigDecimal, dateTime: LocalDateTime): Transaction {
        val map = data.getLocalMap<String, Transaction>(TRANSACTIONS_MAP)
        return map.compute(id) { _: String?, prev: Transaction? ->
            if (prev != null) {
                throw DuplicatedRequestException("Transaction ($id) already exists") // should not happen
            }
            Transaction(id, fromId, toId, amount, dateTime)
        }!!
    }

    override fun getTransaction(id: String): Transaction {
        val map = data.getLocalMap<String, Transaction>(TRANSACTIONS_MAP)
        return map[id] ?: throw RuntimeException("Transaction ($id) is not found")
    }
}
