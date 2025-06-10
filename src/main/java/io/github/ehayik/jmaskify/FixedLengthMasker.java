package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.repeat;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * Replaces the input string with a fixed number of substitution characters.
 *
 * <p>
 *      The length of the masked output is determined by a specified fixed length.
 *      This class is immutable and thread-safe.
 */
@Slf4j
public final class FixedLengthMasker implements Masker<String> {

    private final int fixedLength;
    private final char substitution;
    private final int prefixLength;
    private final int suffixLength;
    private final char charToIgnore;

    public FixedLengthMasker(int fixedLength, char substitution, int prefixLength, int suffixLength, char charToIgnore) {
        this.fixedLength = fixedLength;
        this.substitution = substitution;
        this.prefixLength = prefixLength;
        this.suffixLength = suffixLength;
        this.charToIgnore = charToIgnore;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @deprecated Use {@link #builder()} or {@link Masker#fixedLength()} instead
     */
    @Deprecated
    public FixedLengthMasker() {
        this(0, DEF_SUBSTITUTION_CHAR, 0, 0, '\0');
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

        if (charToIgnore != '\0') {
            return applyWithCharToIgnore(value);
        }

        if (prefixLength > 0 || suffixLength > 0) {
            return applyWithSuffixAndPrefixPreservation(value);
        }

        if (fixedLength <= 0) {
            log.debug("Fixed length is less than or equal to zero. Using value length as fixed length.");
            return repeat(substitution, value.length());
        }

        return repeat(substitution, fixedLength);
    }

    private String applyWithCharToIgnore(String value) {
        if (prefixLength > 0 || suffixLength > 0) {
            return applyWithSuffixAndPrefixPreservationAndCharToIgnore(value);
        }

        var  result = new StringBuilder();

        for (char c : value.toCharArray()) {
            if (c == charToIgnore) {
                result.append(c);
            } else {
                result.append(substitution);
            }
        }
        return result.toString();
    }

    private String applyWithSuffixAndPrefixPreservationAndCharToIgnore(String value) {
        var result = new StringBuilder();

        // Add prefix if specified and not exceeding the value length
        var effectivePrefixLength = Math.min(prefixLength, value.length());

        if (effectivePrefixLength > 0) {
            result.append(value, 0, effectivePrefixLength);
        }

        // Calculate the middle section to be masked
        var effectiveSuffixLength = Math.min(suffixLength, value.length() - effectivePrefixLength);
        int middleEnd = value.length() - effectiveSuffixLength;

        // Mask the middle section, preserving the character to ignore
        for (int i = effectivePrefixLength; i < middleEnd; i++) {
            char c = value.charAt(i);
            if (c == charToIgnore) {
                result.append(c);
            } else {
                result.append(substitution);
            }
        }

        // Add suffix if specified and not exceeding the remaining value length
        if (effectiveSuffixLength > 0) {
            result.append(value.substring(value.length() - effectiveSuffixLength));
        }

        return result.toString();
    }

    private String applyWithSuffixAndPrefixPreservation(String value) {
        var result = new StringBuilder();

        // Add prefix if specified and not exceeding the value length
        var effectivePrefixLength = Math.min(prefixLength, value.length());

        if (effectivePrefixLength > 0) {
            result.append(value, 0, effectivePrefixLength);
        }

        // Calculate the middle section to be masked
        var effectiveSuffixLength = Math.min(suffixLength, value.length() - effectivePrefixLength);

        // Use fixed length for the middle section if specified, otherwise calculate based on the original string
        int middleLength;

        if (fixedLength > 0) {
            middleLength = fixedLength;
        } else {
            middleLength = value.length() - effectivePrefixLength - effectiveSuffixLength;
        }

        if (middleLength > 0) {
            result.append(repeat(substitution, middleLength));
        }

        // Add suffix if specified and not exceeding the remaining value length
        if (effectiveSuffixLength > 0) {
            result.append(value.substring(value.length() - effectiveSuffixLength));
        }

        return result.toString();
    }

    /**
     * Builder for creating instances of {@link FixedLengthMasker}.
     */
    public static final class Builder implements MaskerBuilder<String, FixedLengthMasker> {

        private int fixedLength;
        private char substitution = DEF_SUBSTITUTION_CHAR;
        private int prefixLength;
        private int suffixLength;
        private char charToIgnore = '\0';

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

        public Builder preserveSuffix(int suffixLength) {
            this.suffixLength = suffixLength;
            return this;
        }

        public Builder preservePrefix(int prefixLength) {
            this.prefixLength = prefixLength;
            return this;
        }

        /**
         * Sets the character to ignore during masking.
         *
         * @param charToIgnore the character that should be preserved in the output
         * @return this builder for method chaining
         */
        public Builder ignore(char charToIgnore) {
            this.charToIgnore = charToIgnore;
            return this;
        }

        /**
         * Builds and returns a new {@link FixedLengthMasker} instance with the configured settings.
         *
         * @return a new {@link FixedLengthMasker} instance
         */
        @Override
        public FixedLengthMasker build() {
            return new FixedLengthMasker(fixedLength, substitution, prefixLength, suffixLength, charToIgnore);
        }
    }
}
