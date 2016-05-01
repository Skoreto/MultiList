package cz.uhk.fim.skoreto.todolist;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskList;
import cz.uhk.fim.skoreto.todolist.utils.AudioController;

/**
 * Aktivita pro zmenu, smazani a zobrazeni detailu ukolu.
 * Created by Tomas.
 */
public class TaskDetailActivity extends AppCompatActivity {

    Toolbar tlbEditTaskActivity;
    ActionBar actionBar;
    Task task;
    EditText etTaskName;
    EditText etTaskDueDate;
    EditText etTaskDescription;
    CheckBox chbTaskCompleted;
    Spinner spinTaskLists;
    Button btnTakePhoto;
    DataModel dm = new DataModel(this);
    int taskId;
    int listId;

    private AudioManager audioManager;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    ImageView ivTaskPhoto;
    static final int REQUEST_TAKE_PHOTO = 888;

    Calendar calendar;
    DatePickerDialog datePickerDialog;

    /**
     * Metoda pro zobrazeni predvyplneneho formulare upravy ukolu.
     */
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail_activity);

        // Implementace ActionBaru.
        tlbEditTaskActivity = (Toolbar) findViewById(R.id.tlbEditTaskListActivity);
        if (tlbEditTaskActivity != null) {
            setSupportActionBar(tlbEditTaskActivity);

            // Ziskani podpory ActionBaru korespondujiciho s Toolbarem.
            actionBar = getSupportActionBar();
            // Povoleni tlacitka Zpet.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Detail úkolu");
        }

        etTaskName = (EditText) findViewById(R.id.etTaskName);
        etTaskDueDate = (EditText) findViewById(R.id.etTaskDueDate);
        etTaskDescription = (EditText) findViewById(R.id.etTaskDescription);
        chbTaskCompleted = (CheckBox) findViewById(R.id.chbTaskCompleted);
        spinTaskLists = (Spinner) findViewById(R.id.spinTaskLists);

        ivTaskPhoto = (ImageView) findViewById(R.id.ivTaskPhoto);

        Intent anyTaskListIntent = getIntent();
        // Nastaveni listId pro filtraci ukolu v seznamu.
        // Ve vychozim pripade 1 (Inbox) - pokud IntExtra neprijde ze zadneho intentu.
        listId = anyTaskListIntent.getIntExtra("listId", 1);
        // Nastaveni prichozi taskId z TaskListActivity pro ziskani aktualne upravovaneho ukolu.
        taskId = anyTaskListIntent.getIntExtra("taskId", 1);
        task = dm.getTask(taskId);

        etTaskName.setText(task.getName());
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        etTaskDueDate.setText(dateFormat.format(task.getDueDate()));
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

        if (!task.getPhotoName().equals("")) {
            final String photoDir = Environment.getExternalStorageDirectory() + "/MultiList/Photos/" + task.getPhotoName() + ".jpg";
            // TODO Prirazeni prave miniatury
//            String photoDir = task.getPhotoName();
//            ivTaskPhoto.setImageBitmap(BitmapFactory.decodeFile(photoDir));

            Bitmap photoThumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoDir), 200, 356);
            ivTaskPhoto.setImageBitmap(photoThumbnail);

            ivTaskPhoto.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       // Zobrazeni velke fotografie.
                       Intent sendPhotoDirectoryIntent = new Intent(TaskDetailActivity.this, SinglePhotoActivity.class);
                       sendPhotoDirectoryIntent.putExtra("photoDir", photoDir);
                       startActivity(sendPhotoDirectoryIntent);
                   }
               }
            );
        }

        // NAHRAVANI / PREHRAVANI ZVUKU
        final ToggleButton btnRecordTask = (ToggleButton) findViewById(R.id.btnRecordTask);
        final ToggleButton btnPlayTask = (ToggleButton) findViewById(R.id.btnPlayTask);

        btnRecordTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btnPlayTask.setEnabled(!isChecked);
                onRecordPressed(isChecked);
            }
        });

        btnPlayTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btnRecordTask.setEnabled(!isChecked);
                onPlayPressed(isChecked);
            }
        });

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(afcListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // DATE PICKER - DUE DATE
        calendar = Calendar.getInstance();

        // Listener pro potvrzeni vybraneho datumu v dialogu kalendare.
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Sestaveni noveho datumu.
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Date newDueDate = calendar.getTime();

                // Nastaveni datumu aktualni instanci ukolu.
                task.setDueDate(newDueDate);

                // Zobrazeni noveho datumu v EditTextu.
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                etTaskDueDate.setText(dateFormat.format(task.getDueDate()));
            }
        };

        etTaskDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Zjisteni aktualniho roku, mesice, dne.
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Pouzit aktualni datum jako vychozi datum v datepickeru.
                datePickerDialog = new DatePickerDialog(TaskDetailActivity.this, datePickerListener, year, month, day);
                datePickerDialog.show();
            }
        });

        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

    }

    /**
     * Metoda pro zmenu atributu ukolu.
     */
    public void editTask(){
        etTaskName = (EditText) findViewById(R.id.etTaskName);
        etTaskDescription = (EditText) findViewById(R.id.etTaskDescription);
        chbTaskCompleted = (CheckBox) findViewById(R.id.chbTaskCompleted);

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
        Toast.makeText(TaskDetailActivity.this, "Úkol upraven", Toast.LENGTH_SHORT).show();

        finish();
    }

    /**
     * Metoda pro smazani ukolu.
     */
    public void deleteTask(){
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

        dm.deleteTask(taskId);
        // Informovani uzivatele o uspesnem smazani ukolu.
        Toast.makeText(TaskDetailActivity.this, "Úkol smazán", Toast.LENGTH_SHORT).show();

        finish();
    }

    /**
     * Metoda pro intent prechodu na TaskListActivity.
     */
    public void activateTaskListActivity(int listId){
        Intent taskListActivityIntent = new Intent(getApplication(), TaskListActivity.class);
        // Predej ID seznamu pro prechod do aktivity TaskListActivity.
        taskListActivityIntent.putExtra("listId", listId);
        startActivity(taskListActivityIntent);
    }

    /**
     * Metoda pro otevreni fotoaparatu po kliknuti na tlacitko Vyfot a sejmuti fotografie.
     * Pro volani metody z XML nutne predate argument takePhoto(View view) !!!
     */
    public void takePhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            // Vytvor soubor, do ktereho bude fotografie zapsana.
            File photoFile = null;
            try {
                photoFile = createPhotoFile();
            } catch (IOException ex) {
                Toast.makeText(TaskDetailActivity.this, "Vyskytla se chyba při vytváření souboru fotografie", Toast.LENGTH_SHORT).show();
            }

            // Pokracuj pouze, pokud byl soubor uspesne vytvoren.
            if (photoFile != null) {
                Toast.makeText(TaskDetailActivity.this, "Vyfoť úkol", Toast.LENGTH_SHORT).show();
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                // Bude obslouzeno metodou onActivityResult.
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Metoda pro vytvoreni souboru fotografie a jeji ulozeni do interniho uloziste.
     */
    private File createPhotoFile() throws IOException {
        // Smazani stare fotografie, pokud je o ni zaznam a pokud jeji soubor existuje.
        if (!task.getPhotoName().equals("")) {
            String oldTaskPhotoPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos/" + task.getPhotoName() + ".jpg";
            File oldTaskPhoto = new File(oldTaskPhotoPath);
            boolean isTaskPhotoDeleted = oldTaskPhoto.delete();
        }

        // Vytvor unikatni jmeno fotografie z casu iniciace vyfoceni ukolu.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String photoFileName = "JPEG_" + timeStamp;

        // Vytvor potrebne slozky "Internal storage: /MultiList/MultiListPhotos" pokud neexistuji.
        String folderPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            File photosDirectory = new File(folderPath);
            photosDirectory.mkdirs();
        }

        // Uloz soubor fotografie do slozky MultiListPhotos.
        File photoFile = new File(folderPath + File.separator + photoFileName + ".jpg");

        // Prirad v databazi fotografii k ukolu.
        task.setPhotoName(photoFileName);
        dm.updateTask(task);

        Toast.makeText(TaskDetailActivity.this, photoFileName, Toast.LENGTH_SHORT).show();
        return photoFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Po potvrzeni vyfocene fotografie prejdi na stejnou upravu ukolu.
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Intent taskDetailActivityIntent = new Intent(getApplication(), TaskDetailActivity.class);
            // Predej ID ukolu do intentu editTaskIntent.
            taskDetailActivityIntent.putExtra("taskId", task.getId());
            // Predej ID seznamu pro prechod do aktivity TaskDetailActivity.
            taskDetailActivityIntent.putExtra("listId", listId);
            startActivity(taskDetailActivityIntent);
        }
    }

    /**
     * Metoda pro inicializaci layoutu ActionBaru.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_detail_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Metoda pro obluhu tlacitek v ActionBaru.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Smazat ukol.
            case R.id.action_delete_task:
                // Potvrdit zmeny a ulozit do databaze.
                deleteTask();
                return true;

            // Potvrdit zmeny a ulozit do databaze.
            case R.id.action_done:
                editTask();
                return true;

            default:
                // Vyvolani superclass pro obsluhu nerozpoznane akce.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metoda pro obsluhu tlacitka pro spusteni nahravani zvuku.
     */
    private void onRecordPressed(boolean bReady) {
        if (bReady) AudioController.startRecording(task, mediaRecorder, audioManager, dm, TaskDetailActivity.this);
        else {
            AudioController.stopRecording(mediaRecorder);
            mediaRecorder = null;
        }
    }

    /**
     * Metoda pro obsluhu tlacitka spusteni prehravani.
     */
    private void onPlayPressed(boolean bReady) {
        if (bReady) AudioController.startPlaying(dm, taskId, mediaPlayer, TaskDetailActivity.this);
        else {
            AudioController.stopPlaying(mediaPlayer);
            mediaPlayer = null;
        }
    }

    AudioManager.OnAudioFocusChangeListener afcListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                audioManager.abandonAudioFocus(afcListener);
                if (mediaPlayer.isPlaying()) AudioController.stopPlaying(mediaPlayer);
            }
        }
    };

    /**
     * Ochrana pro uvolneni zdroju prehravace a mikrofonu po preruseni aktivity.
     */
    @Override
    public void onPause() {
        super.onPause();
        // Uvolni mediaRecorder, pokud zustala instance vytvorena.
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }

        // Uvolni mediaPlayer, pokud zustala instance vytvorena.
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


}


