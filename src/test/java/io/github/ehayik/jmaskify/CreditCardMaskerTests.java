package io.github.ehayik.jmaskify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class CreditCardMaskerTests {

    @ParameterizedTest
    @MethodSource("shouldMaskCreditCardSource")
    void shouldMaskCreditCard(String givenCreditCard, String expectedMaskedCreditCard) {
        // Given
        var masker = Masker.creditCard('X');

        // When - Then
        assertThat(masker.apply(givenCreditCard)).isEqualTo(expectedMaskedCreditCard);
    }

    private static Stream<Arguments> shouldMaskCreditCardSource() {
        return Stream.of(
                Arguments.of("4289-3874-8064-8976", "XXXX-XXXX-XXXX-8976"),
                Arguments.of("3765-742992-42008", "XXXX-XXXXXX-42008"),
                Arguments.of("6759-2834-7682-8981-725", "XXXX-XXXX-XXXX-XXXX-725"),
                Arguments.of("6706804721876067", "XXXXXXXXXXXX6067"),
                Arguments.of("12345", "XXXXXXXXXXXX2345"),
                Arguments.of(" 6706804721876067 ", "XXXXXXXXXXXX6067"),
                Arguments.of("\t6706804721876067\n", "XXXXXXXXXXXX6067"),
                Arguments.of("\u00006706804721876067", "XXXXXXXXXXXX6067"),
                Arguments.of("4000000000000000006", "XXXXXXXXXXXX0006"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", " \t\n "})
    void shouldNotMaskString(String value) {
        // Given
        var masker = Masker.creditCard();

        // When - Then
        assertThat(masker.apply(value)).isEqualTo(value);
    }

    @ParameterizedTest
    @MethodSource("shouldFullyMaskShortUndashedCreditCardSource")
    void shouldFullyMaskShortUndashedCreditCard(char substitution, String givenCreditCard) {
        // Given
        var masker = Masker.creditCard(substitution);

        // When
        var result = masker.apply(givenCreditCard);

        // Then
        assertThat(result).isEqualTo(String.valueOf(substitution).repeat(16));
    }

    private static Stream<Arguments> shouldFullyMaskShortUndashedCreditCardSource() {
        return Stream.of(Masker.DEF_SUBSTITUTION_CHAR, 'X').flatMap(substitution -> Stream.of(
                        "1", "12", "123", "1234", " 123 ", "\t12\n", "\u00001234", "1234\u0000")
                .map(givenCreditCard -> Arguments.of(substitution, givenCreditCard)));
    }
}
