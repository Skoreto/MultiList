package cz.uhk.fim.skoreto.todolist;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskList;
import cz.uhk.fim.skoreto.todolist.utils.AudioController;


/**
 * Aktivita pro zmenu a smazani ukolu.
 * Created by Tomas.
 */
public class EditTaskActivity extends AppCompatActivity {

    Toolbar tlbEditTaskActivity;
    ActionBar actionBar;
    Task task;
    EditText etTaskName;
    EditText etTaskDescription;
    CheckBox chbTaskCompleted;
    Spinner spinTaskLists;
    DataModel dm = new DataModel(this);
    int taskId;
    int listId;

    private AudioManager audioManager;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    ImageView ivTaskPhoto;
    static final int REQUEST_TAKE_PHOTO = 888;

    /**
     * Metoda pro zobrazeni predvyplneneho formulare upravy ukolu.
     */
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task_activity);

        // Implementace ActionBaru.
        tlbEditTaskActivity = (Toolbar) findViewById(R.id.tlbEditTaskListActivity);
        if (tlbEditTaskActivity != null) {
            setSupportActionBar(tlbEditTaskActivity);

            // Ziskani podpory ActionBaru korespondujiciho s Toolbarem.
            actionBar = getSupportActionBar();
            // Povoleni tlacitka Zpet.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        etTaskName = (EditText) findViewById(R.id.etTaskName);
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
            String photoDir = Environment.getExternalStorageDirectory() + "/MultiList/MultiListPhotos/" + task.getPhotoName() + ".jpg";
//            String photoDir = task.getPhotoName();
//            ivTaskPhoto.setImageBitmap(BitmapFactory.decodeFile(photoDir));

            Bitmap photoThumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoDir), 200, 356);
            ivTaskPhoto.setImageBitmap(photoThumbnail);
        }

        ivTaskPhoto.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Toast.makeText(EditTaskActivity.this, "Zobraz fotku", Toast.LENGTH_SHORT).show();
               }
           }
        );

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
        Toast.makeText(EditTaskActivity.this, "Úkol upraven", Toast.LENGTH_SHORT).show();

        this.activateTaskListActivity(listId);
    }

    /**
     * Metoda pro smazani ukolu.
     */
    public void deleteTask(){
        dm.deleteTask(taskId);
        // Informovani uzivatele o uspesnem smazani ukolu.
        Toast.makeText(EditTaskActivity.this, "Úkol smazán", Toast.LENGTH_SHORT).show();

        this.activateTaskListActivity(listId);
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
     */
    public void takePhoto(View view) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            // Vytvor soubor, do ktereho bude fotografie zapsana.
            File photoFile = null;
            try {
                photoFile = createPhotoFile();
            } catch (IOException ex) {
                Toast.makeText(EditTaskActivity.this, "Vyskytla se chyba při vytváření souboru fotografie", Toast.LENGTH_SHORT).show();
            }

            // Pokracuj pouze, pokud byl soubor uspesne vytvoren.
            if (photoFile != null) {
                Toast.makeText(EditTaskActivity.this, "Vyfoť úkol", Toast.LENGTH_SHORT).show();
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
        // Vytvor unikatni jmeno fotografie z casu iniciace vyfoceni ukolu.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String photoFileName = "JPEG_" + timeStamp;

        // Vytvor potrebne slozky "Internal storage: /MultiList/MultiListPhotos" pokud neexistuji.
        String folderPath = Environment.getExternalStorageDirectory() + "/MultiList/MultiListPhotos";
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

        Toast.makeText(EditTaskActivity.this, photoFileName, Toast.LENGTH_SHORT).show();
        return photoFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Po potvrzeni vyfocene fotografie prejdi na stejnou upravu ukolu.
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Intent editTaskIntent = new Intent(getApplication(), EditTaskActivity.class);
            // Predej ID ukolu do intentu editTaskIntent.
            editTaskIntent.putExtra("taskId", task.getId());
            // Predej ID seznamu pro prechod do aktivity EditTaskActivity.
            editTaskIntent.putExtra("listId", listId);
            startActivity(editTaskIntent);
        }
    }

    /**
     * Metoda pro inicializaci layoutu ActionBaru.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_task_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Metoda pro obluhu tlacitek v ActionBaru.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                // Potvrdit zmeny a ulozit do databaze.
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
        if (bReady) AudioController.startRecording(task, mediaRecorder, audioManager, dm, EditTaskActivity.this);
        else {
            AudioController.stopRecording(mediaRecorder);
            mediaRecorder = null;
        }
    }

    /**
     * Metoda pro obsluhu tlacitka spusteni prehravani.
     */
    private void onPlayPressed(boolean bReady) {
        if (bReady) AudioController.startPlaying(task, mediaPlayer, EditTaskActivity.this);
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


