package pack.web.param;

import java.util.List;

public class GetAccountRq implements Rq {

    private final String id;

    public GetAccountRq(String id) {
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
