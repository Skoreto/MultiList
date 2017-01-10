package cz.uhk.fim.skoreto.todolist;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
 * Aktivita pro zobrazeni detailu ukolu.
 * Created by Tomas Skorepa.
 */
public class TaskDetailActivity extends AppCompatActivity {

    private Toolbar tlbEditTaskActivity;
    private ActionBar actionBar;
    private Task task;
    private TextView tvTaskName;
    private EditText etTaskDueDate;

    private EditText etTaskDescription;
    private CheckBox chbTaskCompleted;
    private Spinner spinTaskLists;
    private DataModel dm;
    private int taskId;
    private int listId;

    private AudioManager audioManager;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private ImageView ivTaskPhoto;
    private static final int REQUEST_TAKE_PHOTO = 888;
    private String photoFileName;
    private String photoThumbnailFileName;
    private String folderPath;
    private String thumbnailFolderPath;

    private Calendar calendar;
    private DatePickerDialog datePickerDialog;

    private final int PERMISSIONS_REQUEST_CAMERA = 102;
    private final int PERMISSIONS_REQUEST_RECORD_AUDIO = 103;

    static final int NUM_ITEMS = 2;
    MyAdapter mAdapter;
    ViewPager mPager;

    /**
     * Metoda pro zobrazeni predvyplneneho formulare upravy ukolu.
     */
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_detail_activity);
        dm = new DataModel(this);

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

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.goto_first);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPager.setCurrentItem(0);
            }
        });
        button = (Button)findViewById(R.id.goto_last);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPager.setCurrentItem(NUM_ITEMS-1);
            }
        });

        tvTaskName = (TextView) findViewById(R.id.tvTaskName);
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

        tvTaskName.setText(task.getName());
        if (task.getDueDate() != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
            etTaskDueDate.setText(dateFormat.format(task.getDueDate()));
        } else {
            etTaskDueDate.setText("");
        }
        etTaskDescription.setText(task.getDescription());

        // Zaskrtnuti checkboxu podle toho zda ukol je/neni splnen.
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
        spinTaskLists.setSelection(taskListsAdapter.getPosition(dm.getTaskListById(listId)), true);

        // Listener pro kliknuti na spinner.
        spinTaskLists.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TaskList taskList = (TaskList) spinTaskLists.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (!task.getPhotoName().equals("")) {
            // Prime prirazeni nahledu fotografie do ImageView.
            String photoThumbnailPath = Environment.getExternalStorageDirectory() + "/MultiList/PhotoThumbnails/" + "THUMBNAIL_" + task.getPhotoName() + ".jpg";
            final String photoPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos/" + task.getPhotoName() + ".jpg";
            ivTaskPhoto.setImageBitmap(BitmapFactory.decodeFile(photoThumbnailPath));

            ivTaskPhoto.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       // Zobrazeni velke fotografie po kliknuti na nahled.
                       Intent sendPhotoDirectoryIntent = new Intent(TaskDetailActivity.this, SinglePhotoActivity.class);
                       sendPhotoDirectoryIntent.putExtra("photoPath", photoPath);
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

    }

    /**
     * Metoda pro smazani ukolu.
     * Kaskadne vymaze pripojene fotografie a nahravky z externiho uloziste.
     */
    public void deleteTask() {
        // Smazani stare fotografie, pokud je o ni zaznam a pokud jeji soubor existuje.
        if (!task.getPhotoName().equals("")) {
            String oldTaskPhotoPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos/" + task.getPhotoName() + ".jpg";
            File oldTaskPhoto = new File(oldTaskPhotoPath);
            boolean isTaskPhotoDeleted = oldTaskPhoto.delete();

            // Smazani prislusne miniatury stare fotografie.
            String oldTaskPhotoThumbnailPath = Environment.getExternalStorageDirectory() + "/MultiList/PhotoThumbnails/" + "THUMBNAIL_" + task.getPhotoName() + ".jpg";
            File oldTaskPhotoThumbnail = new File(oldTaskPhotoThumbnailPath);
            boolean isTaskPhotoThumbnailDeleted = oldTaskPhotoThumbnail.delete();
        }

        // Smazani stare nahravky, pokud je o ni zaznam a pokud jeji soubor existuje.
        if (!task.getRecordingName().equals("")) {
            String oldTaskRecordingPath = Environment.getExternalStorageDirectory() + "/MultiList/Recordings/" + task.getRecordingName() + ".3gp";
            File oldTaskRecording = new File(oldTaskRecordingPath);
            boolean isTaskRecordingDeleted = oldTaskRecording.delete();
        }

        // Smazani mista nalezejicimu k ukolu z databaze
        if (task.getTaskPlaceId() != -1)
            dm.deleteTaskPlace(task.getTaskPlaceId());

        dm.deleteTask(taskId);
        // Informovani uzivatele o uspesnem smazani ukolu.
        Toast.makeText(TaskDetailActivity.this, "Úkol smazán", Toast.LENGTH_SHORT).show();

        // Presmerovani na seznam ukolu, odkud ukol pochazi.
        Intent returnIntent = new Intent();
        returnIntent.putExtra("listId", listId);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
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

            // Smazani prislusne miniatury stare fotografie.
            String oldTaskPhotoThumbnailPath = Environment.getExternalStorageDirectory() + "/MultiList/PhotoThumbnails/" + "THUMBNAIL_" + task.getPhotoName() + ".jpg";
            File oldTaskPhotoThumbnail = new File(oldTaskPhotoThumbnailPath);
            boolean isTaskPhotoThumbnailDeleted = oldTaskPhotoThumbnail.delete();
        }

        // Vytvor unikatni jmeno fotografie z casu iniciace vyfoceni ukolu.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        photoFileName = timeStamp;
        photoThumbnailFileName = "THUMBNAIL_" + timeStamp;

        // Vytvor potrebne slozky "Internal storage: /MultiList/Photos" pokud neexistuji.
        folderPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            File photosDirectory = new File(folderPath);
            photosDirectory.mkdirs();
        }

        thumbnailFolderPath = Environment.getExternalStorageDirectory() + "/MultiList/PhotoThumbnails";
        File thumbnailFolder = new File(thumbnailFolderPath);
        if (!thumbnailFolder.exists()) {
            File photoThumbnailsDirectory = new File(thumbnailFolderPath);
            photoThumbnailsDirectory.mkdirs();
        }

        // Uloz soubor fotografie do slozky MultiListPhotos.
        File photoFile = new File(folderPath + File.separator + photoFileName + ".jpg");

        // Prirad v databazi fotografii k ukolu.
        task.setPhotoName(photoFileName);
        dm.updateTask(task);

        Toast.makeText(TaskDetailActivity.this, "Vyfoť úkol", Toast.LENGTH_SHORT).show();
        return photoFile;
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
            case R.id.action_edit_task:
                Intent taskEditIntent = new Intent(TaskDetailActivity.this, TaskEditActivity.class);
                // Predej ID ukolu do intentu taskEditIntent.
                taskEditIntent.putExtra("taskId", task.getId());
                // Predej ID seznamu pro prechod do aktivity TaskEditActivity.
                taskEditIntent.putExtra("listId", task.getListId());
                (TaskDetailActivity.this).startActivityForResult(taskEditIntent, 122);
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
        if (bReady) {
            // Kontrola permission k mikrofonu
            if (ContextCompat.checkSelfPermission(TaskDetailActivity.this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(TaskDetailActivity.this,
                        Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(TaskDetailActivity.this,
                            "Povolení přístupu k mikrofonu je nutné pro nahrání úkolu.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(TaskDetailActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSIONS_REQUEST_RECORD_AUDIO);
                    // V pripade ziskani povoleni nahravat zvuk v onRequestPermissionsResult
                }
            }
        } else {
            AudioController.stopRecording(mediaRecorder);
            mediaRecorder = null;
        }
    }

    /**
     * Metoda pro obsluhu tlacitka spusteni prehravani.
     */
    private void onPlayPressed(boolean bReady) {
        if (bReady) {
            mediaPlayer = new MediaPlayer();
            AudioController.startPlaying(dm, taskId, mediaPlayer, TaskDetailActivity.this);
        } else {
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
            // Clearne nastaveni recorderu.
            mediaRecorder.reset();
            // Uvolneni instance recorderu.
            mediaRecorder.release();
            mediaRecorder = null;
        }

        // Uvolni mediaPlayer, pokud zustala instance vytvorena.
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Metoda handlujici request pristupu k dangerous zdrojum.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Povoleni udeleno, spustit fotoaparat
                    takePhoto();
                } else {
                    Toast.makeText(TaskDetailActivity.this,
                            "Povolení nebylo uděleno, nelze spustit fotoaparát.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Povoleni udeleno, spustit nahravani zvuku
                    mediaRecorder = new MediaRecorder();
                    AudioController.startRecording(task, mediaRecorder, audioManager, dm, TaskDetailActivity.this);
                } else {
                    Toast.makeText(TaskDetailActivity.this,
                            "Povolení k mikrofonu nebylo uděleno, nelze nahrávat zvuk.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Po navratu z upravy ukolu.
        if (requestCode == 122) {
            if(resultCode == Activity.RESULT_OK){
                // Refreshni udaje nove upraveneho ukolu.
                taskId = data.getIntExtra("taskId", 1);
                task = dm.getTask(taskId);

                tvTaskName.setText(task.getName());
                if (task.getDueDate() != null) {
                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    etTaskDueDate.setText(dateFormat.format(task.getDueDate()));
                } else {
                    etTaskDueDate.setText("");
                }
                etTaskDescription.setText(task.getDescription());

                // Zaskrtnuti checkboxu podle toho zda ukol je/neni splnen.
                if (task.getCompleted() == 1)
                    chbTaskCompleted.setChecked(true);
                if (task.getCompleted() == 0)
                    chbTaskCompleted.setChecked(false);

                // Pokud bylo vybrano misto ukolu, inicializuj ho
                if (task.getTaskPlaceId() != -1) {
//                    chosenTaskPlace = dm.getTaskPlace(task.getTaskPlaceId());
//                    etTaskPlace.setText(chosenTaskPlace.getAddress());
                }
            }
        }
        // Po potvrzeni vyfocene fotografie prejdi na stejnou upravu ukolu.
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // Vytvoreni zmenseneho nahledu z porizene fotografie.
            Bitmap photoBitmap = BitmapFactory.decodeFile(folderPath + File.separator + photoFileName + ".jpg");
            Bitmap photoThumbnail = Bitmap.createScaledBitmap(photoBitmap, 200, 356, true);

            // Ulozeni nahledu do externiho uloziste.
            try {
                OutputStream stream = new FileOutputStream(thumbnailFolderPath + File.separator + photoThumbnailFileName + ".jpg");
                photoThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            } catch (IOException e) {
                Toast.makeText(TaskDetailActivity.this, "Chyba při vytváření náhledu fotografie", Toast.LENGTH_SHORT).show();
            }

            // Presmerovani na seznam ukolu, odkud ukol pochazi.
            Intent returnIntent = new Intent();
            returnIntent.putExtra("listId", listId);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        // Pokud != RESULT_OK - nedelat nic - dulezite napr. pro tlacitko zpet v dolnim panelu.
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return GeneralFragment.newInstance(position);
                case 1:
                    return ArrayListFragment.newInstance(position);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }

    public static class ArrayListFragment extends ListFragment {
        int mNum;
        String[] CHEESES = {
                "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance" };

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static ArrayListFragment newInstance(int num) {
            ArrayListFragment f = new ArrayListFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_pager_list, container, false);
            View tv = v.findViewById(R.id.text);
            ((TextView)tv).setText("Fragment #" + mNum);
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, CHEESES));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Log.i("FragmentList", "Item clicked: " + id);
        }
    }

    public static class GeneralFragment extends Fragment {
        int mNum;
        Task task;
        private TextView tvTaskPlace;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static GeneralFragment newInstance(int num) {
            GeneralFragment f = new GeneralFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_pager_general, container, false);
            View tv = v.findViewById(R.id.text);
            ((TextView)tv).setText("Fragment #" + mNum);


            tvTaskPlace = (TextView) v.findViewById(R.id.tvTaskPlace);

//            // Pokud bylo vybrano misto ukolu, inicializuj ho
//            if (task.getTaskPlaceId() != -1) {
//                chosenTaskPlace = dm.getTaskPlace(task.getTaskPlaceId());
//                etTaskPlace.setText(chosenTaskPlace.getAddress());
//            }

            return v;
        }

    }

}


