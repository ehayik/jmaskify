package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DelegateMaskerTests {

    @Test
    void shouldMaskString() {
        // Given
        var text = "Hello World!";
        var expectedText = repeat("X", text.length());
        var masker = Masker.delegate((String x) -> repeat("X", x.length()));

        // When
        var actualText = masker.apply(text);

        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }
}
