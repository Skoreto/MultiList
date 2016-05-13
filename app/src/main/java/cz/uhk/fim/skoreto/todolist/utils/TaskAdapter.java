package cz.uhk.fim.skoreto.todolist.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.SinglePhotoActivity;
import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;

/**
 * Vlastni ArrayAdapter pro definici aplikacni logiky polozky v seznamu ukolu.
 * Created by Tomas.
 */
public class TaskAdapter extends ArrayAdapter<Task> {

    DataModel dm = new DataModel(getContext());

    private class ViewHolder {
        TextView tvTaskName;
        CheckBox chbTaskCompleted;
        ImageView ivPhotoThumbnail;
    }

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        super(context, 0, tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Ziskej data pro ukol z teto pozice.
        final Task task = getItem(position);

        // Over, zda se znovupouziva existujici view, jinak inflatuj toto view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_list_item, parent, false);

            holder = new ViewHolder();
            holder.tvTaskName = (TextView) convertView.findViewById(R.id.tvTaskName);
            holder.chbTaskCompleted = (CheckBox) convertView.findViewById(R.id.chbTaskCompleted);
            holder.ivPhotoThumbnail = (ImageView) convertView.findViewById(R.id.ivPhotoThumbnail);

            // Prirazeni nahledu fotografie k ukolu.
            if (!task.getPhotoName().equals("")) {
                String photoThumbnailPath = Environment.getExternalStorageDirectory() + "/MultiList/PhotoThumbnails/" + "THUMBNAIL_" + task.getPhotoName() + ".jpg";
                final String photoPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos/" + task.getPhotoName() + ".jpg";

                // Optimalizace dekodovani a nacteni miniatury z nahledu v externim ulozisti do pameti.
                Bitmap photoThumbnail = ThumbnailUtils.extractThumbnail(decodeSampledBitmapFromFile(photoThumbnailPath, 90, 90), 90, 90);
                holder.ivPhotoThumbnail.setImageBitmap(photoThumbnail);

                holder.ivPhotoThumbnail.setOnClickListener(new View.OnClickListener() {
                       public void onClick(View v) {
                           Intent sendPhotoDirectoryIntent = new Intent(getContext(), SinglePhotoActivity.class);
                           sendPhotoDirectoryIntent.putExtra("photoPath", photoPath);
                           getContext().startActivity(sendPhotoDirectoryIntent);
                       }
                   }
                );
            } else {
                holder.ivPhotoThumbnail.setImageResource(R.drawable.ic_add_a_photo_black_48dp);
                holder.ivPhotoThumbnail.setColorFilter(Color.rgb(158, 158, 158), PorterDuff.Mode.SRC_ATOP);
            }

            // Odskrtni checkboxy ukolu, podle toho, zda jsou splneny.
            if (task.getCompleted() == 0){
                holder.chbTaskCompleted.setChecked(false);
            } else {
                holder.chbTaskCompleted.setChecked(true);
            }

            convertView.setTag(holder);

            holder.chbTaskCompleted.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox chb = (CheckBox) v;

                    // Po kliknuti na checkbox zjisti jeho stav a dle toho prenastav splneni ukolu.
                    if (chb.isChecked()) {
                        task.setCompleted(1);
                    } else {
                        task.setCompleted(0);
                    }

                    // Aktualizuj ukol v databazi.
                    dm.updateTask(task);

                    // Informuj uzivatele o provedene zmene stavu ukolu.
                    String taskState = "neurcen";
                    if (task.getCompleted() == 0)
                        taskState = "nesplněn";
                    if (task.getCompleted() == 1)
                        taskState = "splněn";
                    Toast.makeText(getContext(), "Úkol " + task.getName() + " je " + taskState, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Zamezeni preteceni nazvu ukolu v uvodnim seznamu.
        if (task.getName().length() > 25) {
            holder.tvTaskName.setText(task.getName().substring(0, 25) + " ...");
        } else {
            holder.tvTaskName.setText(task.getName());
        }

        return convertView;
    }

    /**
     * Prepocet rozmeru vzorku vzhledem k poradovanym rozmerum miniatury.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Vyska a sirka puvodni plne fotografie.
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 2;

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
     */
    public static Bitmap decodeSampledBitmapFromFile(String res, int reqWidth, int reqHeight) {
        // Nejprve dekoduj s inJustDEcodeBounds=true pro overeni rozmeru.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Spocitej inSampleSize.
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Dekoduj bitmapu s nastavenou inSampleSize.
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }


}
