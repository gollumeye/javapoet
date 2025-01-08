package com.squareup.javapoet;

import java.util.LinkedHashMap;
import java.util.Map;

// A makeshift multi-set implementation
public class Multiset<T> {
    private final Map<T, Integer> map = new LinkedHashMap<>();

    public void add(T t) {
        int count = map.getOrDefault(t, 0);
        map.put(t, count + 1);
    }

    public void remove(T t) {
        int count = map.getOrDefault(t, 0);
        if (count == 0) {
            throw new IllegalStateException(t + " is not in the multiset");
        }
        map.put(t, count - 1);
    }

    public boolean contains(T t) {
        return map.getOrDefault(t, 0) > 0;
    }
}
