package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class FixedLengthMaskerTests {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "Hello World!"})
    void shouldMaskStringToFixedLength(String text) {
        // Given
        var masker = Masker.fixedLength().withFixedLength(4);

        // When
        var actualText = masker.apply(text);

        // Then
        assertThat(actualText).isEqualTo("****");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "Hello World!"})
    void shouldMaskStringToInputLength(String text) {
        // Given
        var expectedText = repeat("*", text.length());
        var masker = Masker.fixedLength();

        // When
        var actualText = masker.apply(text);

        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }

    @Test
    void shouldNotMaskStringWhenNull() {
        // Given
        var masker = Masker.fixedLength();

        // When - Then
        assertThat(masker.apply(null)).isNull();
    }

    @Test
    void shouldMaskStringWithCustomSubstitution() {
        // Given
        var text = new Faker().finance().iban();
        var expectedText = repeat("■", text.length());

        // When
        var actualText = Masker.fixedLength().withSubstitution('■').apply(text);

        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }

    @ParameterizedTest
    @MethodSource("preservationTestCases")
    void shouldMaskStringWithSectionsPreservation(Masker<String> masker, String expectedText) {
        // Given
        var text = "123-45-6789";

        // When
        var actualText = masker.apply(text);

        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }

    private static Stream<Arguments> preservationTestCases() {
        return Stream.of(
                // pre-configured masker, expectedText
                // Preserving suffix sections with fixed length
                Arguments.of(
                        Masker.fixedLength()
                                .withFixedLength(4)
                                .preservePrefix(2)
                                .preserveSuffix(3)
                                .build(),
                        "12****789"),
                // Preserving suffix and prefix with default length
                Arguments.of(
                        Masker.fixedLength().preservePrefix(2).preserveSuffix(3).build(), "12******789"),
                // Preserving suffix with default length
                Arguments.of(Masker.fixedLength().preserveSuffix(3).build(), "********789"),

                // Preserving suffix with fixed length
                Arguments.of(
                        Masker.fixedLength()
                                .preserveSuffix(3)
                                .withFixedLength(4)
                                .build(),
                        "****789"),
                // Preserving prefix with default length
                Arguments.of(Masker.fixedLength().preservePrefix(2).build(), "12*********"),
                // Preserving prefix with fixed length
                Arguments.of(                        Masker.fixedLength()
                                .preservePrefix(2)
                                .withFixedLength(4)
                                .build(),
                        "12****"));
    }
}
