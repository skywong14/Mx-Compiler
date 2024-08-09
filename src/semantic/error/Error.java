package semantic.error;

public class Error extends Exception {
    String message;

    public Error(String message_) {
        super(message_);
        message = message_;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
