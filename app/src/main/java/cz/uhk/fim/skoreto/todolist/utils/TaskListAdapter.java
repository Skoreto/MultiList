package cz.uhk.fim.skoreto.todolist.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.TaskList;

/**
 * Created by Tomas.
 */
public class TaskListAdapter extends ArrayAdapter<TaskList> {

    DataModel dm = new DataModel(getContext());

    private class ViewHolder {
        TextView tvTaskListName;
    }

    public TaskListAdapter(Context context, ArrayList<TaskList> taskLists) {
        super(context, 0, taskLists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        // Ziskej data pro seznam ukolu z teto pozice.
        final TaskList taskList = getItem(position);

        // Over, zda se znovupouziva existujici view, jinak inflatuj toto view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_lists_item, parent, false);

            holder = new ViewHolder();
            holder.tvTaskListName = (TextView) convertView.findViewById(R.id.tvTaskListName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Zamezeni preteceni nazvu seznamu ukolu v seznamu seznamu ukolu.
        if (taskList.getName().length() > 40) {
            holder.tvTaskListName.setText(taskList.getName().substring(0, 40) + " ...");
        } else {
            holder.tvTaskListName.setText(taskList.getName());
        }

        return convertView;
    }

}
