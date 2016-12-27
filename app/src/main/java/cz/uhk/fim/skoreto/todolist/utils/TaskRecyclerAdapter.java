package cz.uhk.fim.skoreto.todolist.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.SinglePhotoActivity;
import cz.uhk.fim.skoreto.todolist.TaskDetailActivity;
import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskPlace;

/**
 * Created by Tomas.
 */
public class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskRecyclerAdapter.ViewHolder> {

    private List<Task> tasks;
    private Context context;
    private DataModel dm;

    public TaskRecyclerAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * View holder pro zobrazeni jednotlivych itemu RecyclerView.
     */
    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhotoThumbnail;
        private TextView tvTaskName;
        private TextView tvDueDate;
        private TextView tvTaskPlace;
        private CheckBox chbTaskCompleted;
        private View container;

        protected ViewHolder(View view) {
            super(view);
            ivPhotoThumbnail = (ImageView) view.findViewById(R.id.ivPhotoThumbnail);
            tvTaskName = (TextView) view.findViewById(R.id.tvTaskName);
            tvDueDate = (TextView) view.findViewById(R.id.tvDueDate);
            tvTaskPlace = (TextView) view.findViewById(R.id.tvTaskPlace);
            chbTaskCompleted = (CheckBox) view.findViewById(R.id.chbTaskCompleted);
            container = view.findViewById(R.id.card_view);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        context = viewGroup.getContext();
        dm = new DataModel(context);

        // Inflatuj layout a predej ho ViewHolderu.
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TaskRecyclerAdapter.ViewHolder viewHolder, int position) {
        // Ziskej data pro ukol z teto pozice.
        final Task task = tasks.get(position);

        // Prirazeni nahledu fotografie k ukolu.
        if (!task.getPhotoName().equals("")) {
            String photoThumbnailPath = Environment.getExternalStorageDirectory() + "/MultiList/PhotoThumbnails/" + "THUMBNAIL_" + task.getPhotoName() + ".jpg";
            final String photoPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos/" + task.getPhotoName() + ".jpg";

            // Optimalizace dekodovani a nacteni miniatury z nahledu v externim ulozisti do pameti.
            Bitmap photoThumbnail = ThumbnailUtils.extractThumbnail(BitmapHelper.decodeSampledBitmapFromPath(photoThumbnailPath, 100, 100), 100, 100);
            viewHolder.ivPhotoThumbnail.setImageBitmap(photoThumbnail);

            viewHolder.ivPhotoThumbnail.setOnClickListener(new View.OnClickListener() {
                   public void onClick(View v) {
                       Intent sendPhotoDirectoryIntent = new Intent(context, SinglePhotoActivity.class);
                       sendPhotoDirectoryIntent.putExtra("photoPath", photoPath);
                       context.startActivity(sendPhotoDirectoryIntent);
                   }
               }
            );
        } else {
            viewHolder.ivPhotoThumbnail.setImageResource(R.drawable.ic_add_a_photo_black_48dp);
            viewHolder.ivPhotoThumbnail.setColorFilter(Color.rgb(158, 158, 158), PorterDuff.Mode.SRC_ATOP);
            viewHolder.ivPhotoThumbnail.setPadding(3, 0, 0, 0);
        }

        // Zamezeni preteceni nazvu ukolu v uvodnim seznamu.
        if (task.getName().length() > 30) {
            viewHolder.tvTaskName.setText(task.getName().substring(0, 30) + " ...");
        } else {
            viewHolder.tvTaskName.setText(task.getName());
        }

        // DATUM SPLNENI
        if (task.getDueDate() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E d.M.");
            String dueDate = simpleDateFormat.format(task.getDueDate());
            viewHolder.tvDueDate.setText(dueDate);

            // Obarveni ikon
            viewHolder.tvDueDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_black_18dp, 0, 0, 0);
            for (Drawable drawable : viewHolder.tvDueDate.getCompoundDrawables()) {
                if (drawable != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(Color.rgb(97, 97, 97), PorterDuff.Mode.SRC_IN));
                }
            }

            Calendar calendar = Calendar.getInstance();
            Calendar calTaskDueDate = Calendar.getInstance();
            calTaskDueDate.setTime(task.getDueDate());

            // Priprav ciste datumy bez casu pro ucely porovnani
            Date currentDate = new Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            Date taskDueDate = new Date(calTaskDueDate.get(Calendar.YEAR), calTaskDueDate.get(Calendar.MONTH) + 1, calTaskDueDate.get(Calendar.DAY_OF_MONTH));

            int retValue = currentDate.compareTo(taskDueDate);
            if (retValue > 0) {
                // Datum ukolu je vetsi nez soucasne (uplynulo)
                viewHolder.tvDueDate.setTextColor(Color.rgb(183, 28, 28));
            } else if (retValue == 0) {
                // Datumy jsou stejne (dnes)
                viewHolder.tvDueDate.setText("Dnes");
                viewHolder.tvDueDate.setTextColor(Color.rgb(33, 150, 243));
            } else {
                // Datum ukolu teprve v budoucnu nastane
                // Pridej 1 den (pro ziskani zitrejsiho datumu)
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date tomorrowDate = new Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                if (tomorrowDate.compareTo(taskDueDate) == 0)
                    viewHolder.tvDueDate.setText("Zítra");

                viewHolder.tvDueDate.setTextColor(Color.rgb(33, 150, 243));
            }
        } else {
            viewHolder.tvDueDate.setText("nezadáno");
        }

        // MISTO UKOLU
        if (task.getTaskPlaceId() != -1) {
            // Obarveni ikon
            viewHolder.tvTaskPlace.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_room_black_18dp, 0, 0, 0);
            for (Drawable drawable : viewHolder.tvTaskPlace.getCompoundDrawables()) {
                if (drawable != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(Color.rgb(97, 97, 97), PorterDuff.Mode.SRC_IN));
                }
            }
            // Zamezeni preteceni adresy mista ukolu v seznamu.
            TaskPlace taskPlace = dm.getTaskPlace(task.getTaskPlaceId());
            if (taskPlace.getAddress().length() > 31) {
                viewHolder.tvTaskPlace.setText(taskPlace.getAddress().substring(0, 31) + " ...");
            } else {
                viewHolder.tvTaskPlace.setText(taskPlace.getAddress());
            }
        } else {
            viewHolder.tvTaskPlace.setText("nezadáno");
        }

        // Odskrtni checkboxy ukolu, podle toho, zda jsou splneny.
        if (task.getCompleted() == 0){
            viewHolder.chbTaskCompleted.setChecked(false);
        } else {
            viewHolder.chbTaskCompleted.setChecked(true);
        }

        viewHolder.chbTaskCompleted.setOnClickListener(new View.OnClickListener() {
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
                Toast.makeText(context, "Úkol " + task.getName() + " je " + taskState, Toast.LENGTH_SHORT).show();
            }
        });

        // Nastaveni onClickListeneru pro kazdy element.
       viewHolder.container.setOnClickListener(onClickListener(position));
    }

    // Editace ukolu po klepnuti v seznamu ukolu.
    private View.OnClickListener onClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Po klepnuti na polozku seznamu ziskej instanci zvoleneho ukolu.
                Task task = (Task) tasks.get(position);
                Intent taskDetailIntent = new Intent(context, TaskDetailActivity.class);
                // Predej ID ukolu do intentu editTaskIntent.
                taskDetailIntent.putExtra("taskId", task.getId());
                // Predej ID seznamu pro prechod do aktivity TaskEditActivity.
                taskDetailIntent.putExtra("listId", task.getListId());
                ((Activity) context).startActivityForResult(taskDetailIntent, 121);
            }
        };
    }

    @Override
    public int getItemCount() {
        return (null != tasks ? tasks.size() : 0);
    }

}
