package cz.uhk.fim.skoreto.todolist.utils;

import android.app.Activity;
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

import java.util.List;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.Task;

/**
 * Created by Tomas.
 */
public class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskRecyclerAdapter.ViewHolder> {

    private List<Task> tasks;
    private Activity activity;

    public TaskRecyclerAdapter(Activity activity, List<Task> tasks) {
        this.tasks = tasks;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Inflatuj layout a predej ho ViewHolderu.
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.recycler_item, viewGroup, false);
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

//            viewHolder.ivPhotoThumbnail.setOnClickListener(new View.OnClickListener() {
//                   public void onClick(View v) {
//                       Intent sendPhotoDirectoryIntent = new Intent(getContext(), SinglePhotoActivity.class);
//                       sendPhotoDirectoryIntent.putExtra("photoPath", photoPath);
//                       getContext().startActivity(sendPhotoDirectoryIntent);
//                   }
//               }
//            );
        } else {
            viewHolder.ivPhotoThumbnail.setImageResource(R.drawable.ic_add_a_photo_black_48dp);
            viewHolder.ivPhotoThumbnail.setColorFilter(Color.rgb(158, 158, 158), PorterDuff.Mode.SRC_ATOP);
        }

        // Odskrtni checkboxy ukolu, podle toho, zda jsou splneny.
        if (task.getCompleted() == 0){
            viewHolder.chbTaskCompleted.setChecked(false);
        } else {
            viewHolder.chbTaskCompleted.setChecked(true);
        }


        // Zamezeni preteceni nazvu ukolu v uvodnim seznamu.
        if (task.getName().length() > 25) {
            viewHolder.tvTaskName.setText(task.getName().substring(0, 25) + " ...");
        } else {
            viewHolder.tvTaskName.setText(task.getName());
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

    /**
     * View holder pro zobrazeni jednotlivych itemu RecyclerView.
     */
    protected class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskName;
        private CheckBox chbTaskCompleted;
        private ImageView ivPhotoThumbnail;
        private View container;

        public ViewHolder(View view) {
            super(view);
            tvTaskName = (TextView) view.findViewById(R.id.tvTaskName);
            chbTaskCompleted = (CheckBox) view.findViewById(R.id.chbTaskCompleted);
            ivPhotoThumbnail = (ImageView) view.findViewById(R.id.ivPhotoThumbnail);
            container = view.findViewById(R.id.card_view);
        }
    }

}
