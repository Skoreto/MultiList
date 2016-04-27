package cz.uhk.fim.skoreto.todolist.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;

/**
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
        ViewHolder holder = null;

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
                String photoDir = Environment.getExternalStorageDirectory() + "/MultiList/MultiListPhotos/" + task.getPhotoName() + ".jpg";
                Bitmap photoThumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoDir), 80, 80);
                holder.ivPhotoThumbnail.setImageBitmap(photoThumbnail);
            }

            holder.ivPhotoThumbnail.setOnClickListener(new View.OnClickListener() {
                   public void onClick(View v) {
                       Toast.makeText(getContext(), "Kliknuto na obrazek - " + task.getName(), Toast.LENGTH_SHORT).show();
                   }}
            );

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



}