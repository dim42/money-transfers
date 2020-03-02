package pack.web.param;

import java.util.List;

public class GetTransactionRq implements Rq {

    private final String id;

    public GetTransactionRq(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public List<String> getLocks() {
        return List.of();
    }
}
