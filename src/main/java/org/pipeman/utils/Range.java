package org.pipeman.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public record Range(int lower, int upper) implements Iterable<Integer> {

        public static Range of(int i1, int i2) {
            return new Range(Math.min(i1, i2), Math.max(i1, i2));
        }

        public boolean isInRange(int i) {
            return i >= lower && i <= upper;
        }

        @NotNull
        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return lower + i <= upper;
                }

                @Override
                public Integer next() {
                    return i++ + lower;
                }
            };
        }
    }