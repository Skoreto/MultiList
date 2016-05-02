package cz.uhk.fim.skoreto.todolist;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskList;
import cz.uhk.fim.skoreto.todolist.utils.TaskAdapter;

/**
 * Trida aktivity prezentujici seznam ukolu.
 */
public class TaskListActivity extends AppCompatActivity {

    Toolbar tlbTaskListActivity;
    ActionBar actionBar;
    ListView listView;
    ArrayAdapter<Task> arrayAdapter;
    DataModel dataModel;
    EditText etTaskName;
    int listId;
    boolean hideCompleted;
    boolean orderAscendingDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list_activity);

        dataModel = new DataModel(this);

        // Implementace ActionBaru.
        tlbTaskListActivity = (Toolbar) findViewById(R.id.tlbTaskListActivity);
        if (tlbTaskListActivity != null) {
            setSupportActionBar(tlbTaskListActivity);

            // Ziskani podpory ActionBaru korespondujiciho s Toolbarem.
            actionBar = getSupportActionBar();

            // Povoleni tlacitka Zpet.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        listView = (ListView) findViewById(R.id.lvTasksList);

        Intent anyIntent = getIntent();
        // Nastaveni listId pro filtraci ukolu v seznamu.
        // Ve vychozim pripade 1 (Inbox) - pokud IntExtra neprijde ze zadneho intentu.
        listId = anyIntent.getIntExtra("listId", 1);

        // Nastaveni nazvu aktualniho listu do hlavicky ActionBaru.
        TaskList taskList = dataModel.getTaskListById(listId);
        actionBar.setTitle(taskList.getName());

        orderAscendingDueDate = true;
        // Zobrazit vsechny / pouze splnene ukoly.
        hideCompleted = false;
        if (!hideCompleted) {
            arrayAdapter = new TaskAdapter(TaskListActivity.this, dataModel.getTasksByListId(listId, orderAscendingDueDate));
        } else {
            arrayAdapter = new TaskAdapter(TaskListActivity.this, dataModel.getIncompletedTasksByListId(listId, orderAscendingDueDate));
        }

        listView.setAdapter(arrayAdapter);

        // Editace ukolu po klepnuti v seznamu ukolu.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Po klepnuti na polozku seznamu ziskej instanci zvoleneho ukolu.
                Task task = (Task) listView.getItemAtPosition(position);
                Intent taskDetailIntent = new Intent(TaskListActivity.this, TaskDetailActivity.class);
                // Predej ID ukolu do intentu editTaskIntent.
                taskDetailIntent.putExtra("taskId", task.getId());
                // Predej ID seznamu pro prechod do aktivity TaskDetailActivity.
                taskDetailIntent.putExtra("listId", listId);
//                taskDetailIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(taskDetailIntent);
            }
        });

        // Pridani noveho ukolu.
        FloatingActionButton btnAddTask = (FloatingActionButton) findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTaskName = (EditText) findViewById(R.id.etTaskName);

                // Pokud neni prazdny nazev noveho ukolu.
                if (!etTaskName.getText().toString().equals("")){
                    // Ziskani aktualniho casu a vytvoreni instance datumu.
                    Calendar calendar = Calendar.getInstance();
                    Date dueDate = calendar.getTime();

                    // Ve vychozim pripade pridej novy ukol s prazdnym popisem do Inboxu jako nesplneny a s datumem splneni do dnes.
                    dataModel.addTask(etTaskName.getText().toString(), "", listId, 0, "", "", dueDate);

                    // Vyprazdneni pole po pridani ukolu.
                    etTaskName.setText("");
                    etTaskName.clearFocus();
                    etTaskName.clearComposingText();

                    // Aktualizace seznamu ukolu.
                    refreshTasksInTaskList();

                    // Informovani uzivatele o uspesnem pridani ukolu.
                    Toast.makeText(TaskListActivity.this, "Úkol přidán", Toast.LENGTH_SHORT).show();
                } else {
                    // Informovani uzivatele o nutnosti vyplnit název úkolu.
                    Toast.makeText(TaskListActivity.this, "Prázdný název úkolu!" , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * Metoda pro inicializaci layoutu ActionBaru.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_list_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Metoda pro obluhu tlacitek v ActionBaru.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                // Razeni dle data splneni vzestupne/sestupne.
                if (orderAscendingDueDate == true) {
                    orderAscendingDueDate = false;
                } else {
                    orderAscendingDueDate = true;
                }

                refreshTasksInTaskList();
                return true;

            case R.id.action_show_all_tasks:
                hideCompleted = false;
                refreshTasksInTaskList();
                return true;

            case R.id.action_hide_completed:
                hideCompleted = true;
                refreshTasksInTaskList();
                return true;

            case R.id.action_settings:
                // implementace nastavení
                return true;

            default:
                // Vyvolani superclass pro obsluhu nerozpoznane akce.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTasksInTaskList();
    }

    /**
     * Metoda pro aktualizaci seznamu ukolu dle nastavenych parametru.
     */
    public void refreshTasksInTaskList() {
        // Aktualizace seznamu ukolu.
        arrayAdapter.clear();

        if (!hideCompleted) {
            arrayAdapter.addAll(dataModel.getTasksByListId(listId, orderAscendingDueDate));
        } else {
            arrayAdapter.addAll(dataModel.getIncompletedTasksByListId(listId, orderAscendingDueDate));
        }

        listView.setAdapter(arrayAdapter);
    }


}
