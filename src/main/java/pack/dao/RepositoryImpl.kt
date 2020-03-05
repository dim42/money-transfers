package pack.dao;

import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import pack.model.Account;
import pack.model.Request;
import pack.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.lang.String.format;

public class RepositoryImpl implements Repository {

    private static final String REQUESTS_MAP = "requestMap";
    private static final String ACCOUNTS_MAP = "accountsMap";
    private static final String TRANSACTIONS_MAP = "transactionsMap";

    private final SharedData data;

    public RepositoryImpl(SharedData data) {
        this.data = data;
    }

    @Override
    public Request createRequest(String id) {
        LocalMap<String, Request> map = data.getLocalMap(REQUESTS_MAP);
        return map.compute(id, (id_, prev) -> {
            if (prev != null) {
                throw new DuplicatedRequestException(format("Request (%s) already exists", id));
            }
            return new Request(id);
        });
    }

    @Override
    public Account createAccount(String id) {
        LocalMap<String, Account> map = data.getLocalMap(ACCOUNTS_MAP);
        return map.compute(id, (id_, prev) -> {
            if (prev != null) {
                throw new DuplicatedRequestException(format("Account (%s) already exists", id));
            }
            return new Account(id);
        });
    }

    @Override
    public Account getAccount(String id) {
        LocalMap<String, Account> map = data.getLocalMap(ACCOUNTS_MAP);
        Account account = map.get(id);
        if (account == null) {
            throw new RuntimeException(format("Account (%s) is not found", id));
        }
        return account;
    }

    @Override
    public Transaction createTransaction(String id, String fromId, String toId, BigDecimal amount, LocalDateTime dateTime) {
        LocalMap<String, Transaction> map = data.getLocalMap(TRANSACTIONS_MAP);
        return map.compute(id, (id_, prev) -> {
            if (prev != null) {
                throw new DuplicatedRequestException(format("Transaction (%s) already exists", id));// should not happen
            }
            return new Transaction(id, fromId, toId, amount, dateTime);
        });
    }

    @Override
    public Transaction getTransaction(String id) {
        LocalMap<String, Transaction> map = data.getLocalMap(TRANSACTIONS_MAP);
        Transaction transaction = map.get(id);
        if (transaction == null) {
            throw new RuntimeException(format("Transaction (%s) is not found", id));
        }
        return transaction;
    }
}
