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
    private static final int DATABASE_VERSION = 4;

    public Database(Context context) {
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

    public void deleteTask(int id){
        getWritableDatabase().delete("TASKS", "ID=" + id, null);
    }

    /**
     * Metoda vraci seznam vsech ukolu v databazi.
     */
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
