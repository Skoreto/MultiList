package cz.uhk.fim.skoreto.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.TaskList;
import cz.uhk.fim.skoreto.todolist.utils.TaskListAdapter;

/**
 * Created by Tomas.
 */
public class TaskListsActivity extends Activity {

    ListView lvTaskLists;
    ArrayAdapter<TaskList> arrayAdapter;
    DataModel dataModel;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_lists);

        dataModel = new DataModel(this);
        lvTaskLists = (ListView) findViewById(R.id.lvTaskListsList);
        arrayAdapter = new TaskListAdapter(TaskListsActivity.this, dataModel.getAllTaskLists());
        lvTaskLists.setAdapter(arrayAdapter);

        lvTaskLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Po klepnuti na polozku listu ziskej instanci zvoleneho seznamu ukolu.
                TaskList taskList = (TaskList) lvTaskLists.getItemAtPosition(position);

                Intent mainActivityIntent = new Intent(getApplication(), MainActivity.class);
                // Predej ID ukolu do intentu editTaskIntent.
                mainActivityIntent.putExtra("listId", taskList.getId());
                startActivity(mainActivityIntent);
            }
        });

    }

}
