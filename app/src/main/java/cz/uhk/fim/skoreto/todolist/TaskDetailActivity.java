package cz.uhk.fim.skoreto.todolist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;
import cz.uhk.fim.skoreto.todolist.model.TaskPlace;
import cz.uhk.fim.skoreto.todolist.model.Weather;
import cz.uhk.fim.skoreto.todolist.utils.AudioController;
import cz.uhk.fim.skoreto.todolist.utils.WeatherDownload;

/**
 * Aktivita pro zobrazeni detailu ukolu.
 * Created by Tomas Skorepa.
 */
public class TaskDetailActivity extends AppCompatActivity {
    private Toolbar tlbEditTaskActivity;
    private ActionBar actionBar;
    private Task task;
    private TextView tvTaskName;
//    private ImageView ivTaskPhoto;

    private DataModel dm;
    private int taskId;
    private int listId;
    public static Weather weather;

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
//        ivTaskPhoto = (ImageView) findViewById(R.id.ivTaskPhoto);

        Intent anyTaskListIntent = getIntent();
        // Nastaveni listId pro filtraci ukolu v seznamu.
        // Ve vychozim pripade 1 (Inbox) - pokud IntExtra neprijde ze zadneho intentu.
        listId = anyTaskListIntent.getIntExtra("listId", 1);
        // Nastaveni prichozi taskId z TaskListActivity pro ziskani aktualne upravovaneho ukolu.
        taskId = anyTaskListIntent.getIntExtra("taskId", 1);
        task = dm.getTask(taskId);

        tvTaskName.setText(task.getName());

        // POCASI
        if (task.getTaskPlaceId() != -1) {
            weather = new Weather();
            TaskPlace taskPlace = dm.getTaskPlace(task.getTaskPlaceId());
            WeatherDownload weatherDownload = new WeatherDownload();
            String sLat = String.valueOf(taskPlace.getLatitude());
            String sLong = String.valueOf(taskPlace.getLongitude());
            weatherDownload.execute("http://api.openweathermap.org/data/2.5/weather?lat=" +
                    sLat + "&lon="+ sLong +"&appid=792b095348cf903a77b8ee3f2bc8251e");
        }

//        if (!task.getPhotoName().equals("")) {
//            // Prime prirazeni nahledu fotografie do ImageView.
//            String photoThumbnailPath = Environment.getExternalStorageDirectory() + "/MultiList/PhotoThumbnails/" + "THUMBNAIL_" + task.getPhotoName() + ".jpg";
//            final String photoPath = Environment.getExternalStorageDirectory() + "/MultiList/Photos/" + task.getPhotoName() + ".jpg";
//            ivTaskPhoto.setImageBitmap(BitmapFactory.decodeFile(photoThumbnailPath));
//
//            ivTaskPhoto.setOnClickListener(new View.OnClickListener() {
//                   @Override
//                   public void onClick(View view) {
//                       // Zobrazeni velke fotografie po kliknuti na nahled.
//                       Intent sendPhotoDirectoryIntent = new Intent(TaskDetailActivity.this, SinglePhotoActivity.class);
//                       sendPhotoDirectoryIntent.putExtra("photoPath", photoPath);
//                       startActivity(sendPhotoDirectoryIntent);
//                   }
//               }
//            );
//        }

        // Komponenta nadpisu tabu
        TabLayout detailTabLayout = (TabLayout) findViewById(R.id.detailTabLayout);
        detailTabLayout.addTab(detailTabLayout.newTab().setText("Obecné"));
        detailTabLayout.addTab(detailTabLayout.newTab().setText("Popis"));
        detailTabLayout.addTab(detailTabLayout.newTab().setText("Mapa"));
        detailTabLayout.addTab(detailTabLayout.newTab().setText("Počasí"));
//        detailTabLayout.addTab(detailTabLayout.newTab().setText("List"));
        detailTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

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
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
//                    GeneralFragment generalFragment = new GeneralFragment();
//                    return (Fragment) generalFragment.newInstance(position, task, dm, context);

//                    GeneralFragment f = GeneralFragment.newInstance(task, dm, context);
//                    ImageView ivTaskPhoto = (ImageView) findViewById(R.id.ivTaskPhoto);
                    return GeneralFragment.newInstance(task, dm, context);
                case 1:
                    return DescriptionFragment.newInstance(task);
                case 2:
                    return TaskPlaceMapFragment.newInstance(task, dm);
                case 3:
                    return WeatherFragment.newInstance(weather);
//                case 4:
//                    return ArrayListFragment.newInstance(position);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator.
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
                    return "Počasí";
//                case 4:
//                    return "List";
                default:
                    return "Page " + position;
            }
        }
    }

    /**
     * Fragment obecnych informaci o ukolu.
     */
    public static class GeneralFragment extends Fragment {
        private TextView tvTaskPlace;
        private TextView tvTaskDueDate;
        private TextView tvAssignedTaskList;
        private CheckBox chbTaskCompleted;
//        private ImageView ivTaskPhoto;

//        @Override
//        public void onActivityCreated(Bundle savedInstanceState) {
//            super.onActivityCreated(savedInstanceState);
//            Context context = getActivity();
//        }

//        @Override
//        public void onAttach(Context context) {
//            super.onAttach(context);
//
//            Activity activity;
//            OnArticleSelectedListener mListener;
//
//            if (context instanceof Activity){
//                activity=(Activity) context;
//
//                try {
//                    mListener = (OnArticleSelectedListener) activity;
//                } catch (ClassCastException e) {
//                    throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
//                }
//            }

//        }

        static GeneralFragment newInstance(Task task, DataModel dm, Context context) {
            GeneralFragment f = new GeneralFragment();
            String taskPlaceAddress = "";
            // Pokud bylo vybrano misto ukolu, inicializuj ho
            if (task.getTaskPlaceId() != -1) {
                TaskPlace chosenTaskPlace = dm.getTaskPlace(task.getTaskPlaceId());
                taskPlaceAddress = chosenTaskPlace.getAddress();
            }

            String taskDueDate = "";
            if (task.getDueDate() != null) {
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
                taskDueDate = dateFormat.format(task.getDueDate());
            }

            Bundle args = new Bundle();
            args.putString("taskPlaceAddress", taskPlaceAddress);
            args.putString("taskDueDate", taskDueDate);
            args.putString("tvAssignedTaskListName",
                    dm.getTaskListById(task.getListId()).getName());
            args.putInt("isTaskCompleted", task.getCompleted());
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_pager_general, container, false);

            tvTaskPlace = (TextView) view.findViewById(R.id.tvTaskPlace);
            tvTaskPlace.setText(getArguments().getString("taskPlaceAddress"));
            tvTaskDueDate = (TextView) view.findViewById(R.id.tvTaskDueDate);
            tvTaskDueDate.setText(getArguments().getString("taskDueDate"));
            tvAssignedTaskList = (TextView) view.findViewById(R.id.tvAssignedTaskList);
            tvAssignedTaskList.setText(getArguments().getString("tvAssignedTaskListName"));
            chbTaskCompleted = (CheckBox) view.findViewById(R.id.chbTaskCompleted);
//            ivTaskPhoto = (ImageView) view.findViewById(R.id.ivTaskPhoto);

            // Zaskrtnuti checkboxu podle toho zda ukol je/neni splnen.
            if (getArguments().getInt("isTaskCompleted") == 1)
                chbTaskCompleted.setChecked(true);
            if (getArguments().getInt("isTaskCompleted") == 0)
                chbTaskCompleted.setChecked(false);

            return view;
        }
    }

    /**
     * Fragment blizsiho popisu ukolu.
     */
    public static class DescriptionFragment extends Fragment {
        private TextView tvTaskDescription;

        static DescriptionFragment newInstance(Task task) {
            DescriptionFragment f = new DescriptionFragment();
            Bundle args = new Bundle();
            args.putString("taskDescription", task.getDescription());
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_pager_description, container, false);
            tvTaskDescription = (TextView) view.findViewById(R.id.tvTaskDescription);
            tvTaskDescription.setText(getArguments().getString("taskDescription"));
            return view;
        }
    }

    /**
     * Fragment umisteni a radiusu ukolu.
     */
    public static class TaskPlaceMapFragment extends Fragment {
        private GoogleMap gMap;
        MapView mapView;

        static TaskPlaceMapFragment newInstance(Task task, DataModel dm) {
            TaskPlaceMapFragment f = new TaskPlaceMapFragment();
            Bundle args = new Bundle();
            boolean isTaskPlaceFilled = false;
            if (task.getTaskPlaceId() != -1) {
                // Pokud je vyplneno misto ukolu predej Lat Long
                isTaskPlaceFilled = true;
                TaskPlace taskPlace = dm.getTaskPlace(task.getTaskPlaceId());
                args.putFloat("taskPlaceLat", (float) taskPlace.getLatitude());
                args.putFloat("taskPlaceLong", (float) taskPlace.getLongitude());
                args.putInt("taskPlaceRadius", taskPlace.getRadius());
            }
            args.putBoolean("isTaskPlaceFilled", isTaskPlaceFilled);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_pager_map, container, false);
            mapView = (MapView) view.findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);

            // Inicializace GoogleMap z MapView
            gMap = mapView.getMap();
            gMap.getUiSettings().setMyLocationButtonEnabled(false);
//            gMap.setMyLocationEnabled(true);

            if (getArguments().getBoolean("isTaskPlaceFilled")) {
                // Pokud je vyplneno misto ukolu - vyznac jej na mape
                float taskPlaceLatitude = getArguments().getFloat("taskPlaceLat");
                float taskPlaceLongitude = getArguments().getFloat("taskPlaceLong");
                int taskPlaceRadius = getArguments().getInt("taskPlaceRadius");
                gMap.addMarker(new MarkerOptions().position(
                        new LatLng(taskPlaceLatitude, taskPlaceLongitude)));
                // Radius specifikovan v metrech by mel byt 0 nebo vetsi
                gMap.addCircle(new CircleOptions()
                        .center(new LatLng(taskPlaceLatitude, taskPlaceLongitude))
                        .radius(taskPlaceRadius)
                        .strokeColor(Color.RED).strokeWidth(7));

                // Nutne zavolat MapsInitializer pred volanim CameraUpdateFactory
                MapsInitializer.initialize(this.getActivity());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(taskPlaceLatitude, taskPlaceLongitude), 11);
                gMap.animateCamera(cameraUpdate);
            }

            return view;
        }

        @Override
        public void onResume() {
            mapView.onResume();
            super.onResume();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mapView.onDestroy();
        }

        @Override
        public void onLowMemory() {
            super.onLowMemory();
            mapView.onLowMemory();
        }

    }

    /**
     * Fragment predpovedi pocasi.
     */
    public static class WeatherFragment extends Fragment {
        private ImageView ivMainIcon;
        private TextView tvTemp;
        private TextView tvMain;
        private TextView tvPressure;
        private TextView tvWind;


        static WeatherFragment newInstance(Weather weather) {
            WeatherFragment f = new WeatherFragment();
            Bundle args = new Bundle();
            args.putString("icon", weather.getIcon());
            args.putDouble("temp", weather.getTemp());
            args.putString("main", weather.getMain());
            args.putDouble("pressure", weather.getPressure());
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_pager_weather, container, false);
            Bundle args = getArguments();

            ivMainIcon = (ImageView) view.findViewById(R.id.ivMainIcon);
            String icon = args.getString("icon");
            String iconImage = String.format("http://openweathermap.org/img/w/%s.png", icon);
            Picasso.with(getContext()).load(iconImage).into(ivMainIcon);

            tvTemp = (TextView) view.findViewById(R.id.tvTemp);
            tvTemp.setText(
                    String.format("%.1f", args.getDouble("temp")) + " °C");
            tvMain = (TextView) view.findViewById(R.id.tvMain);
            tvMain.setText(args.getString("main"));
            tvPressure = (TextView) view.findViewById(R.id.tvPressure);
            tvPressure.setText(
                    String.format("%.0f", args.getDouble("pressure")) + " hPa");
            tvWind = (TextView) view.findViewById(R.id.tvWind);


//            ivPressure.setImageResource(R.drawable.ic_event_black_18dp);

            return view;
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


}


