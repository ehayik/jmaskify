package io.github.ehayik.jmaskify;

/**
 * A custom exception class used to signify errors occurring during the masking process.
 */
public class MaskingException extends RuntimeException {

    public MaskingException(String message) {
        super(message);
    }

    public MaskingException(String message, Throwable cause) {
        super(message, cause);
    }
}
