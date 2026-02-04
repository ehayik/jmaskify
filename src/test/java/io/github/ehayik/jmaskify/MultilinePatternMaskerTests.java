package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MultilinePatternMaskerTests {

    private static final Faker FAKER = new Faker();
    private static final String USERNAME_PATTERN = "username\\s*=\\s*([^\\s]+)";
    private static final String IP_ADDRESS_PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
    private static final String EMAIL_PATTERN = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
    private static final String CREDIT_CARD_PATTERN = "\\b\\d{4}-\\d{4}-\\d{4}-\\d{4}\\b";
    private static final String GIVEN_MULTILINE_TEXT =
            """
				2023-05-15 INFO User john.doe@example.com logged in
				2023-05-15 INFO Processing payment with card 5431-8923-1203-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				2023-05-15 INFO IP Address: 192.168.1.1
				""";

    @Test
    void shouldMaskSingleLine() {
        // Given
        var username = FAKER.internet().username();
        var inputValue = "This is username=%s sensitive data.".formatted(username);
        var expectedValue = "This is username=%s sensitive data.".formatted(repeat("*", username.length()));
        var masker = Masker.multilinePattern().withMaskPattern(USERNAME_PATTERN);

        // When -Then
        assertThat(masker.apply(inputValue)).isEqualTo(expectedValue);
    }

    @Test
    void shouldMaskMultiLine() {
        // Given
        var username = FAKER.internet().username();
        var ipAddress = FAKER.internet().ipV4Address();

        var inputValue = """
		Line 1 with username=%s sensitive data.
		Line 2 %s with more sensitive info.
		"""
                .formatted(username, ipAddress);

        var expectedValue = """
		Line 1 with username=%s sensitive data.
		Line 2 %s with more sensitive info.
		"""
                .formatted(repeat("*", username.length()), repeat("*", ipAddress.length()));

        var masker = Masker.multilinePattern().withMaskPattern(USERNAME_PATTERN).withMaskPattern(IP_ADDRESS_PATTERN);

        // When -Then
        assertThat(masker.apply(inputValue)).isEqualTo(expectedValue);
    }

    @Test
    void shouldNotMaskNull() {
        // Given
        var masker = Masker.multilinePattern().withMaskPattern(IP_ADDRESS_PATTERN);

        // When - Then
        assertThat(masker.apply(null)).isNull();
    }

    @Test
    void shouldMaskWithCustomSubstitution() {
        // Given
        var ipAddress = FAKER.internet().ipV4Address();
        var inputValue = "This is %s sensitive data.".formatted(ipAddress);
        var expectedValue = "This is %s sensitive data.".formatted(repeat("#", ipAddress.length()));
        var masker =
                Masker.multilinePattern().withMaskPattern(IP_ADDRESS_PATTERN).withSubstitution('#');

        // When -Then
        assertThat(masker.apply(inputValue)).isEqualTo(expectedValue);
    }

    @Test
    void shouldMaskWhenCustomAndDefaultMaskingStrategiesConfigured() {
        // Given
        var expectedText =
                """
				2023-05-15 INFO User ******************** logged in
				2023-05-15 INFO Processing payment with card XXXX-XXXX-XXXX-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				2023-05-15 INFO IP Address: ■■■.■■■.■.■
				""";

        var masker = Masker.multilinePattern()
                .withMaskPattern(EMAIL_PATTERN)
                .withMaskPattern(
                        IP_ADDRESS_PATTERN,
                        Masker.fixedLength().withSubstitution('■').ignore('.'))
                .withMaskPattern(CREDIT_CARD_PATTERN, Masker.creditCard('X'))
                .build();

        // When
        var actualText = masker.apply(GIVEN_MULTILINE_TEXT);

        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }

    @Test
    void shouldMaskWhenCustomMaskingStrategiesConfigured() {
        // Given
        var expectedText =
                """
				2023-05-15 INFO User john.doe@example.com logged in
				2023-05-15 INFO Processing payment with card XXXX-XXXX-XXXX-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				2023-05-15 INFO IP Address: ■■■.■■■.■.■
				""";

        var masker = Masker.multilinePattern()
                .withMaskPattern(
                        IP_ADDRESS_PATTERN,
                        Masker.fixedLength().withSubstitution('■').ignore('.'))
                .withMaskPattern(CREDIT_CARD_PATTERN, Masker.creditCard('X'))
                .build();

        // When
        var actualText = masker.apply(GIVEN_MULTILINE_TEXT);

        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }

    @Test
    void shouldFailsWhenNoPatternSpecified() {
        // Given
        var multilineMasker = Masker.multilinePattern();

        // When - Then
        assertThatThrownBy(() -> multilineMasker.apply("Hello World!"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("At least one masking pattern must be specified. Use withMaskPattern() to add patterns.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldFailsWhenMaskPatternIsBlank(String maskPattern) {
        // Given
        var multilineMasker = Masker.multilinePattern().withMaskPattern(maskPattern);

        // When - Then
        assertThatThrownBy(() -> multilineMasker.apply("Hello World!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mask pattern cannot be blank");
    }

    @Test
    void shouldFailsWhenDuplicateDefaultMaskingPatternSpecified() {
        // Given
        var masker = Masker.multilinePattern().withMaskPattern(IP_ADDRESS_PATTERN, Masker.fixedLength());

        // When - Then
        assertThatThrownBy(() -> masker.withMaskPattern(IP_ADDRESS_PATTERN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate pattern detected: '%s' is already registered.".formatted(IP_ADDRESS_PATTERN));
    }

    @Test
    void shouldFailsWhenCustomMaskingPatternSpecified() {
        // Given
        var masker = Masker.multilinePattern().withMaskPattern(IP_ADDRESS_PATTERN);
        var fixedLengthMasker = Masker.fixedLength();

        // When - Then
        assertThatThrownBy(() -> masker.withMaskPattern(IP_ADDRESS_PATTERN, fixedLengthMasker))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate pattern detected: '%s' is already registered.".formatted(IP_ADDRESS_PATTERN));
    }

    @ParameterizedTest
    @MethodSource("mutateTestCases")
    void shouldRetainExistingSettingsWhenMutating(MultilinePatternMasker masker, String expectedText) {
        // When - Then
        assertThat(masker.apply(GIVEN_MULTILINE_TEXT)).isEqualTo(expectedText);
    }

    public static Stream<Arguments> mutateTestCases() {
        var masker = Masker.multilinePattern()
                .withSubstitution('■')
                .withMaskPattern(IP_ADDRESS_PATTERN)
                .build();
        return Stream.of(
                // 1) Retain settings with no changes: same behavior as original masker
                Arguments.of(
                        masker.mutate().build(),
                        """
				2023-05-15 INFO User john.doe@example.com logged in
				2023-05-15 INFO Processing payment with card 5431-8923-1203-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				2023-05-15 INFO IP Address: ■■■■■■■■■■■
				"""),
                // 2) Retain existing settings while adding a new default pattern (email)
                Arguments.of(
                        masker.mutate().withMaskPattern(EMAIL_PATTERN).build(),
                        """
				2023-05-15 INFO User ■■■■■■■■■■■■■■■■■■■■ logged in
				2023-05-15 INFO Processing payment with card 5431-8923-1203-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				2023-05-15 INFO IP Address: ■■■■■■■■■■■
				"""),
                // 3) Retain patterns but override substitution
                Arguments.of(
                        masker.mutate().withSubstitution('#').build(),
                        """
				2023-05-15 INFO User john.doe@example.com logged in
				2023-05-15 INFO Processing payment with card 5431-8923-1203-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				2023-05-15 INFO IP Address: ###########
				"""),
                // 4) Retain existing settings and add a custom masking strategy (credit card) plus a new default
                // pattern (email)
                Arguments.of(
                        masker.mutate()
                                .withMaskPattern(EMAIL_PATTERN)
                                .withMaskPattern(CREDIT_CARD_PATTERN, Masker.creditCard('X'))
                                .build(),
                        """
				2023-05-15 INFO User ■■■■■■■■■■■■■■■■■■■■ logged in
				2023-05-15 INFO Processing payment with card XXXX-XXXX-XXXX-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				2023-05-15 INFO IP Address: ■■■■■■■■■■■
				"""),
                // 5) Retain existing settings and add a custom masker (Base64) for email
                Arguments.of(
                        masker.mutate()
                                .withMaskPattern(EMAIL_PATTERN, Masker.base64())
                                .build(),
                        """
				2023-05-15 INFO User am9obi5kb2VAZXhhbXBsZS5jb20= logged in
				2023-05-15 INFO Processing payment with card 5431-8923-1203-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				2023-05-15 INFO IP Address: ■■■■■■■■■■■
				"""));
    }
}
