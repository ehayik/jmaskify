package io.github.ehayik.jmaskify;

/**
 * Capable of produce a {@link MaskerBuilder builder} for creating variations of itself.
 *
 * @param <T> the input type for the masker
 * @param <M> the specific masker type being built
 */
public interface Buildable<T, M extends Masker<T>> {

    /**
     * @return a {@link MaskerBuilder builder} to create a new {@code Masker} whose settings are replicated from the current {@code Masker}.
     */
    MaskerBuilder<T, M> mutate();
}
