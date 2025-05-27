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
public final class FixedLengthMasker implements Masker<String> {

    private final int fixedLength;
    private final char substitution;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @deprecated Use {@link #builder()} or {@link Masker#fixedLength()} instead
     */
    @Deprecated
    public FixedLengthMasker() {
        this(0, DEF_SUBSTITUTION_CHAR);
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
            return repeat(substitution, value.length());
        }

        return repeat(substitution, fixedLength);
    }

    /**
     * Builder for creating instances of {@link FixedLengthMasker}.
     */
    public static final class Builder implements MaskerBuilder<String, FixedLengthMasker> {

        private int fixedLength;
        private char substitution = DEF_SUBSTITUTION_CHAR;

        /**
         * Sets the fixed length for the masker.
         *
         * @param fixedLength the length to which the input string should be masked.
         *                   If the value is zero or negative,
         *                  the input string is masked to the same length as the input string.
         * @return this builder for method chaining
         */
        public Builder withFixedLength(int fixedLength) {
            this.fixedLength = fixedLength;
            return this;
        }

        /**
         * Sets the substitution character for the masker.
         *
         * @param substitution the character to use for masking.
         *                        If not specified, defaults to {@link Masker#DEF_SUBSTITUTION_CHAR}.
         * @return this builder for method chaining
         */
        public Builder withSubstitution(char substitution) {
            this.substitution = substitution;
            return this;
        }

        /**
         * Builds and returns a new {@link FixedLengthMasker} instance with the configured settings.
         *
         * @return a new {@link FixedLengthMasker} instance
         */
        @Override
        public FixedLengthMasker build() {
            return new FixedLengthMasker(fixedLength, substitution);
        }
    }
}
