package io.github.ehayik.jmaskify;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * Provides Base64 encoding as a masking strategy for string values.
 *
 * <p>
 *      This class is immutable and thread-safe.
 */
@Slf4j
final class Base64Masker implements Masker<String> {

    /**
     * Applies Base64 encoding to the given input string.
     *
     * <p>
     *      Converts the input string to its Base64 encoded representation.
     *      It uses UTF-8 encoding to convert the string to a byte array before encoding.
     *
     * @param value the input string to be encoded, can be {@code null}
     * @return the Base64 encoded representation of the input string,
     * or {@code null} if the input was {@code null}
     */
    @Override
    @Nullable
    @SneakyThrows
    public String apply(@Nullable String value) {

        if (value == null) {
            log.debug("Input value is null. Returning null");
            return null;
        }

        byte[] jsonBytes = value.getBytes(UTF_8);
        return encodeBase64String(jsonBytes);
    }
}
