package pack.service

import org.slf4j.LoggerFactory
import pack.dao.Repository
import pack.dto.PaymentDto
import pack.dto.TransactionDto
import pack.dto.TransferDto
import pack.model.Account
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class PaymentService(private val repository: Repository) {
    fun createPayment(requestId: String, toId: String, amount: BigDecimal): PaymentDto {
        val request = repository.createRequest(requestId)
        val toAcct = repository.getAccount(toId)
        val tx = repository.createTransaction(UUID.randomUUID().toString(), null, toId, amount, LocalDateTime.now())
        toAcct.credit(amount)
        request.setRefId(tx.id)
        request.setDescription("payment")
        log.debug("New payment: {}", tx.id)
        return PaymentDto(tx.id, tx.time)
    }

    /*
         In presence of real DB accounts should be locked and checked/updated within the transaction.
    */
    fun transfer(requestId: String, fromId: String, toId: String, amount: BigDecimal): TransferDto {
        val request = repository.createRequest(requestId)
        val fromAcct = repository.getAccount(fromId)
        checkInsufficientBalance(fromAcct, amount)
        val toAcct = repository.getAccount(toId)
        val tx = repository.createTransaction(UUID.randomUUID().toString(), fromId, toId, amount, LocalDateTime.now())
        fromAcct.debit(amount)
        toAcct.credit(amount)
        request.setRefId(tx.id)
        request.setDescription("transfer")
        log.debug("New transfer: {}", tx.id)
        return TransferDto(tx.id, tx.time)
    }

    fun getTransaction(id: String): TransactionDto {
        val transaction = repository.getTransaction(id)
        return TransactionDto(transaction.id, transaction.fromId, transaction.toAcct, transaction.amount, transaction.time)
    }

    private fun checkInsufficientBalance(account: Account, amount: BigDecimal) {
        if (account.balance < amount) {
            throw RuntimeException("Insufficient balance (${account.balance}) for account:${account.id}, required $amount")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(PaymentService::class.java)
    }
}
