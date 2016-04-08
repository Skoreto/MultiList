package cz.uhk.fim.skoreto.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tomas.
 */
public class DataModel extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TODOLIST";
    private static final int DATABASE_VERSION = 5;

    public DataModel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Metoda pro ulozeni noveho ukolu do databaze.
     */
    public void saveTask(String name, String description, int listId, int completed){
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", name);
        contentValues.put("DESCRIPTION", description);
        contentValues.put("LIST_ID", listId);
        contentValues.put("COMPLETED", completed);

        getWritableDatabase().insert("TASKS", null, contentValues);
    }

    /**
     * Metoda pro zmenu ukolu v databazi.
     * Vraci pocet aktualizovanych zaznamu.
     */
    public int updateTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contVal = new ContentValues();
        contVal.put("NAME", task.getName());
        contVal.put("DESCRIPTION", task.getDescription());
        contVal.put("LIST_ID", task.getListId());
        contVal.put("COMPLETED", task.getCompleted());

        return db.update("TASKS", contVal, "ID = ?",  new String[] {String.valueOf(task.getId())});
    }

    /**
     * Metoda pro smazani ukolu z databaze.
     */
    public void deleteTask(int id){
        getWritableDatabase().delete("TASKS", "ID=" + id, null);
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
    public Task getTask(int id){
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

                task.setId(taskId);
                task.setName(name);
                task.setDescription(description);
                task.setListId(listId);
                task.setCompleted(completed);
            } while (cursor.moveToNext());
        }
        return task;
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

                Task task = new Task(id, name, description, listId, completed);
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        return tasks;
    }

    /**
     * Metoda se vola v pripade, ze databazove objekty jeste neexistuji a je potreba je vytvorit.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TASKS (ID INTEGER PRIMARY KEY NOT NULL, NAME TEXT, DESCRIPTION TEXT, LIST_ID INTEGER, COMPLETED INTEGER)");
    }

    /**
     * Metoda se vola, pokud je verze databaze (atribut DATABASE_VERSION) starsi nez hodnota
     * v parametrech konstruktoru rodicovske tridy SQLiteOpenHelper.
     * Vola se pri aktualizaci aplikace, ktera meni i navrhovou strukturu databazovych objektu.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TASKS");
        onCreate(db);
    }
}
