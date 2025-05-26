package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FixedLengthMaskerTests {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "Hello World!"})
    void shouldMaskStringToFixedLength(String text) {
        // Given
        var masker = Masker.fixedLength(4);

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
        var actualText = Masker.fixedLength().withSubstitutionChar('■').apply(text);

        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }
}
