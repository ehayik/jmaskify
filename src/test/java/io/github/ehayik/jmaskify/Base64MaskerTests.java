package io.github.ehayik.jmaskify;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Base64MaskerTests {

    private final Masker<String> maskerUnderTest = Masker.base64();

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "Hello World!"})
    void shouldMaskStringToBase64(String text) {
        // When
        var actualText = maskerUnderTest.apply(text);

        // Then
        assertThat(actualText).isBase64();
    }

    @Test
    void shouldNotMaskStringWhenNull() {
        assertThat(maskerUnderTest.apply(null)).isNull();
    }
}
