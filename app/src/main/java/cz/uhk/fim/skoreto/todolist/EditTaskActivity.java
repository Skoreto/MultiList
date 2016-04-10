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
public class EditTaskActivity extends Activity {

    EditText etTaskName;
    EditText etTaskDescription;
    EditText etTaskCompleted;
    DataModel dm = new DataModel(this);
    int taskId;
    int listId;

    /**
     * Metoda pro zobrazeni predvyplneneho formulare upravy ukolu.
     */
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit);

        etTaskName = (EditText) findViewById(R.id.etTaskName);
        etTaskDescription = (EditText) findViewById(R.id.etTaskDescription);
        etTaskCompleted = (EditText) findViewById(R.id.etTaskCompleted);

        Intent anyTaskListIntent = getIntent();
        // Nastaveni listId pro filtraci ukolu v seznamu.
        // Ve vychozim pripade 1 (Inbox) - pokud IntExtra neprijde ze zadneho intentu.
        listId = anyTaskListIntent.getIntExtra("listId", 1);
        // Nastaveni prichozi taskId z TaskListActivity pro ziskani aktualne upravovaneho ukolu.
        taskId = anyTaskListIntent.getIntExtra("taskId", 1);
        Task task = dm.getTask(taskId);

        String taskState = "neurceno";
        if (task.getCompleted() == 1){
            taskState = "1";
        }
        if (task.getCompleted() == 0){
            taskState = "0";
        }

        if (task != null) {
            etTaskName.setText(task.getName());
            etTaskDescription.setText(task.getDescription());
            etTaskCompleted.setText(taskState);
        }
    }

    /**
     * Metoda pro zmenu atributu ukolu.
     */
    public void editTask(View view){
        etTaskName = (EditText) findViewById(R.id.etTaskName);
        etTaskDescription = (EditText) findViewById(R.id.etTaskDescription);
        etTaskCompleted = (EditText) findViewById(R.id.etTaskCompleted);

        // Ziskani upravovaneho ukolu.
        Task task = dm.getTask(taskId);

        // Uprava atributu ukolu dle editacnich poli.
        task.setId(taskId);
        task.setName(etTaskName.getText().toString());
        task.setDescription(etTaskDescription.getText().toString());

        if (etTaskCompleted.getText().equals("1")) {
            task.setCompleted(1);
        } else {
            task.setCompleted(0);
        }

        dm.updateTask(task);
        // Informovani uzivatele o uspesnem upraveni ukolu.
        Toast.makeText(EditTaskActivity.this, "Úkol upraven", Toast.LENGTH_SHORT).show();

        this.activateMainActivity(view, listId);
    }

    /**
     * Metoda pro smazani ukolu.
     */
    public void deleteTask(View view){
        dm.deleteTask(taskId);
        // Informovani uzivatele o uspesnem smazani ukolu.
        Toast.makeText(EditTaskActivity.this, "Úkol smazán", Toast.LENGTH_SHORT).show();

        this.activateMainActivity(view, listId);
    }

    /**
     * Metoda pro intent prechodu na MainActivity.
     */
    public void activateMainActivity(View view, int listId){
        Intent mainActivityIntent = new Intent(getApplication(), MainActivity.class);
        // Predej ID seznamu pro prechod do aktivity MainActivity.
        mainActivityIntent.putExtra("listId", listId);
        startActivity(mainActivityIntent);
    }

}
