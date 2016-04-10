package cz.uhk.fim.skoreto.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskList;

/**
 * Aktivita pro zmenu a smazani ukolu.
 * Created by Tomas.
 */
public class EditTaskActivity extends Activity {

    EditText etTaskName;
    EditText etTaskDescription;
    CheckBox chbTaskCompleted;
    Spinner spinTaskLists;
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
        chbTaskCompleted = (CheckBox) findViewById(R.id.chbTaskCompleted);
        spinTaskLists = (Spinner) findViewById(R.id.spinTaskLists);

        Intent anyTaskListIntent = getIntent();
        // Nastaveni listId pro filtraci ukolu v seznamu.
        // Ve vychozim pripade 1 (Inbox) - pokud IntExtra neprijde ze zadneho intentu.
        listId = anyTaskListIntent.getIntExtra("listId", 1);
        // Nastaveni prichozi taskId z TaskListActivity pro ziskani aktualne upravovaneho ukolu.
        taskId = anyTaskListIntent.getIntExtra("taskId", 1);
        Task task = dm.getTask(taskId);

        etTaskName.setText(task.getName());
        etTaskDescription.setText(task.getDescription());

        // Zaskrtnuti checkbocu podle toho zda ukol je/neni splnen.
        if (task.getCompleted() == 1)
            chbTaskCompleted.setChecked(true);
        if (task.getCompleted() == 0)
            chbTaskCompleted.setChecked(false);

        // SPINNER seznamu ukolu
        List<TaskList> taskLists = dm.getAllTaskLists();

        // Vytvoreni instance adapteru pro spinner a pripojeni jeho dat.
        // POZOR! Zobrazeni nazvu bylo docileno pouhym prepsanim metody toString() ve tride TaskList.
        // Pro aktualni ucely nebylo nutne tvorit vlastni adapter.
        ArrayAdapter<TaskList> taskListsAdapter = new ArrayAdapter<TaskList>(this, R.layout.support_simple_spinner_dropdown_item, taskLists);
        spinTaskLists.setAdapter(taskListsAdapter);

        // Vychozi nastaveni zvoleneho seznamu.
        // TODO NEFUNGUJE
        spinTaskLists.setSelection(taskListsAdapter.getPosition(dm.getTaskListById(listId)), true);

        // Listener pro kliknuti na spinner.
        spinTaskLists.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO nejspis neni potreba
                TaskList taskList = (TaskList) spinTaskLists.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO automaticky generovana metoda
            }
        });

    }

    /**
     * Metoda pro zmenu atributu ukolu.
     */
    public void editTask(View view){
        etTaskName = (EditText) findViewById(R.id.etTaskName);
        etTaskDescription = (EditText) findViewById(R.id.etTaskDescription);
        chbTaskCompleted = (CheckBox) findViewById(R.id.chbTaskCompleted);

        // Ziskani upravovaneho ukolu.
        Task task = dm.getTask(taskId);

        // Uprava atributu ukolu dle editacnich poli.
        task.setId(taskId);
        task.setName(etTaskName.getText().toString());
        task.setDescription(etTaskDescription.getText().toString());

        if (chbTaskCompleted.isChecked())
            task.setCompleted(1);
        else
            task.setCompleted(0);

        // Ziskani vybraneho seznamu ukolu a dle nej prirazeni ukolu do prislusneho seznamu.
        TaskList taskList = (TaskList) spinTaskLists.getSelectedItem();
        task.setListId(taskList.getId());

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
