package cz.uhk.fim.skoreto.todolist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
//import android.support.v7.app.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    ArrayAdapter<String> arrayAdapter;

    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = new Database(this);
        listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, database.getAllTasks());

        listView.setAdapter(arrayAdapter);

        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.saveTask("Nejaky ukol", "Ukolem tohoto ukolu je provest nejakou cinnost.", 1, 0);

                arrayAdapter.clear();
                arrayAdapter.addAll(database.getAllTasks());
            }
        });

    }
}
