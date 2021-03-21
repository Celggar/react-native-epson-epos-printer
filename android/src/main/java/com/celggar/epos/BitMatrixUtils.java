package com.celggar.epos;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.common.BitMatrix;

// @ref https://gist.github.com/adrianoluis/fa9374d7f2f8ca1115b00cc83cd7aacd and leesiongchan/react-native-esc-pos
public class BitMatrixUtils {
    public static Bitmap convertToBitmap(BitMatrix data) {
        final int w = data.getWidth();
        final int h = data.getHeight();
        final int[] pixels = new int[w * h];

        for (int y = 0; y < h; y++) {
            final int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = data.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);

        return bitmap;
    }

    public static Bitmap convert2QrCodeToBitmap(BitMatrix data, BitMatrix data2, int shift) {
        final int w = data.getWidth();
        final int h = data.getHeight();

        final int[] pixels = new int[w * h];
        final int[] pixels2 = new int[w * h];
        for (int y = 0; y < h; y++) {
            final int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = data.get(x, y) ? Color.BLACK : Color.WHITE;
                pixels2[offset + x] = data2.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        final int[] pixels_shift = new int[shift * h];
        for (int y = 0; y < h; y++) {
            final int offset = y * shift;
            for (int x = 0; x < shift; x++) {
                pixels_shift[offset + x] = Color.WHITE;
            }
        }

        final Bitmap bitmap = Bitmap.createBitmap(w*2+shift, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        bitmap.setPixels(pixels_shift, 0, shift, w, 0, shift, h);
        bitmap.setPixels(pixels2, 0, w, w+shift, 0, w, h);

        return bitmap;
    }
}
