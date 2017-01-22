package cz.uhk.fim.skoreto.todolist.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DateFormat;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskPlace;

/**
 * Fragment obecnych informaci o ukolu. Umisten v DetailFragmentPageru detailu ukolu.
 */
public class GeneralFragment extends Fragment {
    private TextView tvTaskPlace;
    private TextView tvTaskDueDate;
    private TextView tvAssignedTaskList;
    private CheckBox chbTaskCompleted;

    public static GeneralFragment newInstance(Task task, DataModel dm, Context context) {
        GeneralFragment f = new GeneralFragment();
        String taskPlaceAddress = "";
        // Pokud bylo vybrano misto ukolu, inicializuj ho
        if (task.getTaskPlaceId() != -1) {
            TaskPlace chosenTaskPlace = dm.getTaskPlace(task.getTaskPlaceId());
            taskPlaceAddress = chosenTaskPlace.getAddress();
        }

        String taskDueDate = "";
        if (task.getDueDate() != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
            taskDueDate = dateFormat.format(task.getDueDate());
        }

        Bundle args = new Bundle();
        args.putString("taskPlaceAddress", taskPlaceAddress);
        args.putString("taskDueDate", taskDueDate);
        args.putString("tvAssignedTaskListName",
                dm.getTaskListById(task.getListId()).getName());
        args.putInt("isTaskCompleted", task.getCompleted());
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_general, container, false);

        tvTaskPlace = (TextView) view.findViewById(R.id.tvTaskPlace);
        tvTaskPlace.setText(getArguments().getString("taskPlaceAddress"));
        tvTaskDueDate = (TextView) view.findViewById(R.id.tvTaskDueDate);
        tvTaskDueDate.setText(getArguments().getString("taskDueDate"));
        tvAssignedTaskList = (TextView) view.findViewById(R.id.tvAssignedTaskList);
        tvAssignedTaskList.setText(getArguments().getString("tvAssignedTaskListName"));
        chbTaskCompleted = (CheckBox) view.findViewById(R.id.chbTaskCompleted);

        // Zaskrtnuti checkboxu podle toho zda ukol je/neni splnen.
        if (getArguments().getInt("isTaskCompleted") == 1)
            chbTaskCompleted.setChecked(true);
        if (getArguments().getInt("isTaskCompleted") == 0)
            chbTaskCompleted.setChecked(false);

        return view;
    }
}
