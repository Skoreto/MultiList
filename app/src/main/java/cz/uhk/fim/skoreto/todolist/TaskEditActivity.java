package cz.uhk.fim.skoreto.todolist;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskList;
import cz.uhk.fim.skoreto.todolist.model.TaskPlace;
import cz.uhk.fim.skoreto.todolist.utils.AudioController;

/**
 * Aktivita pro upravu a smazani ukolu.
 * Created by Tomas Skorepa.
 */
public class TaskEditActivity extends AppCompatActivity {

    private Toolbar tlbEditTaskActivity;
    private ActionBar actionBar;
    private Task task;
    private EditText etTaskName;
    private EditText etTaskDueDate;
    private EditText etTaskPlace;
    private TextView tvRadius;
    private SeekBar sbRadius;
    private EditText etTaskDescription;
    private CheckBox chbTaskCompleted;
    private Spinner spinTaskLists;
    private ImageButton imgbtnCurrentPlace;
    private ImageButton imgbtnChooseTaskPlace;
    private ImageButton imgbtnTakePhoto;
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
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 104;
    private final int PERMISSIONS_REQUEST_CURRENT_PLACE = 105;

    private int REQUEST_PLACE_PICKER = 801;
    private TaskPlace chosenTaskPlace = null;
    private boolean chosenTaskPlaceChanged = false;
    private RequestQueue requestQueue;

    /**
     * Metoda pro zobrazeni predvyplneneho formulare upravy ukolu.
     */
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit_activity);
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
            actionBar.setTitle("Úprava úkolu");
        }

        etTaskName = (EditText) findViewById(R.id.etTaskName);
        etTaskDueDate = (EditText) findViewById(R.id.etTaskDueDate);
        etTaskPlace = (EditText) findViewById(R.id.etTaskPlace);
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
            chosenTaskPlace = dm.getTaskPlace(task.getTaskPlaceId());
            etTaskPlace.setText(chosenTaskPlace.getAddress());
        }

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
                       Intent sendPhotoDirectoryIntent = new Intent(TaskEditActivity.this, SinglePhotoActivity.class);
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
                datePickerDialog = new DatePickerDialog(TaskEditActivity.this, datePickerListener, year, month, day);
                datePickerDialog.show();
            }
        });

        imgbtnCurrentPlace = (ImageButton) findViewById(R.id.imgbtnCurrentPlace);
        imgbtnCurrentPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kontrola permission k GPS
                if (ContextCompat.checkSelfPermission(TaskEditActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(TaskEditActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(TaskEditActivity.this,
                                "Povolení přístupu k GPS je nutné pro zjištění aktuální polohy.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(TaskEditActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_CURRENT_PLACE);
                        // V pripade ziskani povoleni prejit na intent mapy
                    }
                } else {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    // Create a criteria object to retrieve provider
                    Criteria criteria = new Criteria();
                    // Get the name of the best provider
                    String provider = locationManager.getBestProvider(criteria, true);
                    // Get current location
                    final Location currentLocation = locationManager.getLastKnownLocation(provider);

                    // Ziskani adresy soucasne pozice z coordinates
                    // Oproti tride Geocoder vraci pristup s GeocodingAPI vzdy vysledek
                    requestQueue = Volley.newRequestQueue(TaskEditActivity.this);

                    JsonObjectRequest request = new JsonObjectRequest("https://maps.googleapis.com/maps/api/geocode/json?latlng="
                            + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                            + "&key=AIzaSyC1Vaq8FOHelH58mXhZ3Zn8ksvPbsb9loo", new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String currentPlaceAddress = response.getJSONArray("results")
                                        .getJSONObject(0).getString("formatted_address");

                                // Poznamenej si, ze misto bylo zmeneno. Udrz si novou instanci
                                // pred pripadnym updatem databaze po potvrzeni editace ukolu.
                                chosenTaskPlaceChanged = true;
                                chosenTaskPlace = new TaskPlace(currentLocation.getLatitude(),
                                        currentLocation.getLongitude(), currentPlaceAddress);
                                etTaskPlace.setText(chosenTaskPlace.getAddress());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(TaskEditActivity.this, "Volley networking chyba",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(request);
                }
            }
        });

        imgbtnChooseTaskPlace = (ImageButton) findViewById(R.id.imgbtnChooseTaskPlace);
        imgbtnChooseTaskPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kontrola permission k GPS
                if (ContextCompat.checkSelfPermission(TaskEditActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(TaskEditActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(TaskEditActivity.this,
                                "Povolení přístupu k GPS je nutné pro zjištění aktuální polohy.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(TaskEditActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                        // V pripade ziskani povoleni prejit na intent mapy
                    }
                } else {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                    try {
                        startActivityForResult(builder.build(TaskEditActivity.this), REQUEST_PLACE_PICKER);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // SEEKBAR radius
        tvRadius = (TextView) findViewById(R.id.tvRadius);
        sbRadius = (SeekBar) findViewById(R.id.sbRadius);
        sbRadius.setProgress(15);
        sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float radius = progress * 100;
                DecimalFormat df = new DecimalFormat("#.#");

                if (radius < 1000) {
                    // Pod 1 km zobrazuj vzdalenost v metrech
                    String sRadius = df.format(radius);
                    tvRadius.setText(sRadius + " m");
                } else {
                    // Nad 1 km zobrazuj vzdalenost v kilometrech
                    String sRadius = df.format(radius / 1000);
                    tvRadius.setText(sRadius + " km");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        imgbtnTakePhoto = (ImageButton) findViewById(R.id.imgbtnTakePhoto);
        imgbtnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kontrola permission k fotoaparatu
                if (ContextCompat.checkSelfPermission(TaskEditActivity.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(TaskEditActivity.this,
                            Manifest.permission.CAMERA)) {
                        Toast.makeText(TaskEditActivity.this,
                                "Povolení přístupu ke kameře je nutné pro vyfocení úkolu.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(TaskEditActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                PERMISSIONS_REQUEST_CAMERA);
                        // V pripade ziskani povoleni spustit fotoaparat v onRequestPermissionsResult
                    }
                } else {
                    takePhoto();
                }
            }
        });

    }

    /**
     * Metoda pro zmenu atributu ukolu.
     */
    public void editTask() {
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

        if (task.getTaskPlaceId() == -1) {
            // Pokud nebylo drive zvoleno misto ukolu
            if (chosenTaskPlaceChanged) {
                // A nyni je zvoleno misto ukolu
                // Vytvor v databazi novy zaznam mista, vrat jeho id a inicializuj instanci
                // chosenTaskPlace s id.
                long newTaskPlaceId = dm.addTaskPlaceReturnId(chosenTaskPlace.getLatitude(),
                        chosenTaskPlace.getLongitude(), chosenTaskPlace.getAddress());
                if (newTaskPlaceId == -1)
                    Toast.makeText(TaskEditActivity.this, "Chyba při přidávání nového místa do databáze", Toast.LENGTH_SHORT).show();
                else {
                    chosenTaskPlace = dm.getTaskPlace((int) newTaskPlaceId);
                    task.setTaskPlaceId((int) newTaskPlaceId);
                }
            }
        } else {
            // Pokud bylo jiz drive zvoleno misto ukolu
            if (chosenTaskPlaceChanged) {
                // Smaz puvodni misto z databaze.
                dm.deleteTaskPlace(task.getTaskPlaceId());

                if (chosenTaskPlace == null) {
                    // Ale bylo pouze odebrano
                    task.setTaskPlaceId(-1);
                } else {
                    // A nyni se misto ukolu zmenilo na nove
                    // Vytvor v databazi novy zaznam mista, vrat jeho id a inicializuj instanci
                    // chosenTaskPlace s id.
                    long newTaskPlaceId = dm.addTaskPlaceReturnId(chosenTaskPlace.getLatitude(),
                            chosenTaskPlace.getLongitude(), chosenTaskPlace.getAddress());
                    if (newTaskPlaceId == -1)
                        Toast.makeText(TaskEditActivity.this, "Chyba při přidávání nového místa do databáze", Toast.LENGTH_SHORT).show();
                    else {
                        chosenTaskPlace = dm.getTaskPlace((int) newTaskPlaceId);
                        task.setTaskPlaceId((int) newTaskPlaceId);
                    }
                }
            }
        }

        // Ziskani vybraneho seznamu ukolu a dle nej prirazeni ukolu do prislusneho seznamu.
        TaskList taskList = (TaskList) spinTaskLists.getSelectedItem();
        task.setListId(taskList.getId());

        dm.updateTask(task);
        // Informovani uzivatele o uspesnem upraveni ukolu.
        Toast.makeText(TaskEditActivity.this, "Úkol upraven", Toast.LENGTH_SHORT).show();

        // Presmerovani na seznam ukolu, odkud ukol pochazi.
        Intent returnIntent = new Intent();
        returnIntent.putExtra("taskId", task.getId());
        returnIntent.putExtra("listId", taskList.getId());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
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
        Toast.makeText(TaskEditActivity.this, "Úkol smazán", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(TaskEditActivity.this, "Vyskytla se chyba při vytváření souboru fotografie", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(TaskEditActivity.this, "Vyfoť úkol", Toast.LENGTH_SHORT).show();
        return photoFile;
    }

    /**
     * Metoda pro inicializaci layoutu ActionBaru.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_edit_activity_menu, menu);
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
        if (bReady) {
            // Kontrola permission k mikrofonu
            if (ContextCompat.checkSelfPermission(TaskEditActivity.this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(TaskEditActivity.this,
                        Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(TaskEditActivity.this,
                            "Povolení přístupu k mikrofonu je nutné pro nahrání úkolu.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(TaskEditActivity.this,
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
            AudioController.startPlaying(dm, taskId, mediaPlayer, TaskEditActivity.this);
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
                    Toast.makeText(TaskEditActivity.this,
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
                    AudioController.startRecording(task, mediaRecorder, audioManager, dm, TaskEditActivity.this);
                } else {
                    Toast.makeText(TaskEditActivity.this,
                            "Povolení k mikrofonu nebylo uděleno, nelze nahrávat zvuk.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Povoleni udeleno, prejit na PlacePicker
//                    Intent taskDetailIntent = new Intent(TaskEditActivity.this, TaskPlacesMapActivity.class);
//                    startActivityForResult(taskDetailIntent, 778);

                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                    try {
                        startActivityForResult(builder.build(TaskEditActivity.this), REQUEST_PLACE_PICKER);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }


                } else {
                    Toast.makeText(TaskEditActivity.this,
                            "Povolení k GPS nebylo uděleno, nelze zjistit polohu.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSIONS_REQUEST_CURRENT_PLACE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Povoleni udeleno, vyplnit soucasnou pozici
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    // Create a criteria object to retrieve provider
                    Criteria criteria = new Criteria();
                    // Get the name of the best provider
                    String provider = locationManager.getBestProvider(criteria, true);
                    // Get current location
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Location currentLocation = locationManager.getLastKnownLocation(provider);

                    // Ziskani adresy soucasne pozice z coordinates
                    // Oproti tride Geocoder vraci pristup s GeocodingAPI vzdy vysledek
                    requestQueue = Volley.newRequestQueue(TaskEditActivity.this);

                    JsonObjectRequest request = new JsonObjectRequest("https://maps.googleapis.com/maps/api/geocode/json?latlng="
                            + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                            + "&key=AIzaSyC1Vaq8FOHelH58mXhZ3Zn8ksvPbsb9loo", new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String currentPlaceAddress = response.getJSONArray("results").getJSONObject(0)
                                        .getString("formatted_address");
                                etTaskPlace.setText(currentPlaceAddress);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(TaskEditActivity.this, "Volley networking chyba", Toast.LENGTH_SHORT).show();
                        }
                    });

                    requestQueue.add(request);
                } else {
                    Toast.makeText(TaskEditActivity.this,
                            "Povolení k GPS nebylo uděleno, nelze zjistit polohu.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                Toast.makeText(TaskEditActivity.this, "Chyba při vytváření náhledu fotografie", Toast.LENGTH_SHORT).show();
            }

            // Presmerovani na seznam ukolu, odkud ukol pochazi.
            Intent returnIntent = new Intent();
            returnIntent.putExtra("listId", listId);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        // Nacteni adresy vybraneho mista z TaskPlace Pickeru
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(TaskEditActivity.this, data);

                // Poznamenej si, ze misto bylo zmeneno. Udrz si novou instanci
                // pred pripadnym updatem databaze po potvrzeni editace ukolu.
                chosenTaskPlaceChanged = true;
                chosenTaskPlace = new TaskPlace(place.getLatLng().latitude,
                        place.getLatLng().longitude, place.getAddress().toString());
                etTaskPlace.setText(chosenTaskPlace.getAddress());
            }
        }
    }

}


