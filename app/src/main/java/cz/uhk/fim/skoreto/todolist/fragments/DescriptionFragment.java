package cz.uhk.fim.skoreto.todolist.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.model.Task;

/**
 * Fragment blizsiho popisu ukolu. Umisten v DetailFragmentPageru detailu ukolu.
 */
public class DescriptionFragment extends Fragment {
    private TextView tvTaskDescription;

    public static DescriptionFragment newInstance(Task task) {
        DescriptionFragment f = new DescriptionFragment();
        Bundle args = new Bundle();
        args.putString("taskDescription", task.getDescription());
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_description, container, false);
        tvTaskDescription = (TextView) view.findViewById(R.id.tvTaskDescription);
        tvTaskDescription.setText(getArguments().getString("taskDescription"));
        return view;
    }
}
