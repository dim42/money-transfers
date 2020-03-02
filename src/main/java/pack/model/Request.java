package pack.model;

import io.vertx.core.shareddata.Shareable;

import java.time.LocalDateTime;

public class Request implements Shareable {
    private final String id;
    private final LocalDateTime dateTime;
    private String refId;
    private String description;

    public Request(String id) {
        this.id = id;
        dateTime = LocalDateTime.now();
    }

    public void setRefId(String id) {
        refId = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
