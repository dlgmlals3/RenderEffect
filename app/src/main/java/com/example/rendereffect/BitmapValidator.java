package com.example.rendereffect;

import android.graphics.Bitmap;

public class BitmapValidator {

    public static boolean isValidBitmap(Bitmap bmp, int expectedWidth, int expectedHeight, Bitmap.Config expectedConfig) {
        if (bmp == null) {
            return false;
        }

        if (bmp.isRecycled()) {
            return false;
        }

        if (bmp.getWidth() != expectedWidth || bmp.getHeight() != expectedHeight) {
            return false;
        }

        if (bmp.getConfig() != expectedConfig) {
            return false;
        }

        return true;
    }
}