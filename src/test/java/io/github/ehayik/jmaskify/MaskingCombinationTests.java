package io.github.ehayik.jmaskify;

import static io.github.ehayik.jmaskify.Masker.base64;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MaskingCombinationTests {

    @Test
    void shouldApplyMaskingStrategiesSequence() {
        // Given
        var json = """
		{
		"name": "John Doe",
		"logs": [
			"2023-05-15 INFO IP Address: 192.168.1.1"
		]
		}
""";

        var jsonMasker = Masker.json()
                .withProperty("name", Masker.delegate(value -> "■■■■■■"))
                .build();

        var multilineMasker = Masker.multilinePattern()
                .withMaskPattern("(\\d+\\.\\d+\\.\\d+\\.\\d+)")
                .build();

        var compositeMasker = jsonMasker.andThen(multilineMasker).andThen(base64());

        // When - Then
        assertThat(compositeMasker.apply(json))
                .isEqualTo(
                        "eyJuYW1lIjoi4pag4pag4pag4pag4pag4pagIiwibG9ncyI6WyIyMDIzLTA1LTE1IElORk8gSVAgQWRkcmVzczogKioqKioqKioqKioiXX0=");
    }
}
