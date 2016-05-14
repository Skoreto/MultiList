package cz.uhk.fim.skoreto.todolist.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Pomocna trida pro generovani fotografie na celou obrazovku.
 * Created by Tomas.
 */
public class SinglePhotoWorkerTask extends AsyncTask<File, Void, Bitmap> {

    WeakReference<ImageView> imageViewReferences;
    final int displayWidth;
    final int displayHeight;
    private File mImageFile;

    public SinglePhotoWorkerTask(ImageView imageView, int width, int height) {
        displayWidth = width;
        displayHeight = height;
        imageViewReferences = new WeakReference<ImageView>(imageView);
    }

    /**
     * Ziskani bitmapy fotografie.
     */
    @Override
    protected Bitmap doInBackground(File... params) {
        mImageFile = params[0];
        Bitmap bitmap = BitmapHelper.decodeBitmapFromFile(mImageFile, displayWidth, displayHeight);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null && imageViewReferences != null) {
            ImageView imageView = imageViewReferences.get();
            if (imageView != null) {
                // Nastaveni cerneho pozadi, pokud fotografie nevyplnuje rozmery celou obrazovku.
                imageView.setBackgroundColor(Color.rgb(0, 0, 0));
                imageView.setImageBitmap(bitmap);
            }
        }
    }

}