package io.github.ehayik.jmaskify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class CreditCardMaskerTests {

    public static Stream<Arguments> shouldMaskCreditCardSource() {
        return Stream.of(
                Arguments.of("4289-3874-8064-8976", "XXXX-XXXX-XXXX-8976"),
                Arguments.of("3765-742992-42008", "XXXX-XXXXXX-42008"),
                Arguments.of("6759-2834-7682-8981-725", "XXXX-XXXX-XXXX-XXXX-725"),
                Arguments.of("6706804721876067", "XXXXXXXXXXXX6067"));
    }

    @ParameterizedTest
    @MethodSource("shouldMaskCreditCardSource")
    void shouldMaskCreditCard(String givenCreditCard, String expectedMaskedCreditCard) {
        // Given
        var masker = Masker.creditCard();

        // When - Then
        assertThat(masker.apply(givenCreditCard)).isEqualTo(expectedMaskedCreditCard);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldNotMaskString(String value) {
        // Given
        var masker = Masker.creditCard();

        // When - Then
        assertThat(masker.apply(value)).isEqualTo(value);
    }
}
