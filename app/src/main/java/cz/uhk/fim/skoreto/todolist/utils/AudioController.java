package cz.uhk.fim.skoreto.todolist.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.uhk.fim.skoreto.todolist.model.DataModel;
import cz.uhk.fim.skoreto.todolist.model.Task;

/**
 * Pomocna trida pro implementaci metod nahravani a prehravani zvukovych nahravek.
 * Created by Tomas.
 */
public class AudioController {

    public AudioController() {
    }

    /**
     * Metoda pro spusteni nahravani zvuku.
     */
    public static void startRecording(Task task, MediaRecorder mediaRecorder, AudioManager audioManager, DataModel dm, Context context) {
        // Smazani stare nahravky, pokud je o ni zaznam a pokud jeji soubor existuje.
        if (!task.getRecordName().equals("")) {
            String oldTaskRecordPath = Environment.getExternalStorageDirectory() + "/MultiList/MultiListRecordings/" + task.getRecordName() + ".3gp";
            File oldTaskRecord = new File(oldTaskRecordPath);
            boolean isTaskRecordDeleted = oldTaskRecord.delete();
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        // Vytvor potrebne slozky "Internal storage: /MultiList/MultiListRecordings" pokud neexistuji.
        String folderPath = Environment.getExternalStorageDirectory() + "/MultiList/MultiListRecordings";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            File photosDirectory = new File(folderPath);
            photosDirectory.mkdirs();
        }

        // Vytvor unikatni jmeno nahravky z casu iniciace nahravani ukolu.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String taskRecordName = "nahravka_" + timeStamp;
        String taskRecordPath = Environment.getExternalStorageDirectory() + "/MultiList/MultiListRecordings/" + taskRecordName + ".3gp";

        mediaRecorder.setOutputFile(taskRecordPath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Toast.makeText(context, "CHYBA MediaRecorder nahravani", Toast.LENGTH_SHORT).show();
        }

        mediaRecorder.start();

        // Pridani zaznamu o nahravce do databaze.
        task.setRecordName(taskRecordName);
        dm.updateTask(task);
    }

    /**
     * Metoda pro zastaveni nahravani nahravky.
     */
    public static void stopRecording(MediaRecorder mediaRecorder) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
    }

    /**
     * Metoda pro spusteni prehravani nahravky.
     */
    public static void startPlaying(Task task, MediaPlayer mediaPlayer, Context context) {
        String taskRecordName = task.getRecordName();
        String taskRecordPath = Environment.getExternalStorageDirectory() + "/MultiList/MultiListRecordings/" + taskRecordName + ".3gp";

        try {
            if (!taskRecordName.equals("")) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(taskRecordPath);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } else {
                Toast.makeText(context, "K úkolu neexistuje nahrávka", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(context, "CHYBA MediaPlayer prehravani", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Metoda pro zastaveni prehravani nahravky.
     */
    public static void stopPlaying(MediaPlayer mediaPlayer) {
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer.release();
    }

}
