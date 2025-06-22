package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MultilinePatternMaskerTests {

    private static final Faker FAKER = new Faker();
    private static final String USERNAME_PATTERN = "username\\s*=\\s*([^\\s]+)";
    private static final String IP_ADDRESS_PATTERN = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";

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
    void shouldFailsWhenNoPatternAddedToBuilder() {
        assertThatThrownBy(() -> Masker.multilinePattern().apply("Hello World!"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("At least one mask pattern must be specified. Use withMaskPattern() to add patterns.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldFailsWhenMaskPatternIsBlank(String maskPattern) {
        assertThatThrownBy(() ->
                        Masker.multilinePattern().withMaskPattern(maskPattern).apply("Hello World!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mask pattern cannot be blank");
    }

    @Test
    void shouldMaskWithConfiguredStrategies() {
        // Given
        var text =
                """
				2023-05-15 INFO User john.doe@example.com logged in
				2023-05-15 INFO Processing payment with card 5431-8923-1203-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				""";

        var expectedText =
                """
				2023-05-15 INFO User ******************** logged in
				2023-05-15 INFO Processing payment with card XXXX-XXXX-XXXX-5467
				2023-05-15 DEBUG Session ID: aX92mLpQ7zB3
				""";

        var masker = Masker.multilinePattern()
                .withMaskPattern("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b")
                .withMaskPattern("\\b\\d{4}-\\d{4}-\\d{4}-\\d{4}\\b", Masker.creditCard('X'))
                .build();

        // When
        var actualText = masker.apply(text);

        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }
}
