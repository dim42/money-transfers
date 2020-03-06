package pack.service

import org.slf4j.LoggerFactory
import pack.dao.Repository
import pack.dto.AccountDto
import java.util.*

class AccountService(private val repository: Repository) {
    fun createAccount(requestId: String): AccountDto {
        val request = repository.createRequest(requestId)
        val accountId = UUID.randomUUID().toString()
        val account = repository.createAccount(accountId)
        request.setRefId(account.id)
        request.setDescription("account")
        log.debug("New account: {}", account.id)
        return AccountDto(account.id, account.balance)
    }

    fun getAccount(id: String): AccountDto {
        val account = repository.getAccount(id)
        log.debug("Account: {}", account.id)
        return AccountDto(account.id, account.balance)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AccountService::class.java)
    }
}
