package cz.uhk.fim.skoreto.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;

/**
 * Aktivita pro zmenu a smazani ukolu.
 * Created by Tomas.
 */
public class EditTask extends Activity {

    EditText etTaskName;
    EditText etTaskDescription;
    DataModel dm = new DataModel(this);

    /**
     * Metoda pro zobrazeni predvyplneneho formulare upravy ukolu.
     */
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit);

        etTaskName = (EditText) findViewById(R.id.etTaskName);
        etTaskDescription = (EditText) findViewById(R.id.etTaskDescription);

        Intent ii = getIntent();
        int taskId = ii.getIntExtra("taskId", 1);
        Task task = dm.getTask(taskId);

        if (task != null) {
            etTaskName.setText(task.getName());
            etTaskDescription.setText(task.getDescription());
        }
    }

    /**
     * Metoda pro zmenu atributu ukolu.
     */
    public void editTask(View view){
        etTaskName = (EditText) findViewById(R.id.etTaskName);
        etTaskDescription = (EditText) findViewById(R.id.etTaskDescription);

        // Ziskani puvodniho ukolu.
        Intent ii = getIntent();
        int taskId = ii.getIntExtra("taskId", 1);
        Task task = dm.getTask(taskId);

        // Uprava atributu ukolu dle editacnich poli.
        task.setId(taskId);
        task.setName(etTaskName.getText().toString());
        task.setDescription(etTaskDescription.getText().toString());

        dm.updateTask(task);
        // Informovani uzivatele o uspesnem upraveni ukolu.
        Toast.makeText(EditTask.this, "Úkol upraven", Toast.LENGTH_SHORT).show();

        this.activateMainActivity(view);
    }

    /**
     * Metoda pro smazani ukolu.
     */
    public void deleteTask(View view){
        Intent ii = getIntent();
        int taskId = ii.getIntExtra("taskId", 1);

        dm.deleteTask(taskId);
        // Informovani uzivatele o uspesnem smazani ukolu.
        Toast.makeText(EditTask.this, "Úkol smazán", Toast.LENGTH_SHORT).show();

        this.activateMainActivity(view);
    }

    public void activateMainActivity(View view){
        Intent ii = new Intent(getApplication(), MainActivity.class);
        startActivity(ii);
    }

}
