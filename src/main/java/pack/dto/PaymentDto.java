package pack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class PaymentDto {
    private final String id;
    private final LocalDateTime time;

    public PaymentDto(String id, LocalDateTime time) {
        this.id = id;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    @JsonProperty("date_time")
    public String getTime() {
        return time.toString();
    }
}
