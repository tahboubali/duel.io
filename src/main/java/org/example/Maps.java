package org.example;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Maps {
    private Maps() {
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> of(Object... entries) {
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("Map entries must be key/value pairs.");
        }

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (int i = 0; i < entries.length; i += 2) {
            result.put((K) entries[i], (V) entries[i + 1]);
        }
        return result;
    }
}
