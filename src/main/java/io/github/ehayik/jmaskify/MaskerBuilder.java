package io.github.ehayik.jmaskify;

import jakarta.annotation.Nullable;
import java.util.function.Function;

/**
 * Common interface for all masker builders, providing a standardized way
 * to build and apply masking operations.
 *
 * @param <T> the input type for the masker
 * @param <M> the specific masker type being built
 */
public interface MaskerBuilder<T, M extends Masker<T>> {

    /**
     * Builds and returns the masker instance.
     *
     * @return the configured masker instance
     */
    M build();

    /**
     * Applies the masking operation to the input value by building the masker first.
     *
     * @param value the input value to be masked
     * @return the masked result, or {@code null} if the input was {@code null}
     */
    @Nullable
    default String apply(@Nullable T value) {
        return build().apply(value);
    }

    /**
     * Creates a composed function that first applies this masking operation and then
     * applies the after operation.
     *
     * @param after the operation to apply after this masker
     * @return a composed function that applies operations in sequence
     */
    default Function<T, String> andThen(Masker<String> after) {
        return build().andThen(after);
    }
}
