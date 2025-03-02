package io.github.ehayik.jmaskify;

import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A generic delegating masker that allows the implementation of custom masking strategies.
 *
 * <p>
 * This class implements the {@link Masker} interface by delegating the masking operation
 * to a provided {@link Function}.
 *
 * @param <T> the type of the object to be masked
 */
@RequiredArgsConstructor
final class DelegateMasker<T> implements Masker<T> {

    @NonNull
    private final Function<T, String> delegate;

    @Override
    public String apply(T t) {
        return delegate.apply(t);
    }
}
