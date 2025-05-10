package com.example.rendereffect;

import android.graphics.Bitmap;

import java.util.LinkedHashMap;
import java.util.Map;

public class CurveCache {

    private static final int MAX_SIZE = 32;

    public static class CurveEntry {
        final int key;
        Bitmap bitmap;

        CurveEntry(int key, Bitmap bitmap) {
            this.key = key;
            this.bitmap = bitmap;
        }
    }

    private final LinkedHashMap<Integer, CurveEntry> cache = new LinkedHashMap<>(MAX_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, CurveEntry> eldest) {
            return size() > MAX_SIZE;
        }
    };

    public CurveEntry get(int key) {
        return cache.get(key);
    }

    public void put(int key, CurveEntry entry) {
        cache.put(key, entry);
    }
}
