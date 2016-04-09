package cz.uhk.fim.skoreto.todolist.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.Task;

/**
 * Created by Tomas.
 */
public class TaskAdapter extends ArrayAdapter<Task> {

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        super(context, 0, tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Task task = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_item, parent, false);
        }

        TextView tvTaskName = (TextView) convertView.findViewById(R.id.tvTaskName);
//        TextView tvTaskId = (TextView) convertView.findViewById(R.id.tvTaskId);

        tvTaskName.setText(task.getName());
//        tvTaskId.setText(task.getDescription());

        return convertView;
    }

}
