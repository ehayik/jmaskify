package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * Provides functionality to mask portions of a string based on specified multiline regular expressions.
 * <p>
 *     This class is immutable and thread-safe.
 *
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>
 * {@code
 * // Define patterns to mask sensitive data
 * String usernamePattern = "username\\s*=\\s*([^\\s]+)";
 * String ipAddressPattern = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
 *
 * // Create an instance of MultilinePatternMasker using the builder
 * MultilinePatternMasker masker = MultilinePatternMasker.builder()
 *     .withMaskPattern(usernamePattern)
 *     .withSubstitution('*')  // Define the substitution character for default masking strategy
 *     .withMaskPattern(ipAddressPattern, Masker.fixedLength().withSubstitution('■').ignore('.'))
 *     .build();
 *
 * // Input string with sensitive data spread across multiple lines
 * String input = "Line 1 with username=johndoe sensitive data.\n"
 *              + "Line 2 192.168.1.1 with more sensitive info.";
 *
 * // Apply the masker to the input
 * String maskedOutput = masker.apply(input);
 *
 * // Result: sensitive data is masked, i.e.,
 * // Line 1 with username=******* sensitive data.
 * // Line 2 ■■■.■■■.■.■ with more sensitive info.
 * }
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public final class MultilinePatternMasker implements Masker<String>, Buildable<String, MultilinePatternMasker> {

    private final char substitution;

    @Nullable
    private final Pattern multilinePattern;

    private final Map<Pattern, Masker<String>> customMaskerPatterns;

    public static MultilinePatternMasker.Builder builder() {
        return new MultilinePatternMasker.Builder();
    }

    /**
     * Applies the specified multiline pattern to the given text,
     * replacing all matched portions with the substitution character.
     *
     * <p>
     *      Each match found by the patterns is replaced with a specified substitution character.
     *
     * @param text the input string to be processed, may be {@code null}
     * @return the processed string with substitutions, or {@code null} if the input text was {@code null}
     */
    @Nullable
    @Override
    public String apply(@Nullable String text) {

        if (text == null) {
            log.debug("Input text is null. Returning null.");
            return null;
        }

        return applyMultilinePattern(applyCustomMaskerPatterns(text));
    }

    private String applyCustomMaskerPatterns(String text) {

        if (customMaskerPatterns.isEmpty()) {
            log.debug("No custom masker patterns were specified.");
            return text;
        }

        var result = text;

        for (var entry : customMaskerPatterns.entrySet()) {
            result = applyCustomMaskerPattern(entry.getKey(), entry.getValue(), result);
        }

        return result;
    }

    private String applyCustomMaskerPattern(Pattern pattern, Masker<String> masker, String text) {
        var matcher = pattern.matcher(text);
        var sb = new StringBuilder();

        // Find and replace each match
        while (matcher.find()) {
            var matched = matcher.group(0);
            var masked = masker.apply(matched);
            // Quote the replacement string to avoid issues with special characters
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private String applyMultilinePattern(String text) {

        if (multilinePattern == null) {
            log.debug("No default masker patterns were specified.");
            return text;
        }

        var sb = new StringBuilder(text);
        var matcher = multilinePattern.matcher(sb);

        while (matcher.find()) {
            maskMatchedContent(matcher, sb);
        }

        return sb.toString();
    }

    private void maskMatchedContent(Matcher matcher, StringBuilder sb) {
        int groupCount = matcher.groupCount();

        if (groupCount == 0) {
            log.debug("Pattern has no groups. Masking entire match.");
            maskRange(sb, matcher.start(), matcher.end());
            return;
        }

        log.debug("Masking captured groups.");
        boolean anyGroupCaptured = maskCapturedGroups(matcher, sb, groupCount);

        if (!anyGroupCaptured) {
            log.debug("No captured groups were found. Masking entire match.");
            maskRange(sb, matcher.start(), matcher.end());
        }
    }

    private boolean maskCapturedGroups(Matcher matcher, StringBuilder sb, int groupCount) {
        boolean anyGroupCaptured = false;
        for (int group = 1; group <= groupCount; group++) {
            if (matcher.group(group) != null) {
                anyGroupCaptured = true;
                maskRange(sb, matcher.start(group), matcher.end(group));
            }
        }
        return anyGroupCaptured;
    }

    private void maskRange(StringBuilder sb, int start, int end) {
        for (int i = start; i < end; i++) {
            sb.setCharAt(i, substitution);
        }
    }

    /**
     *
     * @return {@link MultilinePatternMasker.Builder builder} to create a new {@code MultilinePatternMasker}
     * whose settings are replicated from the current {@code MultilinePatternMasker}.
     */
    @Override
    public Builder mutate() {
        var builder = builder().withSubstitution(substitution);

        if (multilinePattern != null) {
            log.debug("Retaining default masker patterns from existing multiline pattern.");
            var patterns = multilinePattern.pattern().split("\\|", -1);
            Arrays.stream(patterns).forEach(builder::withMaskPattern);
        }

        customMaskerPatterns.forEach(builder::withMaskPattern);
        return builder;
    }

    /**
     * A builder class for creating instances of {@link MultilinePatternMasker}.
     * <p>
     * This builder provides methods to specify the masking patterns and substitution characters
     * to be used when constructing a {@link MultilinePatternMasker}.
     * It allows a flexible and intuitive approach to configuring masker objects through fluent API calls.
     */
    public static final class Builder implements MaskerBuilder<String, MultilinePatternMasker> {

        private static final String DUPLICATE_PATTERN_ERROR_MSG =
                "Duplicate pattern detected: '%s' is already registered.";

        private char substitution = DEF_SUBSTITUTION_CHAR;
        private final Set<String> defaultMaskerPatterns = new HashSet<>();
        private final Map<Pattern, Masker<String>> customMaskerPatterns = new HashMap<>();

        /**
         * Adds a new regular expression pattern to the list of patterns that will
         * be used for masking sensitive data.
         *
         * @param maskPattern the regular expression pattern used for matching sensitive data
         * @return the builder instance for chaining method calls
         * @throws IllegalArgumentException if {@code maskPattern} is {@code null}, blank or
         * a duplicate pattern is detected
         */
        public Builder withMaskPattern(String maskPattern) {

            if (isBlank(maskPattern)) {
                throw new IllegalArgumentException("Mask pattern cannot be blank");
            }

            var isDuplicateDetect =
                    customMaskerPatterns.keySet().stream().map(Pattern::pattern).anyMatch(maskPattern::equals);

            if (isDuplicateDetect) {
                throw new IllegalArgumentException(DUPLICATE_PATTERN_ERROR_MSG.formatted(maskPattern));
            }

            defaultMaskerPatterns.add(maskPattern);
            return this;
        }

        /**
         * Adds a new regular expression pattern with a specific masking strategy.
         *
         * @param pattern the regular expression pattern used for matching sensitive data
         * @param masker the masking strategy to apply to the matched pattern
         * @return the builder instance for chaining method calls
         * @throws IllegalArgumentException if {@code pattern} is {@code null}, blank or
         * a duplicate pattern is detected
         * @throws NullPointerException if {@code masker} is {@code null}
         */
        public Builder withMaskPattern(String pattern, Masker<String> masker) {

            if (isBlank(pattern)) {
                throw new IllegalArgumentException("Pattern cannot be blank");
            }

            if (defaultMaskerPatterns.contains(pattern)) {
                throw new IllegalArgumentException(DUPLICATE_PATTERN_ERROR_MSG.formatted(pattern));
            }

            customMaskerPatterns.put(Pattern.compile(pattern), masker);
            return this;
        }

        void withMaskPattern(Pattern pattern, Masker<String> masker) {

            if (defaultMaskerPatterns.contains(pattern.pattern())) {
                throw new IllegalArgumentException(DUPLICATE_PATTERN_ERROR_MSG.formatted(pattern));
            }

            customMaskerPatterns.put(pattern, masker);
        }

        /**
         * Adds a new regular expression pattern with a specific masking strategy builder.
         * The builder will be built to get the actual masker.
         *
         * @param pattern the regular expression pattern used for matching sensitive data
         * @param maskerBuilder the masking strategy builder to build and apply to the matched pattern
         * @return the builder instance for chaining method calls
         * @throws IllegalArgumentException if {@code pattern} is {@code null}, blank or
         * a duplicate pattern is detected
         * @throws NullPointerException if {@code maskerBuilder} is {@code null}
         */
        public Builder withMaskPattern(String pattern, MaskerBuilder<String, ?> maskerBuilder) {
            return withMaskPattern(pattern, maskerBuilder.build());
        }

        /**
         * Sets the substitution character to be used for replacing matched sensitive data with the default masking strategy.
         *
         * <p>
         *      If not explicitly set, the default substitution character defined in the implementation
         *      will be used.
         *
         * @param substitution the character used for substitution
         * @return the builder instance for chaining method calls
         */
        public Builder withSubstitution(char substitution) {
            this.substitution = substitution;
            return this;
        }

        /**
         * Constructs a new {@link MultilinePatternMasker} instance
         * and applies the specified multiline pattern to the given text.
         *
         * <p>
         *      Each match found by the patterns is replaced with a specified substitution character.
         *
         * @param text the input string to be processed, may be {@code null}
         * @return the processed string with substitutions, or {@code null} if the input text was {@code null}
         * @see MultilinePatternMasker#apply(String)
         */
        @Nullable
        @Override
        public String apply(@Nullable String text) {
            return build().apply(text);
        }

        /**
         * Returns a composed {@link Function} that first applies the current masking operation and then
         * applies the given {@code Masker<String>} operation.
         *
         * @param after the {@code Masker<String>} operation to apply after the current masking operation
         * @return a composed {@code Function<String, String>} that applies the current operation followed by the given one
         * @throws NullPointerException if {@code after} is {@code null}
         */
        @Override
        public Function<String, String> andThen(Masker<String> after) {
            return build().andThen(after);
        }

        /**
         * Constructs and returns a new {@link MultilinePatternMasker} instance
         * based on the configured patterns and substitution character.
         *
         * @return a new {@link MultilinePatternMasker} instance
         * @throws IllegalStateException if no patterns were added to the builder
         */
        @Override
        public MultilinePatternMasker build() {

            if (defaultMaskerPatterns.isEmpty() && customMaskerPatterns.isEmpty()) {
                throw new IllegalStateException(
                        "At least one masking pattern must be specified. Use withMaskPattern() to add patterns.");
            }

            return new MultilinePatternMasker(substitution, compileMultilinePattern(), customMaskerPatterns);
        }

        @Nullable
        private Pattern compileMultilinePattern() {

            if (defaultMaskerPatterns.isEmpty()) {
                return null;
            }

            return Pattern.compile(String.join("|", defaultMaskerPatterns), Pattern.MULTILINE);
        }
    }
}
