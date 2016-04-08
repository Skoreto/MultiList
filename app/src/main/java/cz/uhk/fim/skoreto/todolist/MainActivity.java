package cz.uhk.fim.skoreto.todolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<Task> arrayAdapter;
    DataModel dataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataModel = new DataModel(this);
        listView = (ListView) findViewById(R.id.lvUsers);
        arrayAdapter = new TaskAdapter(MainActivity.this, dataModel.getAllTasks());
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Po klepnuti na polozku seznamu.
                Task task = (Task) listView.getItemAtPosition(position);
                String taskName = task.getName();
                Toast.makeText(MainActivity.this, "You selected : " + taskName , Toast.LENGTH_SHORT).show();

                Intent editTaskIntent = new Intent(getApplication(), EditTask.class);
                editTaskIntent.putExtra("taskId", task.getId());
                startActivity(editTaskIntent);
            }
        });

        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataModel.saveTask("Nejaky ukol", "Ukolem tohoto ukolu je provest nejakou cinnost.", 1, 0);

                arrayAdapter.clear();
                arrayAdapter.addAll(dataModel.getAllTasks());
            }
        });


    }



}
