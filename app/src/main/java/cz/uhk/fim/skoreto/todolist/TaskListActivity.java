package cz.uhk.fim.skoreto.todolist;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskList;
import cz.uhk.fim.skoreto.todolist.model.TaskPlace;
import cz.uhk.fim.skoreto.todolist.utils.TaskRecyclerAdapter;

/**
 * Aktivita prezentujici seznam ukolu.
 * Created by Tomas.
 */
public class TaskListActivity extends AppCompatActivity {

    private Toolbar tlbTaskListActivity;
    private ActionBar actionBar;
    private RecyclerView rvTaskList;
    private TaskRecyclerAdapter taskRecyclerAdapter;
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

        rvTaskList = (RecyclerView) findViewById(R.id.rvTaskList);
        rvTaskList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTaskList.setLayoutManager(layoutManager);

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
            taskRecyclerAdapter = new TaskRecyclerAdapter(dataModel.getTasksByListId(listId, orderAscendingDueDate));
        } else {
            taskRecyclerAdapter = new TaskRecyclerAdapter(dataModel.getIncompletedTasksByListId(listId, orderAscendingDueDate));
        }
        rvTaskList.setAdapter(taskRecyclerAdapter);

        // Pridani noveho ukolu.
        FloatingActionButton btnAddTask = (FloatingActionButton) findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTaskName = (EditText) findViewById(R.id.etTaskName);

                // Pokud neni prazdny nazev noveho ukolu.
                if (!etTaskName.getText().toString().equals("")) {
                    // Ziskani aktualniho casu a vytvoreni instance datumu.
                    Calendar calendar = Calendar.getInstance();
                    Date dueDate = calendar.getTime();

                    // Ve vychozim pripade pridej novy ukol s prazdnym popisem do Inboxu jako nesplneny a s datumem splneni do dnes.
                    dataModel.addTask(etTaskName.getText().toString(), "", listId, 0, "", "", null, -1);

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
                    Toast.makeText(TaskListActivity.this, "Prázdný název úkolu!", Toast.LENGTH_SHORT).show();
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

            case R.id.action_task_places_map:
                Intent taskPlacesMapActivityIntent = new Intent(getApplication(), TaskPlacesMapActivity.class);
                // Predej ID seznamu pro prechod do aktivity TaskPlacesMapActivity.
                taskPlacesMapActivityIntent.putExtra("listId", listId);
                startActivity(taskPlacesMapActivityIntent);
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
        menu.setHeaderTitle("Možnosti řazení");
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

            // Seradit seznam ukolu dle data splneni a pote dle vzdalenosti od soucasne polohy.
            case R.id.sort_by_due_date_then_by_distance:
                // Kontrola permission k lokalizaci
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                // Zjisti soucasnou pozici
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                Location currentLocation = locationManager.getLastKnownLocation(provider);

                List<Task> tasksSortedAscDueDate = dataModel.getTasksByListId(listId, orderAscendingDueDate);
                List<Task> tasksWithoutDueDate = new ArrayList<Task>();

                // Vytrid vsechny ukoly bez zadaneho datumu splneni
                for (int i = 0; i < tasksSortedAscDueDate.size(); i++) {
                    Task currentTask = tasksSortedAscDueDate.get(i);
                    if (currentTask.getDueDate() == null) {
                        tasksWithoutDueDate.add(currentTask);
                        tasksSortedAscDueDate.remove(currentTask);
                        i--;
                    }
                }

                // Vychazime ze seznamu ukolu serazenych dle datumu splneni
                // Ukoly se stejnym datumem budou setrideny do samostatneho seznamu
                List<List<Task>> listEqualDatesLists = new ArrayList<List<Task>>();
                for (int i = 0; i < tasksSortedAscDueDate.size(); i++) {
                    Task currentTask = tasksSortedAscDueDate.get(i);

                    // Zaloz seznam pro datum splneni zkoumaneho ukolu a ukol do nej pridej
                    Date comparedDate = currentTask.getDueDate();
                    List<Task> newListOfEqualDates = new ArrayList<Task>();
                    newListOfEqualDates.add(currentTask);
                    int equalDatesCounter = 0;

                    // Ochrana preskoceni rozsahu - Skonci, pokud index ukolu,
                    // na ktery se bude prechazet, je vyssi nez index posledniho ukolu v seznamu
                    if (i + equalDatesCounter + 1 > tasksSortedAscDueDate.size() - 1)
                        break;

                    // Porovnej datum ukolu s datumem nasledujiciho ukolu
                    // Jsou-li shodne, pridej ho do seznamu ukolu se stejnymi datumy
                    // Zvys pocitadlo pridanych ukolu se stejnym datumem a zkoumej nasledujici ukol
                    while (comparedDate.compareTo(tasksSortedAscDueDate.get(i + equalDatesCounter + 1).getDueDate()) == 0) {
                        newListOfEqualDates.add(tasksSortedAscDueDate.get(i + equalDatesCounter + 1));
                        equalDatesCounter++;

                        // Ochrana preskoceni rozsahu
                        if (i + equalDatesCounter + 1 > tasksSortedAscDueDate.size() - 1)
                            break;
                    }
                    // Pridej seznam ukolu se stejnym datumem do souhrnneho seznamu
                    listEqualDatesLists.add(newListOfEqualDates);

                    // Ochrana preskoceni rozsahu
                    if (i + equalDatesCounter + 1 > tasksSortedAscDueDate.size() - 1)
                        break;

                    // Neni-li zadny dalsi ukol se shodnym datumem, prejdi na nove datum
                    // Zvys i o pocet ukolu pridanych navic, for cyklus navic zvysi na i++
                    i += equalDatesCounter;
                }

                // Jako posledni seznam zarad seznam ukolu bez vyplneneho datumu splneni
                // Ve finale budou ukoly bez datumu i mista na uplnem konci seznamu
                listEqualDatesLists.add(tasksWithoutDueDate);

                // Razeni dle vzdalenosti mista splneny od soucasne polohy
                List<Task> listFinalSortedTasks = new ArrayList<Task>();
                for (List<Task> listEqualDates: listEqualDatesLists) {
                    // Pouziti TreeMap - prvek vzdy zarazen na pozici dle vzdalenosti.
                    // Key = currentPlace to TaskPlace distance, Value = taskId
                    TreeMap<Float, Integer> mapByDistance = new TreeMap<Float, Integer>();
                    List<Task> tasksWithoutTaskPlace = new ArrayList<Task>();
                    for (Task task : listEqualDates) {
                        if (task.getTaskPlaceId() != -1) {
                            // Ukol s vyplnenym mistem zarad do TreeMap
                            // dle jeho vzdalenosti od soucasne pozice
                            TaskPlace taskPlace = dataModel.getTaskPlace(task.getTaskPlaceId());

                            Location endLocation = new Location("provider z databaze");
                            endLocation.setLatitude(taskPlace.getLatitude());
                            endLocation.setLongitude(taskPlace.getLongitude());

                            // Vzdalenost mezi misty v metrech
                            Float currentToEndDistance = currentLocation.distanceTo(endLocation);
                            mapByDistance.put(currentToEndDistance, task.getId());
                        } else {
                            // Pokud ukol nema vyplneno misto, odloz si ho do pomocneho seznamu
                            tasksWithoutTaskPlace.add(task);
                        }
                    }

                    // Pridej ukoly daneho datumu s vyplnenym mistem do finalniho seznamu
                    for (Map.Entry<Float, Integer> entry : mapByDistance.entrySet()) {
                        Integer taskId = entry.getValue();
                        listFinalSortedTasks.add(dataModel.getTask(taskId));
                    }
                    // Pridej ukoly daneho datumu bez vyplneneho mista do finalniho seznamu
                    for (int i = 0; i < tasksWithoutTaskPlace.size(); i++) {
                        listFinalSortedTasks.add(tasksWithoutTaskPlace.get(i));
                    }
                }

                // Aktualizace poradi v seznamu ukolu.
                taskRecyclerAdapter = new TaskRecyclerAdapter(listFinalSortedTasks);
                rvTaskList.setAdapter(taskRecyclerAdapter);
                return true;

            // Seradit seznam ukolu dle vzdalenosti od soucasne polohy.
            case R.id.sort_by_distance:
                // Kontrola permission k lokalizaci
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                // Zjisti soucasnou pozici
                LocationManager locationManager2 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria2 = new Criteria();
                String provider2 = locationManager2.getBestProvider(criteria2, true);
                Location currentLocation2 = locationManager2.getLastKnownLocation(provider2);

                List<Task> listTasks = dataModel.getTasksByListId(listId, orderAscendingDueDate);
                List<Task> tasksWithoutTaskPlace = new ArrayList<Task>();

                // Pouziti TreeMap - prvek vzdy zarazen na pozici dle vzdalenosti.
                // K = currentPlace to TaskPlace distance, V = taskId
                TreeMap<Float, Integer> mapDistance = new TreeMap<Float, Integer>();
                for (Task task : listTasks) {
                    if (task.getTaskPlaceId() != -1) {
                        // Ukol s vyplnenym mistem zarad do TreeMap
                        // dle jeho vzdalenosti od soucasne pozice
                        TaskPlace taskPlace = dataModel.getTaskPlace(task.getTaskPlaceId());

                        Location endLocation = new Location("provider z databaze");
                        endLocation.setLatitude(taskPlace.getLatitude());
                        endLocation.setLongitude(taskPlace.getLongitude());

                        // Vzdalenost mezi misty v metrech
                        Float currentToEndDistance = currentLocation2.distanceTo(endLocation);
                        mapDistance.put(currentToEndDistance, task.getId());
                    } else {
                        // Pokud ukol nema vyplneno misto, odloz si ho do pomocneho seznamu
                        tasksWithoutTaskPlace.add(task);
                    }
                }

                List<Task> sortedTasksByDistance = new ArrayList<Task>();
                for (Map.Entry<Float, Integer> entry : mapDistance.entrySet()) {
                    Integer taskId = entry.getValue();
                    sortedTasksByDistance.add(dataModel.getTask(taskId));
                }
                for (int i = 0; i < tasksWithoutTaskPlace.size(); i++) {
                    sortedTasksByDistance.add(tasksWithoutTaskPlace.get(i));
                }

                // Aktualizace poradi v seznamu ukolu.
                taskRecyclerAdapter = new TaskRecyclerAdapter(sortedTasksByDistance);
                rvTaskList.setAdapter(taskRecyclerAdapter);
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
        if (!hideCompleted) {
            taskRecyclerAdapter = new TaskRecyclerAdapter(dataModel.getTasksByListId(listId, orderAscendingDueDate));
        } else {
            taskRecyclerAdapter = new TaskRecyclerAdapter(dataModel.getIncompletedTasksByListId(listId, orderAscendingDueDate));
        }
        rvTaskList.setAdapter(taskRecyclerAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Po navratu z detailu ukolu.
        if (requestCode == 121) {
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
