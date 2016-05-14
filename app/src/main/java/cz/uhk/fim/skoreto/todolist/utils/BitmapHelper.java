package cz.uhk.fim.skoreto.todolist.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Pomocna trida pro dekodovani bitmap.
 * Pro ucely tvorby plnych nahledu fotografii a miniatur v polozkach seznamu ukolu.
 * Created by Tomas.
 */
public class BitmapHelper {

    public BitmapHelper() {
    }

    /**
     * Prepocet rozmeru vzorku vzhledem k poradovanym rozmerum miniatury.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Vyska a sirka puvodni plne fotografie.
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // Pokud je nutne fotografii pro nahled zmensit.
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Spocita nejvetsi moznou velikost inSampleSize, ktera je nasobkem 2
            // a rozmery vzorku vetsi nez jsou pozadovane rozmery miniatury.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Vrati vzorek bitmapy z puvodni plne fotografie.
     * Prijima absolutni cestu k souboru fotografie.
     */
    public static Bitmap decodeSampledBitmapFromPath(String res, int reqWidth, int reqHeight) {
        // Nejprve dekoduj s inJustDecodeBounds=true pro overeni rozmeru.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Spocitej inSampleSize.
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Dekoduj bitmapu s nastavenou inSampleSize.
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }

    /**
     * Vrati vzorek bitmapy z puvodni plne fotografie.
     * Prijima soubor fotgrafie.
     */
    public static Bitmap decodeBitmapFromFile(File photoFile, int displayWidth, int displayHeight) {
        // Nejprve dekoduj s inJustDecodeBounds=true pro overeni rozmeru.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

        // Spocitej inSampleSize.
        options.inSampleSize = calculateInSampleSize(options, displayWidth, displayHeight);

        // Dekoduj bitmapu s nastavenou inSampleSize.
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
    }

}
