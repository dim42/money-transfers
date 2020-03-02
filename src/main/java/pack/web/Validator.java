package pack.web;

import pack.web.param.*;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;

public final class Validator {

    public static void validate(AccountRq rq) {
        requireNotBlank(rq.getRequestId(), "request_id");
    }

    public static void validate(GetAccountRq rq) {
        requireNotBlank(rq.getId(), "account_id");
    }

    public static void validate(PaymentRq rq) {
        requireNotBlank(rq.getRequestId(), "request_id");
        requireNotBlank(rq.getToId(), "target_acc_id");
        if (rq.getAmount().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Amount should be positive");
        }
    }

    public static void validate(TransferRq rq) {
        requireNotBlank(rq.getRequestId(), "request_id");
        requireNotBlank(rq.getFromId(), "source_acc_id");
        requireNotBlank(rq.getToId(), "target_acc_id");
        if (rq.getAmount().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Amount should be positive");
        }
        if (rq.getFromId().equals(rq.getToId())) {
            throw new IllegalArgumentException("Source and target accounts should be different");
        }
    }

    public static void validate(GetTransactionRq rq) {
        requireNotBlank(rq.getId(), "transaction_id");
    }

    public static void requireNotBlank(String string, String field) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException(format("%s param should be filled", field));
        }
    }
}
