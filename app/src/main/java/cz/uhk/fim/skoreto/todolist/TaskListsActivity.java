package cz.uhk.fim.skoreto.todolist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
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

import java.io.File;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskList;
import cz.uhk.fim.skoreto.todolist.utils.TaskListAdapter;

/**
 * Aktivita reprezentujici seznam seznamu ukolu.
 * Created by Tomas.
 */
public class TaskListsActivity extends AppCompatActivity {

    private Toolbar tlbTaskListsActivity;
    private ActionBar actionBar;
    private ListView lvTaskLists;
    private ArrayAdapter<TaskList> arrayAdapter;
    private DataModel dataModel;
    private EditText etTaskListName;

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_lists_activity);

        // Kontrola permission k ulozisti
        if (ContextCompat.checkSelfPermission(TaskListsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(TaskListsActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(TaskListsActivity.this,
                        "Povolení přístupu k zápisu do úložiště je vyžadováno." ,
                        Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(TaskListsActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            Toast.makeText(TaskListsActivity.this,
                    "Povolení přístupu k zápisu do úložiště bylo již uděleno." ,
                    Toast.LENGTH_SHORT).show();
        }

        // Implementace ActionBaru.
        tlbTaskListsActivity = (Toolbar) findViewById(R.id.tlbTaskListsActivity);
        if (tlbTaskListsActivity != null) {
            setSupportActionBar(tlbTaskListsActivity);

            // Ziskani podpory ActionBaru korespondujiciho s Toolbarem.
            actionBar = getSupportActionBar();

            actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setIcon(R.drawable.ic_action_launch);
            actionBar.setTitle("MultiList");
        }

        dataModel = new DataModel(this);
        lvTaskLists = (ListView) findViewById(R.id.lvTaskListsList);
        arrayAdapter = new TaskListAdapter(TaskListsActivity.this, dataModel.getAllTaskLists());
        lvTaskLists.setAdapter(arrayAdapter);

        lvTaskLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Po klepnuti na polozku listu ziskej instanci zvoleneho seznamu ukolu.
                TaskList taskList = (TaskList) lvTaskLists.getItemAtPosition(position);

                Intent mainActivityIntent = new Intent(getApplication(), TaskListActivity.class);
                // Predej ID seznamu pro prechod do aktivity TaskListActivity.
                mainActivityIntent.putExtra("listId", taskList.getId());
                startActivity(mainActivityIntent);
            }
        });

        // Registrace vsech itemu List View pro Context Menu.
        registerForContextMenu(lvTaskLists);
    }

    /**
     * Metoda pro inicializaci layoutu ActionBaru.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_lists_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Metoda pro obsluhu tlacitek v ActionBaru.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task_list:
                // Dialog pro pridani noveho seznamu.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Název seznamu");

                etTaskListName = new EditText(this);
                etTaskListName.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(etTaskListName);

                // Obsluha tlacitka OK dialogu.
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etTaskListName.getText().toString();

                        // Pokud neni prazdny nazev noveho seznamu ukolu.
                        if (!etTaskListName.getText().toString().equals("")) {
                            // Ve vychozim pripade prida novy seznam ukolu s prazdnym popisem do Inboxu jako nesplneny.
                            dataModel.addTaskList(etTaskListName.getText().toString());

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

                // Obsluha tlacitka Zrusit dialogu.
                builder.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;

            default:
                // Vyvolani superclass pro obsluhu nerozpoznane akce.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metoda pro inicializaci layoutu ContextMenu.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_lists_context_menu, menu);
    }

    /**
     * Metoda pro obsluhu tlacitek v ContextMenu.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Ziskani instance vybraneho seznamu.
        final TaskList selectedTaskList = (TaskList) lvTaskLists.getItemAtPosition((int) info.id);

        switch (item.getItemId()) {
            // Prejmenovat vybrany seznam.
            case R.id.task_list_rename:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Nový název seznamu");

                etTaskListName = new EditText(this);
                etTaskListName.setInputType(InputType.TYPE_CLASS_TEXT);
                etTaskListName.setText(selectedTaskList.getName());
                builder.setView(etTaskListName);

                // Obsluha tlacitka OK dialogu.
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etTaskListName.getText().toString();

                        // Pokud neni prazdny nazev noveho seznamu ukolu.
                        if (!etTaskListName.getText().toString().equals("")) {
                            // Prirazeni noveho nazvu seznamu.
                            selectedTaskList.setName(etTaskListName.getText().toString());
                            // Update v databazi.
                            dataModel.updateTaskList(selectedTaskList);

                            // Aktualizace seznamu ukolu.
                            arrayAdapter.clear();
                            arrayAdapter.addAll(dataModel.getAllTaskLists());

                            // Informovani uzivatele o uspesnem pridani seznamu ukolu.
                            Toast.makeText(TaskListsActivity.this, "Seznam přejmenován", Toast.LENGTH_SHORT).show();
                        } else {
                            // Informovani uzivatele o nutnosti vyplnit nazev seznamu ukolu.
                            Toast.makeText(TaskListsActivity.this, "Prázdný název seznamu!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Obsluha tlacitka Zrusit dialogu.
                builder.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;

            // Smaz vybrany seznam vcetne jeho ukolu.
            case R.id.task_list_delete:
                // Ziskani vsech ukolu v mazanem seznamu.
                List<Task> tasksInList = dataModel.getTasksByListId(selectedTaskList.getId(), true);

                // Postupne mazani vsech ukolu v seznamu vcetne fotografii a nahravek.
                for (Task task: tasksInList) {
                    // Smazani stare fotografie, pokud je o ni zaznam a pokud jeji soubor existuje.
                    if (!task.getPhotoName().equals("")) {
                        String oldTaskPhotoPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos/" + task.getPhotoName() + ".jpg";
                        File oldTaskPhoto = new File(oldTaskPhotoPath);
                        boolean isTaskPhotoDeleted = oldTaskPhoto.delete();
                    }

                    // Smazani stare nahravky, pokud je o ni zaznam a pokud jeji soubor existuje.
                    if (!task.getRecordingName().equals("")) {
                        String oldTaskRecordingPath = Environment.getExternalStorageDirectory() + "/MultiList/Recordings/" + task.getRecordingName() + ".3gp";
                        File oldTaskRecording = new File(oldTaskRecordingPath);
                        boolean isTaskRecordingDeleted = oldTaskRecording.delete();
                    }

                    dataModel.deleteTask(task.getId());
                }

                // Smazani seznamu z databaze.
                dataModel.deleteTaskList(selectedTaskList.getId());
                // Aktualizace seznamu ukolu.
                arrayAdapter.clear();
                arrayAdapter.addAll(dataModel.getAllTaskLists());

                Toast.makeText(TaskListsActivity.this, "Seznam s úkoly smazán", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Metoda handlujici request pristupu k dangerous zdrojum.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(TaskListsActivity.this, "Povolení uděleno" , Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TaskListsActivity.this, "Povolení k přístupu do uložiště nebylo uděleno" , Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}
