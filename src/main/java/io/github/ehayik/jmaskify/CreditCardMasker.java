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

    private final char substitution;

    /**
     * Masks provided credit card value based on predefined rules.
     * <ul>
     *     <li>If the input is {@code null} or blank, it returns the input as is.</li>
     *     <li>For card numbers without dashes, it keeps only the last four digits visible and masks the rest.</li>
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

        // Remove any whitespace
        var cleanValue = value.trim();

        // Handle cards without dashes (LASER)
        if (!cleanValue.contains("-")) {
            return repeat(substitution, 12) + cleanValue.substring(cleanValue.length() - 4);
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
}
