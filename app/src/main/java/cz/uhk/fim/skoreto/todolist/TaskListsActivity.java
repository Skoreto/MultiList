package cz.uhk.fim.skoreto.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
    EditText etTaskListName;

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
                // Predej ID seznamu pro prechod do aktivity MainActivity.
                mainActivityIntent.putExtra("listId", taskList.getId());
                startActivity(mainActivityIntent);
            }
        });

        // Pridani noveho ukolu.
        Button btnAddTaskList = (Button) findViewById(R.id.btnAddTaskList);
        btnAddTaskList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTaskListName = (EditText) findViewById(R.id.etTaskListName);

                // Pokud neni prazdny nazev noveho seznamu ukolu.
                if (!etTaskListName.getText().toString().equals("")) {
                    // Ve vychozim pripade prida novy seznam ukolu s prazdnym popisem do Inboxu jako nesplneny.
                    dataModel.addTaskList(etTaskListName.getText().toString());

                    // Vyprazdneni pole po pridani seznamu ukolu.
                    etTaskListName.setText("");
                    etTaskListName.clearFocus();
                    etTaskListName.clearComposingText();

                    // Aktualizace seznamu ukolu.
                    arrayAdapter.clear();
                    arrayAdapter.addAll(dataModel.getAllTaskLists());

                    // Informovani uzivatele o uspesnem pridani seznamu ukolu.
                    Toast.makeText(TaskListsActivity.this, "Seznam přidán", Toast.LENGTH_SHORT).show();
                } else {
                    // Informovani uzivatele o nutnosti vyplnit nazev seznamu ukolu.
                    Toast.makeText(TaskListsActivity.this, "Prázdný název seznamu!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
