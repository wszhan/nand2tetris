
public class InvalidCommandException extends RuntimeException {
    public InvalidCommandException() {
        super();
    }

    public InvalidCommandException(String errorMessage) {
        super(errorMessage);
    }
}
