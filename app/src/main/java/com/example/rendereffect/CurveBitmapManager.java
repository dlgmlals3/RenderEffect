package com.example.rendereffect;

import android.graphics.Bitmap;
import android.util.Log;

import java.nio.IntBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;

public class CurveBitmapManager {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 1;
    private static final int PIXEL_COUNT = WIDTH * HEIGHT;
    private static final int BUFFER_SIZE = PIXEL_COUNT + 2; // magic + crc + data
    private static final int MAGIC_CODE = 0xC0DEC0DE;
    private static final int FIXED_KEY = 20063185;
    private static final int MAX_CACHE_SIZE = 4;

    // 캐시 구조
    private static final LinkedHashMap<Integer, CurveEntry> cache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, CurveEntry> eldest) {
            if (size() > MAX_CACHE_SIZE) {
                CurveEntry entry = eldest.getValue();
                if (entry.bitmap != null && !entry.bitmap.isRecycled()) {
                    entry.bitmap.recycle();
                }
                return true;
            }
            return false;
        }
    };

    // 캐시 항목
    public static class CurveEntry {
        final int key;
        Bitmap bitmap;

        CurveEntry(int key, Bitmap bitmap) {
            this.key = key;
            this.bitmap = bitmap;
        }
    }

    // CRC32 계산
    private static int calculateCRC32(int[] data, int offset, int length) {
        CRC32 crc = new CRC32();
        ByteBuffer buffer = ByteBuffer.allocate(length * 4);
        for (int i = 0; i < length; i++) {
            buffer.putInt(data[offset + i]);
        }
        crc.update(buffer.array());
        return (int) crc.getValue();
    }

    // 픽셀 버퍼 생성
    @NonNull
    public static int[] createPixelBuffer() {
        int[] pixels = new int[BUFFER_SIZE];
        pixels[0] = MAGIC_CODE;

        for (int i = 0; i < PIXEL_COUNT; i++) {
            int gray = i;
            pixels[i + 2] = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
        }

        pixels[1] = calculateCRC32(pixels, 2, PIXEL_COUNT); // CRC32 for pixel data
        Log.d("TAG", "dlgmlals3 create crc : " + pixels[1]);
        return pixels;
    }

    // 유효성 검사
    public static boolean isValidPixelBuffer(int[] pixels) {
        if (pixels == null || pixels.length != BUFFER_SIZE) return false;
        if (pixels[0] != MAGIC_CODE) return false;

        int expectedCRC = pixels[1];
        int actualCRC = calculateCRC32(pixels, 2, PIXEL_COUNT);
        Log.d("TAG", "dlgmlals3 valid pixel buffer !!! " + actualCRC);
        return expectedCRC == actualCRC;
    }

    // 비트맵 유효성 검사
    public static boolean isValidBitmap(Bitmap bmp, int width, int height, Bitmap.Config config) {
        return bmp != null &&
                !bmp.isRecycled() &&
                bmp.getWidth() == width &&
                bmp.getHeight() == height &&
                bmp.getConfig() == config;
    }

    // 안전한 비트맵 생성 (캐시 포함)
    public static Bitmap createCurveBitmapFromBuffer(int[] pixels) {
        if (!isValidPixelBuffer(pixels)) {
            Log.d("TAG", "Valid Pixel Buffer");
            pixels = createPixelBuffer();
        }

        CurveEntry entry = cache.get(FIXED_KEY);

        if (entry == null || !isValidBitmap(entry.bitmap, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)) {
            Bitmap bmp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(IntBuffer.wrap(pixels, 2, PIXEL_COUNT));

            if (entry != null && entry.bitmap != null && !entry.bitmap.isRecycled()) {
                entry.bitmap.recycle();
            }

            entry = new CurveEntry(FIXED_KEY, bmp);
            cache.put(FIXED_KEY, entry);
            return bmp;
        }

        return entry.bitmap;
    }

    // 캐시 해제
    public static void clear() {
        for (CurveEntry entry : cache.values()) {
            if (entry.bitmap != null && !entry.bitmap.isRecycled()) {
                entry.bitmap.recycle();
            }
        }
        cache.clear();
    }
}
