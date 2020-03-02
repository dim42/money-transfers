package pack.web.param;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AccountRq implements Rq {

    private final String requestId;

    @JsonCreator
    public AccountRq(@JsonProperty(value = "request_id", required = true) String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    @Override
    public List<String> getLocks() {
        return List.of(requestId);
    }
}
