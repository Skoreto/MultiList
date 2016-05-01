package cz.uhk.fim.skoreto.todolist;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

import cz.uhk.fim.skoreto.todolist.utils.SinglePhotoWorkerTask;

/**
 * Aktivita pro zobrazeni fotografie na celou obrazovku.
 * Created by Tomas.
 */
public class SinglePhotoActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        ImageView imageView = new ImageView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(displayWidth, displayHeight);
        imageView.setLayoutParams(params);

        setContentView(imageView);

        File photoFile = new File(getIntent().getStringExtra("photoDir"));

        SinglePhotoWorkerTask workerTask = new SinglePhotoWorkerTask(imageView, displayWidth, displayHeight);
        workerTask.execute(photoFile);
    }

}
