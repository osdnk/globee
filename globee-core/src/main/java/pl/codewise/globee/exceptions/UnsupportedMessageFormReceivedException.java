package pl.codewise.globee.exceptions;

public class UnsupportedMessageFormReceivedException extends Exception {

    public UnsupportedMessageFormReceivedException() {
        super();
    }

    public UnsupportedMessageFormReceivedException(String message) {
        super(message);
    }

    public UnsupportedMessageFormReceivedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedMessageFormReceivedException(Throwable cause) {
        super(cause);
    }
}
