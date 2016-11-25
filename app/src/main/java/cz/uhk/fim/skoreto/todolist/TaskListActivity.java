package cz.uhk.fim.skoreto.todolist;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
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
 * Aktivita prezentujici seznam ukolu.
 * Created by Tomas.
 */
public class TaskListActivity extends AppCompatActivity {

    private Toolbar tlbTaskListActivity;
    private ActionBar actionBar;
    private ListView listView;
    private ArrayAdapter<Task> arrayAdapter;
    private DataModel dataModel;
    private EditText etTaskName;
    private int listId;
    private boolean hideCompleted;
    private boolean orderAscendingDueDate;

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
                startActivityForResult(taskDetailIntent, 777);
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
                    dataModel.addTask(etTaskName.getText().toString(), "", listId, 0, "", "", dueDate, -1);

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
                View actionSortView = findViewById(R.id.action_sort);
                registerForContextMenu(actionSortView);
                openContextMenu(actionSortView);
                return true;

            case R.id.action_show_all_tasks:
                hideCompleted = false;
                refreshTasksInTaskList();
                return true;

            case R.id.action_hide_completed:
                hideCompleted = true;
                refreshTasksInTaskList();
                return true;

//            case R.id.action_settings:
//                // implementace nastavení
//                return true;

            default:
                // Vyvolani superclass pro obsluhu nerozpoznane akce.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metoda pro inicializaci layoutu Sort ContextMenu.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_context_menu, menu);
    }

    /**
     * Metoda pro obsluhu tlacitek v Sort ContextMenu.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Seradit seznam ukolu vzestupne dle data splneni.
            case R.id.sort_by_due_date_ascending:
                orderAscendingDueDate = true;
                refreshTasksInTaskList();
                return true;

            // Seradit seznam ukolu vzestupne dle data splneni.
            case R.id.sort_by_due_date_descending:
                orderAscendingDueDate = false;
                refreshTasksInTaskList();
                return true;

            // Seradit seznam ukolu dle vzdalenosti od soucasne polohy.
            case R.id.sort_by_distance:
                // TODO implementace razeni dle vzdalenosti
                return true;

            default:
                return super.onContextItemSelected(item);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Po navratu z detailu ukolu.
        if (requestCode == 777) {
            if(resultCode == Activity.RESULT_OK){
                listId = data.getIntExtra("listId", 1);

                // Refresh nastaveni nazvu aktualniho listu do hlavicky ActionBaru.
                TaskList taskList = dataModel.getTaskListById(listId);
                actionBar.setTitle(taskList.getName());
            }
            // Pokud != RESULT_OK - nedelat nic - dulezite napr. pro tlacitko zpet v dolnim panelu.
        }
    }

}
