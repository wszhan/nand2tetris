public class ExhaustedTokenException extends RuntimeException {

    public ExhaustedTokenException() {
        super();
    }

    public ExhaustedTokenException(String errorMessage) {
        super(errorMessage);
    }
    
}