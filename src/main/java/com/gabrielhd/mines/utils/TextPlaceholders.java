package com.gabrielhd.mines.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public class TextPlaceholders {

    private final Map<String, Supplier<String>> values = new HashMap<>();

    public TextPlaceholders() {
    }

    public <K> TextPlaceholders set(String placeholder, Supplier<K> supplier) {
        this.values.put(placeholder, () -> supplier.get().toString());
        return this;
    }

    public <K> TextPlaceholders set(String placeholder, K value) {
        return this.set(placeholder, () -> value);
    }

    public String parse(String s) {
        String key;
        for(Iterator<String> var2 = this.values.keySet().iterator(); var2.hasNext(); s = s.replace(key, (this.values.get(key)).get())) {
            key = var2.next();
        }

        return s;
    }
}