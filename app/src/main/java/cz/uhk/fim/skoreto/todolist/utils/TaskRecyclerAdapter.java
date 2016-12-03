package cz.uhk.fim.skoreto.todolist.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.SinglePhotoActivity;
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

        public ViewHolder(View view) {
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
            Bitmap photoThumbnail = ThumbnailUtils.extractThumbnail(BitmapHelper.decodeSampledBitmapFromPath(photoThumbnailPath, 90, 90), 90, 90);
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
        }

        // Zamezeni preteceni nazvu ukolu v uvodnim seznamu.
        if (task.getName().length() > 25) {
            viewHolder.tvTaskName.setText(task.getName().substring(0, 25) + " ...");
        } else {
            viewHolder.tvTaskName.setText(task.getName());
        }

        if (task.getDueDate() != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
            viewHolder.tvDueDate.setText(dateFormat.format(task.getDueDate()));

            Date currentDate = Calendar.getInstance().getTime();
            int retValue = currentDate.compareTo(task.getDueDate());
            if (retValue > 0) {
                // Datum ukolu teprve v budoucnu nastane
                viewHolder.tvDueDate.setTextColor(Color.rgb(183, 28, 28));
            } else if (retValue == 0) {
                // Datumy jsou stejne (dnes)
                viewHolder.tvDueDate.setText("Dnes");
                viewHolder.tvDueDate.setTextColor(Color.rgb(33, 150, 243));
            } else {
                // Datum ukolu je vetsi nez soucasne (uplynulo)
                viewHolder.tvDueDate.setTextColor(Color.rgb(33, 150, 243));
            }
        } else {
            viewHolder.tvDueDate.setText("nezadáno");
        }

        // Zamezeni preteceni adresy mista ukolu v seznamu.
        if (task.getTaskPlaceId() != -1) {
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



        // Nastaveni onClickListeneru pro kazdy element.
//        viewHolder.container.setOnClickListener(onClickListener(position));
    }

//    private void setDataToView(TextView name, TextView job, ImageView genderIcon, int position) {
//        name.setText(friends.get(position).getName());
//        job.setText(friends.get(position).getJob());
//        if (friends.get(position).isGender()) {
//            genderIcon.setImageResource(R.mipmap.male);
//        } else {
//            genderIcon.setImageResource(R.mipmap.female);
//        }
//    }

    @Override
    public int getItemCount() {
        return (null != tasks ? tasks.size() : 0);
    }

//    private View.OnClickListener onClickListener(final int position) {
//        return new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Dialog dialog = new Dialog(activity);
//                dialog.setContentView(R.layout.item_recycler);
//                dialog.setTitle("Position " + position);
//                dialog.setCancelable(true); // dismiss when touching outside Dialog
//
//                // set the custom dialog components - texts and image
//                TextView name = (TextView) dialog.findViewById(R.id.name);
//                TextView job = (TextView) dialog.findViewById(R.id.job);
//                ImageView icon = (ImageView) dialog.findViewById(R.id.image);
//
//                setDataToView(name, job, icon, position);
//
//                dialog.show();
//            }
//        };
//    }

}
