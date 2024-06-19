package github.cyberluke.reactive_logging.logging;

public class CustomRuntimeException extends RuntimeException {

    public CustomRuntimeException(Throwable e, String msg) {
        super(msg, e);
    }
}
