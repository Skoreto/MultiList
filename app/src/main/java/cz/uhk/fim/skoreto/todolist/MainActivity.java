package cz.uhk.fim.skoreto.todolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<Task> arrayAdapter;
    DataModel dataModel;
    EditText etTaskName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataModel = new DataModel(this);
        listView = (ListView) findViewById(R.id.lvTasksList);
        arrayAdapter = new TaskAdapter(MainActivity.this, dataModel.getAllTasks());
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Po klepnuti na polozku seznamu ziskej instanci zvoleneho ukolu.
                Task task = (Task) listView.getItemAtPosition(position);
                Intent editTaskIntent = new Intent(getApplication(), EditTask.class);
                // Predej ID ukolu do intentu editTaskIntent.
                editTaskIntent.putExtra("taskId", task.getId());
                startActivity(editTaskIntent);
            }
        });

        // Pridani noveho ukolu.
        Button btnAddTask = (Button) findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTaskName = (EditText) findViewById(R.id.etTaskName);

                // Pokud neni prazdny nazev noveho ukolu.
                if (!etTaskName.getText().toString().equals("")){
                    dataModel.saveTask(etTaskName.getText().toString(), "", 1, 0);

                    // Vyprazdneni pole po pridani ukolu.
                    etTaskName.setText("");
                    etTaskName.clearFocus();
                    etTaskName.clearComposingText();

                    // Aktualizace seznamu ukolu.
                    arrayAdapter.clear();
                    arrayAdapter.addAll(dataModel.getAllTasks());

                    // Informovani uzivatele o uspesnem pridani ukolu.
                    Toast.makeText(MainActivity.this, "Úkol přidán" , Toast.LENGTH_SHORT).show();
                } else {
                    // Informovani uzivatele o nutnosti vyplnit název úkolu.
                    Toast.makeText(MainActivity.this, "Prázdný název úkolu!" , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
