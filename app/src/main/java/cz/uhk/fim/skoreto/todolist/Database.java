package cz.uhk.fim.skoreto.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomas.
 */
public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TODOLIST";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void saveTask(String name, String description){
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", name);
        contentValues.put("DESCRIPTION", description);

        getWritableDatabase().insert("TASKS", null, contentValues);
    }

    public void deleteTask(int id){
        getWritableDatabase().delete("TASKS", "ID=" + id, null);
    }

    public List<String> getAllTasks(){
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

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TASKS (ID INTEGER PRIMARY KEY NOT NULL, NAME TEXT, DESCRIPTION TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE TASKS");
        onCreate(db);
    }
}
