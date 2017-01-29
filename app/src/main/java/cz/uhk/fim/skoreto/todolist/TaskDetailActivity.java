package cz.uhk.fim.skoreto.todolist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.fragments.DescriptionFragment;
import cz.uhk.fim.skoreto.todolist.fragments.GeneralFragment;
import cz.uhk.fim.skoreto.todolist.fragments.TaskPlaceMapFragment;
import cz.uhk.fim.skoreto.todolist.fragments.WeatherCurrentFragment;
import cz.uhk.fim.skoreto.todolist.fragments.WeatherDailyFragment;
import cz.uhk.fim.skoreto.todolist.fragments.WeatherErrorFragment;
import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskPlace;
import cz.uhk.fim.skoreto.todolist.model.Weather;
import cz.uhk.fim.skoreto.todolist.utils.AudioController;
import cz.uhk.fim.skoreto.todolist.utils.WeatherCurrentForecast;
import cz.uhk.fim.skoreto.todolist.utils.WeatherDailyForecast;
import cz.uhk.fim.skoreto.todolist.utils.WeatherHourForecast;

/**
 * Aktivita pro zobrazeni detailu ukolu.
 * Created by Tomas Skorepa.
 */
public class TaskDetailActivity extends AppCompatActivity {
    private Toolbar tlbEditTaskActivity;
    private ActionBar actionBar;
    private Task task;
    private TextView tvTaskName;

    private DataModel dm;
    private int taskId;
    private int listId;

    public static Weather weatherCurrent, weatherHour, weatherDaily;
    public static int weatherDailyCount;
    public static List<Weather> listWeatherDaily;
    private boolean showCurrentWeather, showDailyWeather = false;
    public static Typeface weatherFont;

    private AudioManager audioManager;
    private static MediaPlayer mediaPlayer;

    private DetailFragmentPagerAdapter detailFragmentPagerAdapter;
    private ViewPager detailViewPager;

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

        tvTaskName = (TextView) findViewById(R.id.tvTaskName);

        Intent anyTaskListIntent = getIntent();
        // Nastaveni listId pro filtraci ukolu v seznamu.
        // Ve vychozim pripade 1 (Inbox) - pokud IntExtra neprijde ze zadneho intentu.
        listId = anyTaskListIntent.getIntExtra("listId", 1);
        // Nastaveni prichozi taskId z TaskListActivity pro ziskani aktualne upravovaneho ukolu.
        taskId = anyTaskListIntent.getIntExtra("taskId", 1);
        task = dm.getTask(taskId);

        tvTaskName.setText(task.getName());

        // POCASI
        // Inicializace fontu pro ikony
        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons.ttf");
        if (task.getTaskPlaceId() != -1) {
            TaskPlace taskPlace = dm.getTaskPlace(task.getTaskPlaceId());
            // Pokud je dostupny nejaky zdroj internetoveho pripojeni
            if (isNetworkAvailable()) {
                // AKTUALNI POCASI
                showCurrentWeather = true;
                weatherCurrent = new Weather();
                WeatherCurrentForecast weatherCurrentForecast = new WeatherCurrentForecast();
                String sLat = String.valueOf(taskPlace.getLatitude());
                String sLong = String.valueOf(taskPlace.getLongitude());
                weatherCurrentForecast.execute("http://api.openweathermap.org/data/2.5/weather?lat="
                        + sLat + "&lon=" + sLong +"&appid=792b095348cf903a77b8ee3f2bc8251e");

                // HODINOVE URCENA PREDPOVED POCASI
                weatherHour = new Weather();
                WeatherHourForecast weatherHourForecast = new WeatherHourForecast();
                weatherHourForecast.execute("http://api.openweathermap.org/data/2.5/forecast?lat="
                        + sLat + "&lon=" + sLong + "&appid=792b095348cf903a77b8ee3f2bc8251e");

                // 7 DENNI PREDPOVED
                if (task.getDueDate() != null) {
                    // Pokud je vyplneno datum splneni
                    // Ziskej dnesni datum v 0:00
                    Calendar calToday = Calendar.getInstance();
                    calToday.set(Calendar.HOUR_OF_DAY, 0);
                    calToday.set(Calendar.MINUTE, 0);
                    calToday.set(Calendar.SECOND, 0);
                    calToday.set(Calendar.MILLISECOND, 0);
                    Date dateToday = calToday.getTime();

                    // Ziskej datum za 7 dni
                    calToday.add(Calendar.DAY_OF_MONTH, 8);
                    Date date7daysAhead = calToday.getTime();

                    if (task.getDueDate().compareTo(dateToday) >= 0
                            && task.getDueDate().compareTo(date7daysAhead) < 0) {
                        // Pokud datum splneni je v rozmezi od dneska az do 7 dni
                        showDailyWeather = true;

                        // Stahni predpoved pocasi pro X dni
                        weatherDailyCount = 9;
                        listWeatherDaily = new ArrayList<Weather>();
                        // Inicializuj si prazdne pocasi pro kazdy den
                        for (int i = 0; i < weatherDailyCount; i++) {
                            listWeatherDaily.add(new Weather());
                        }
                        WeatherDailyForecast weatherDailyForecast = new WeatherDailyForecast();
                        weatherDailyForecast.execute(
                                "http://api.openweathermap.org/data/2.5/forecast/daily?lat="
                                        + sLat + "&lon=" + sLong + "&cnt="
                                        + String.valueOf(weatherDailyCount)
                                        + "&mode=json&appid=792b095348cf903a77b8ee3f2bc8251e");
                        // Zalozni instance pro pripad, kdy by se nenacetla spravna predpoved
                        // k dueDate
                        weatherDaily = listWeatherDaily.get(0);
                    }
                }
            }
        }

        // Komponenta nadpisu tabu
        TabLayout detailTabLayout = (TabLayout) findViewById(R.id.detailTabLayout);
        detailTabLayout.addTab(detailTabLayout.newTab().setText("Obecné"));
        detailTabLayout.addTab(detailTabLayout.newTab().setText("Popis"));
        detailTabLayout.addTab(detailTabLayout.newTab().setText("Mapa"));
        if (showCurrentWeather) {
            detailTabLayout.addTab(detailTabLayout.newTab().setText("Aktuálně"));
        }
//        detailTabLayout.addTab(detailTabLayout.newTab().setText("Hour"));
        if (showDailyWeather) {
            detailTabLayout.addTab(detailTabLayout.newTab().setText("Daily"));
        }
        detailTabLayout.setTabGravity(TabLayout.MODE_SCROLLABLE);

        // Inicializace adapteru FragmentPageru
        detailFragmentPagerAdapter = new DetailFragmentPagerAdapter(
                getSupportFragmentManager(), task, dm, getApplicationContext());
        detailViewPager = (ViewPager)findViewById(R.id.detailViewPagerpager);
        detailViewPager.setAdapter(detailFragmentPagerAdapter);
        // Listener pro prepinani tabu slidovanim stranek ViewPageru
        detailViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(detailTabLayout));

        // Listener pro prepinani stranek klikanim na nadpis tabu
        detailTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                detailViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // PREHRAVANI ZVUKU
        final ToggleButton btnPlayTask = (ToggleButton) findViewById(R.id.btnPlayTask);
        btnPlayTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onPlayPressed(isChecked);
            }
        });
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Metoda pro smazani ukolu.
     * Kaskadne vymaze pripojene fotografie a nahravky z externiho uloziste.
     */
    public void deleteTask() {
        // Smazani stare fotografie, pokud je o ni zaznam a pokud jeji soubor existuje.
        if (!task.getPhotoName().equals("")) {
            String oldTaskPhotoPath = Environment.getExternalStorageDirectory() + "/SmartList/Photos/" + task.getPhotoName() + ".jpg";
            File oldTaskPhoto = new File(oldTaskPhotoPath);
            boolean isTaskPhotoDeleted = oldTaskPhoto.delete();

            // Smazani prislusne miniatury stare fotografie.
            String oldTaskPhotoThumbnailPath = Environment.getExternalStorageDirectory() + "/SmartList/PhotoThumbnails/" + "THUMBNAIL_" + task.getPhotoName() + ".jpg";
            File oldTaskPhotoThumbnail = new File(oldTaskPhotoThumbnailPath);
            boolean isTaskPhotoThumbnailDeleted = oldTaskPhotoThumbnail.delete();
        }

        // Smazani stare nahravky, pokud je o ni zaznam a pokud jeji soubor existuje.
        if (!task.getRecordingName().equals("")) {
            String oldTaskRecordingPath = Environment.getExternalStorageDirectory() + "/SmartList/Recordings/" + task.getRecordingName() + ".3gp";
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
//                if (task.getDueDate() != null) {
//                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
//                    etTaskDueDate.setText(dateFormat.format(task.getDueDate()));
//                } else {
//                    etTaskDueDate.setText("");
//                }
//                etTaskDescription.setText(task.getDescription());

                // Zaskrtnuti checkboxu podle toho zda ukol je/neni splnen.
//                if (task.getCompleted() == 1)
//                    chbTaskCompleted.setChecked(true);
//                if (task.getCompleted() == 0)
//                    chbTaskCompleted.setChecked(false);

                // Pokud bylo vybrano misto ukolu, inicializuj ho
                if (task.getTaskPlaceId() != -1) {
//                    chosenTaskPlace = dm.getTaskPlace(task.getTaskPlaceId());
//                    etTaskPlace.setText(chosenTaskPlace.getAddress());
                }
            }
        }
        // Pokud != RESULT_OK - nedelat nic - dulezite napr. pro tlacitko zpet v dolnim panelu.
    }

    /**
     * Vlastni adapter pro stranky fragmentu v detailu ukolu.
     * Upozorneni: Zda se, ze po zvoleni Tabu je okamzite prednacitan fragment nasledujiciho Tabu.
     */
    public class DetailFragmentPagerAdapter extends FragmentStatePagerAdapter {
        Task task;
        DataModel dm;
        Context context;

        DetailFragmentPagerAdapter(FragmentManager fm, Task task, DataModel dm, Context context) {
            super(fm);
            this.task = task;
            this.dm = dm;
            this.context = context;
        }

        @Override
        public int getCount() {
            if (showDailyWeather)
                return 5;
            else {
                if (showCurrentWeather)
                    return 4;
                else
                    return 3;
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return GeneralFragment.newInstance(task, dm, context);
                case 1:
                    return DescriptionFragment.newInstance(task);
                case 2:
                    return TaskPlaceMapFragment.newInstance(task, dm);
                case 3:
                    // Zobrazit chybovy fragment, pokud server nestihl vratit potrebne informace
                    if (weatherCurrent.getDate() != null)
                        return WeatherCurrentFragment.newInstance(weatherCurrent);
                    else
                        return WeatherErrorFragment.newInstance();
//                case 4:
//                    return WeatherHourFragment.newInstance(weatherHour);
                case 4:
                    // Zobrazit chybovy fragment, pokud server nestihl inicializovat posledni
                    // den pocasi, ktery by jeste mohl byt zvolen k zobrazeni
                    if (listWeatherDaily.get(weatherDailyCount - 1).getDate() != null)
                        return WeatherDailyFragment.newInstance(
                                listWeatherDaily, task, weatherDaily);
                    else
                        return WeatherErrorFragment.newInstance();
                default:
                    return null;
            }
        }

        // Vraci titulek stranky pro horni indikator (nahrazeno TabLayoutem)
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Obecné";
                case 1:
                    return "Popis";
                case 2:
                    return "Mapa";
                case 3:
                    return "Aktuálě";
//                case 4:
//                    return "Hour";
                case 4:
                    return "Daily";
                default:
                    return "Page " + position;
            }
        }
    }

    /**
     * Metoda pro overeni dostupnosti nejakeho pripojeni k Internetu.
     * Negarantuje, ze pripojeni ze strany serveru atd. je opravdu funkcni.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}


