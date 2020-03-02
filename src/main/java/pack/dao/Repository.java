package pack.dao;

import pack.model.Account;
import pack.model.Request;
import pack.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface Repository {
    Request createRequest(String requestId);

    Account createAccount(String id);

    Account getAccount(String id);

    Transaction createTransaction(String toString, String fromId, String toId, BigDecimal amount, LocalDateTime now);

    Transaction getTransaction(String id);
}
