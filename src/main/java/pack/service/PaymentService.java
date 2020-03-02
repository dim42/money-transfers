package pack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.dao.Repository;
import pack.dto.PaymentDto;
import pack.dto.TransactionDto;
import pack.dto.TransferDto;
import pack.model.Account;
import pack.model.Request;
import pack.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.String.format;

public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final Repository repository;

    public PaymentService(Repository repository) {
        this.repository = repository;
    }

    public PaymentDto createPayment(String requestId, String toId, BigDecimal amount) {
        Request request = repository.createRequest(requestId);
        Account toAcct = repository.getAccount(toId);
        Transaction tx = repository.createTransaction(UUID.randomUUID().toString(), null, toId, amount, LocalDateTime.now());
        toAcct.credit(amount);
        request.setRefId(tx.getId());
        request.setDescription("payment");
        log.debug("New payment: {}", tx.getId());
        return new PaymentDto(tx.getId(), tx.getTime());
    }

    /*
         In presence of real DB accounts should be locked and checked/updated within the transaction.
    */
    public TransferDto transfer(String requestId, String fromId, String toId, BigDecimal amount) {
        Request request = repository.createRequest(requestId);
        Account fromAcct = repository.getAccount(fromId);
        checkInsufficientBalance(fromAcct, amount);
        Account toAcct = repository.getAccount(toId);
        Transaction tx = repository.createTransaction(UUID.randomUUID().toString(), fromId, toId, amount, LocalDateTime.now());
        fromAcct.debit(amount);
        toAcct.credit(amount);
        request.setRefId(tx.getId());
        request.setDescription("transfer");
        log.debug("New transfer: {}", tx.getId());
        return new TransferDto(tx.getId(), tx.getTime());
    }

    private static void checkInsufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(format("Insufficient balance (%s) for account:%s, required %s", account.getBalance(), account.getId(), amount));
        }
    }

    public TransactionDto getTransaction(String id) {
        Transaction transaction = repository.getTransaction(id);
        return new TransactionDto(transaction.getId(), transaction.getFromId(), transaction.getToAcct(), transaction.getAmount(), transaction.getTime());
    }
}
