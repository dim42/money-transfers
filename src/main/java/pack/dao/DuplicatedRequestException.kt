package pack.dao;

public class DuplicatedRequestException extends RuntimeException {
    public DuplicatedRequestException(String id) {
        super(id);
    }
}
