package org.pipeman.utils;

import java.util.function.Supplier;

public class LazyInitializer<T> {
    private static final Object NO_INIT = new Object();

    @SuppressWarnings("unchecked")
    private volatile T object = (T) NO_INIT;
    private final Supplier<T> initializer;

    public LazyInitializer(Supplier<T> initializer) {
        this.initializer = initializer;
    }

    public T get() {
        T result = object;

        if (result == NO_INIT) {
            synchronized (this) {
                result = object;
                if (result == NO_INIT) {
                    object = result = initializer.get();
                }
            }
        }

        return result;
    }
}
