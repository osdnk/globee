package pl.codewise.globee.client.exceptions;

public class GlobeeClientException extends Exception {

    public GlobeeClientException() {
        super();
    }

    public GlobeeClientException(String message) {
        super(message);
    }

    public GlobeeClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public GlobeeClientException(Throwable cause) {
        super(cause);
    }
}
