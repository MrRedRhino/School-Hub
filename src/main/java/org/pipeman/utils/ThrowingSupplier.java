package org.pipeman.utils;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
