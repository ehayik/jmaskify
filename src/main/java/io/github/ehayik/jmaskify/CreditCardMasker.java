package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.repeat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Focuses on masking credit card numbers, ensuring that only specific parts of the credit
 * card number remain visible after masking.
 */
@Slf4j
@RequiredArgsConstructor
final class CreditCardMasker implements Masker<String> {

    private static final int UNDASHED_VISIBLE_DIGITS = 4;
    private static final int UNDASHED_MASKED_DIGITS = 12;
    private static final int UNDASHED_FULL_MASK_LENGTH = UNDASHED_MASKED_DIGITS + UNDASHED_VISIBLE_DIGITS;

    private final char substitution;

    /**
     * Masks provided credit card value based on predefined rules.
     * <ul>
     *     <li>If the input is {@code null} or blank, it returns the input as is.</li>
     *     <li>For card numbers without dashes longer than four characters (after removing leading and
     *         trailing whitespace and control characters), it returns twelve substitution characters
     *         followed by the last four characters.</li>
     *     <li>For card numbers without dashes of four characters or fewer (after removing leading and
     *         trailing whitespace and control characters), it fully masks the value, returning sixteen
     *         substitution characters.</li>
     *     <li>For card numbers with dashes, it masks all parts except the last segment, with special handling
     *         for cards formatted as "XXXX-XXXXXX-XXXXX" (e.g., American Express or Diners Club),
     *         where only the last segment is preserved.</li>
     * </ul>
     *
     * @param value the credit card number to be masked; can include or exclude dashes as delimiters
     * @return the masked credit card number, preserving only specific visible parts based on the format
     */
    @Override
    public String apply(String value) {

        if (value == null || value.isBlank()) {
            log.debug("Input value is null or blank. Returning value as is.");
            return value;
        }

        var cleanValue = stripPadding(value);

        // Handle cards without dashes (LASER)
        if (!cleanValue.contains("-")) {
            if (cleanValue.length() <= UNDASHED_VISIBLE_DIGITS) {
                return repeat(substitution, UNDASHED_FULL_MASK_LENGTH);
            }
            return repeat(substitution, UNDASHED_MASKED_DIGITS)
                    + cleanValue.substring(cleanValue.length() - UNDASHED_VISIBLE_DIGITS);
        }

        // Split the string by dashes
        var parts = cleanValue.split("-", -1);
        var masked = new StringBuilder();

        // Special handling for AMERICAN_EXPRESS and DINERS_CLUB
        // Format: XXXX-XXXXXX-42008 (keeping last 5 digits)
        if (parts.length == 3 && parts[1].length() == 6) {
            return "%s-%s-%s".formatted(repeat(substitution, 4), repeat(substitution, 6), parts[2]);
        }

        // Standard handling for other card types
        var standardCardMask = repeat(substitution, 4) + "-";
        masked.append(standardCardMask.repeat(Math.max(0, parts.length - 1)));
        return masked.append(parts[parts.length - 1]).toString();
    }

    // Remove leading and trailing whitespace and control characters
    private static String stripPadding(String value) {
        var start = 0;
        var end = value.length();
        while (start < end && isPadding(value.charAt(start))) {
            start++;
        }
        while (end > start && isPadding(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(start, end);
    }

    private static boolean isPadding(char c) {
        return c <= ' ' || Character.isWhitespace(c);
    }
}
