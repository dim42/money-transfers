package pack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.dao.Repository;
import pack.dto.AccountDto;
import pack.model.Account;
import pack.model.Request;

import java.util.UUID;

public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final Repository repository;

    public AccountService(Repository repository) {
        this.repository = repository;
    }

    public AccountDto createAccount(String requestId) {
        Request request = repository.createRequest(requestId);
        String accountId = UUID.randomUUID().toString();
        Account account = repository.createAccount(accountId);
        request.setRefId(account.getId());
        request.setDescription("account");
        log.debug("New account: {}", account.getId());
        return new AccountDto(account.getId(), account.getBalance());
    }

    public AccountDto getAccount(String id) {
        Account account = repository.getAccount(id);
        log.debug("Account: {}", account.getId());
        return new AccountDto(account.getId(), account.getBalance());
    }
}
