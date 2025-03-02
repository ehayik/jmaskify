package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.repeat;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Replaces the input string with a fixed number of substitution characters.
 *
 * <p>
 *      The length of the masked output is determined by a specified fixed length.
 *      This class is immutable and thread-safe.
 */
@Slf4j
@RequiredArgsConstructor
final class FixedLengthMasker implements Masker<String> {

    private final int fixedLength;

    public FixedLengthMasker() {
        this(0);
    }

    /**
     * Applies a masking operation to the given string value based on the fixed length.
     *
     * <p>
     *      If the input string is blank, it returns the input unchanged.
     *      If the fixed length is zero or negative,
     *      the entire input string is masked to the same length as the input string using the substitution character.
     *
     * @param value the input string to be masked, can be {@code null}
     * @return the masked string according to the fixed length,
     * or {@code null} if the input was {@code null}
     */
    @Nullable
    @Override
    public String apply(@Nullable String value) {

        if (value == null) {
            log.debug("Input value is null. Returning null");
            return null;
        }

        if (fixedLength <= 0) {
            log.debug("Fixed length is less than or equal to zero. Using value length as fixed length.");
            return repeat(DEF_SUBSTITUTION_CHAR, value.length());
        }

        return repeat(DEF_SUBSTITUTION_CHAR, fixedLength);
    }
}
