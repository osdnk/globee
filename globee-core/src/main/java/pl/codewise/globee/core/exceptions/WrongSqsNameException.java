package pl.codewise.globee.core.exceptions;

public class WrongSqsNameException extends Exception {

    public WrongSqsNameException() {
        super();
    }

    public WrongSqsNameException(String message) {
        super(message);
    }

    public WrongSqsNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongSqsNameException(Throwable cause) {
        super(cause);
    }
}
