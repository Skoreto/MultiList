package cz.uhk.fim.skoreto.todolist.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tomas.
 */
public class DataModel extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MULTILIST";
    private static final int DATABASE_VERSION = 3;

    public DataModel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Metoda pro ulozeni noveho ukolu do databaze.
     */
    public void addTask(String name, String description, int listId, int completed, String photoName, String recordingName, Date dueDate){
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", name);
        contentValues.put("DESCRIPTION", description);
        contentValues.put("LIST_ID", listId);
        contentValues.put("COMPLETED", completed);
        contentValues.put("PHOTO_NAME", photoName);
        contentValues.put("RECORDING_NAME", recordingName);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String sDueDate = sdf.format(dueDate);
        contentValues.put("DUE_DATE", sDueDate);

        getWritableDatabase().insert("TASKS", null, contentValues);
    }

    /**
     * Metoda pro ulozeni noveho seznamu ukolu do databaze.
     */
    public void addTaskList(String name){
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", name);

        getWritableDatabase().insert("TASK_LISTS", null, contentValues);
    }

    /**
     * Metoda pro zmenu ukolu v databazi.
     * Vraci pocet aktualizovanych zaznamu.
     */
    public int updateTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", task.getName());
        contentValues.put("DESCRIPTION", task.getDescription());
        contentValues.put("LIST_ID", task.getListId());
        contentValues.put("COMPLETED", task.getCompleted());
        contentValues.put("PHOTO_NAME", task.getPhotoName());
        contentValues.put("RECORDING_NAME", task.getRecordingName());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String sDueDate = sdf.format(task.getDueDate());
        contentValues.put("DUE_DATE", sDueDate);

        return db.update("TASKS", contentValues, "ID = ?",  new String[] {String.valueOf(task.getId())});
    }

    /**
     * Metoda pro zmenu seznamu ukolu v databazi.
     * Vraci pocet aktualizovanych zaznamu.
     */
    public int updateTaskList(TaskList taskList){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", taskList.getName());

        return db.update("TASK_LISTS", contentValues, "ID = ?",  new String[] {String.valueOf(taskList.getId())});
    }

    /**
     * Metoda pro smazani ukolu z databaze.
     */
    public void deleteTask(int id){
        getWritableDatabase().delete("TASKS", "ID=" + id, null);
    }

    /**
     * Metoda pro smazani seznamu ukolu z databaze.
     */
    public void deleteTaskList(int id){
        getWritableDatabase().delete("TASK_LISTS", "ID=" + id, null);
    }

    /**
     * Metoda vrati konkretni ukol podle zadaneho id.
     * Ukol je vracen jako objekt typu HashMap.
     */
    public HashMap<String, String> getTaskHashMap(String id){
        HashMap<String, String> hm = new HashMap<String, String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE ID=" + id, null);

        if (cursor.moveToFirst()){
            do {
                hm.put("NAME", cursor.getString(1));
                hm.put("DESCRIPTION", cursor.getString(2));
                hm.put("LIST_ID", cursor.getString(3));
            } while (cursor.moveToNext());
        }
        return hm;
    }

    /**
     * Metoda pro vraceni konkretniho ukolu (dle id) z databaze.
     */
    public Task getTask(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE ID=" + id, null);
        Task task = new Task();
        if (cursor.moveToFirst()){
            do {
                int taskId = cursor.getInt(0);
                String name = cursor.getString(1);
                String description = cursor.getString(2);
                int listId = cursor.getInt(3);
                int completed = cursor.getInt(4);
                String photoName = cursor.getString(5);
                String recordingName = cursor.getString(6);
                String sDueDate = cursor.getString(7);

                task.setId(taskId);
                task.setName(name);
                task.setDescription(description);
                task.setListId(listId);
                task.setCompleted(completed);
                task.setPhotoName(photoName);
                task.setRecordingName(recordingName);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dueDate = null;
                try {
                    dueDate = sdf.parse(sDueDate);
                } catch (ParseException e) {
                    Log.e("Parsovani datumu", "Nepodarilo se naparsovat datum u metody getTask");
                }
                task.setDueDate(dueDate);

            } while (cursor.moveToNext());
        }
        return task;
    }

    /**
     * Metoda pro vraceni konkretniho seznamu ukolu (dle id) z databaze.
     */
    public TaskList getTaskListById(int taskListId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASK_LISTS WHERE ID=" + taskListId, null);
        TaskList taskList = new TaskList();
        if (cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);

                taskList.setId(id);
                taskList.setName(name);
            } while (cursor.moveToNext());
        }
        return taskList;
    }

    /**
     * Metoda vraci seznam vsech nazvu ukolu v databazi.
     */
    public List<String> getNameAllTasks(){
        List<String> tasks = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM TASKS", null);

        if (cursor.moveToFirst()){
            do {
                String task = cursor.getInt(0) + ". " + cursor.getString(1);
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        return tasks;
    }

    /**
     * Metoda vraci seznam vsech ukolu v databazi.
     */
    public ArrayList<Task> getAllTasks(){
        ArrayList<Task> tasks = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM TASKS", null);

        if (cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String description = cursor.getString(2);
                int listId = cursor.getInt(3);
                int completed = cursor.getInt(4);
                String photoName = cursor.getString(5);
                String recordingName = cursor.getString(6);
                String sDueDate = cursor.getString(7);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dueDate = null;
                try {
                    dueDate = sdf.parse(sDueDate);
                } catch (ParseException e) {
                    Log.e("Parsovani datumu", "Nepodarilo se naparsovat datum u metody getAllTasks");
                }

                Task task = new Task(id, name, description, listId, completed, photoName, recordingName, dueDate);
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        return tasks;
    }

    /**
     * Metoda vraci seznam vsech ukolu ve vybranem seznamu ukolu identifikovanem pomoci listId.
     */
    public ArrayList<Task> getTasksByListId(int listId, boolean orderAscendingDueDate){
        ArrayList<Task> tasks = new ArrayList<>();

        // Razeni dle data splneni vzestupne/sestupne.
        String chosenOrder;
        if (orderAscendingDueDate == true) {
            chosenOrder = "ASC";
        } else {
            chosenOrder = "DESC";
        }

        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM TASKS WHERE LIST_ID=" + listId + " ORDER BY COMPLETED, DUE_DATE " + chosenOrder, null);

        if (cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String description = cursor.getString(2);
                int completed = cursor.getInt(4);
                String photoName = cursor.getString(5);
                String recordingName = cursor.getString(6);
                String sDueDate = cursor.getString(7);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dueDate = null;
                try {
                    dueDate = sdf.parse(sDueDate);
                } catch (ParseException e) {
                    Log.e("Parsovani datumu", "Nepodarilo se naparsovat datum u metody getTasksByListId");
                }

                Task task = new Task(id, name, description, listId, completed, photoName, recordingName, dueDate);
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        return tasks;
    }

    /**
     * Metoda vraci seznam vsech ukolu ve vybranem seznamu ukolu identifikovanem pomoci listId.
     */
    public ArrayList<Task> getIncompletedTasksByListId(int listId, boolean orderAscendingDueDate){
        ArrayList<Task> tasks = new ArrayList<>();

        // Razeni dle data splneni vzestupne/sestupne.
        String chosenOrder;
        if (orderAscendingDueDate == true) {
            chosenOrder = "ASC";
        } else {
            chosenOrder = "DESC";
        }

        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM TASKS WHERE LIST_ID=" + listId + " AND COMPLETED=0" + " ORDER BY DUE_DATE " + chosenOrder, null);

        if (cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String description = cursor.getString(2);
                int completed = cursor.getInt(4);
                String photoName = cursor.getString(5);
                String recordingName = cursor.getString(6);
                String sDueDate = cursor.getString(7);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dueDate = null;
                try {
                    dueDate = sdf.parse(sDueDate);
                } catch (ParseException e) {
                    Log.e("Parsovani datumu", "Nepodarilo se naparsovat datum u metody getTasksByListId");
                }

                Task task = new Task(id, name, description, listId, completed, photoName, recordingName, dueDate);
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        return tasks;
    }

    /**
     * Metoda vraci seznam vsech seznamu ukolu v databazi.
     */
    public ArrayList<TaskList> getAllTaskLists(){
        ArrayList<TaskList> taskLists = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM TASK_LISTS", null);

        if (cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);

                TaskList taskList = new TaskList(id, name);
                taskLists.add(taskList);
            } while (cursor.moveToNext());
        }
        return taskLists;
    }

    /**
     * Metoda se vola v pripade, ze databazove objekty jeste neexistuji a je potreba je vytvorit.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TASKS (ID INTEGER PRIMARY KEY NOT NULL, NAME TEXT, DESCRIPTION TEXT, LIST_ID INTEGER, COMPLETED INTEGER, PHOTO_NAME TEXT, RECORDING_NAME TEXT, DUE_DATE TEXT)");
        db.execSQL("CREATE TABLE TASK_LISTS (ID INTEGER PRIMARY KEY NOT NULL, NAME TEXT)");

        // Pocatecni inicializace - vychozi vytvoreni seznamu Inbox - ziska ID 1.
        db.execSQL("INSERT INTO TASK_LISTS VALUES(null, ?)", new Object[] {"Inbox"});
    }

    /**
     * Metoda se vola, pokud je verze databaze (atribut DATABASE_VERSION) starsi nez hodnota
     * v parametrech konstruktoru rodicovske tridy SQLiteOpenHelper.
     * Vola se pri aktualizaci aplikace, ktera meni i navrhovou strukturu databazovych objektu.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TASKS");
        db.execSQL("DROP TABLE IF EXISTS TASK_LISTS");
        onCreate(db);
    }
}
