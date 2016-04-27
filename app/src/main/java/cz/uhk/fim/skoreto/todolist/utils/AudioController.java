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
        if (!task.getRecordingName().equals("")) {
            String oldTaskRecordingPath = Environment.getExternalStorageDirectory() + "/MultiList/Recordings/" + task.getRecordingName() + ".3gp";
            File oldTaskRecording = new File(oldTaskRecordingPath);
            boolean isTaskRecordingDeleted = oldTaskRecording.delete();
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        // Vytvor potrebne slozky "Internal storage: /MultiList/MultiListRecordings" pokud neexistuji.
        String folderPath = Environment.getExternalStorageDirectory() + "/MultiList/Recordings";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            File recordingsDirectory = new File(folderPath);
            recordingsDirectory.mkdirs();
        }

        // Vytvor unikatni jmeno nahravky z casu iniciace nahravani ukolu.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String taskRecordingName = "nahravka_" + timeStamp;
        String taskRecordingPath = Environment.getExternalStorageDirectory() + "/MultiList/Recordings/" + taskRecordingName + ".3gp";

        mediaRecorder.setOutputFile(taskRecordingPath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Toast.makeText(context, "CHYBA MediaRecorder nahravani", Toast.LENGTH_SHORT).show();
        }

        mediaRecorder.start();

        // Pridani zaznamu o nahravce do databaze.
        task.setRecordingName(taskRecordingName);
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
    public static void startPlaying(DataModel dm, int taskId, MediaPlayer mediaPlayer, Context context) {
        // Je potreba nacist novou instanci ukolu, protoze uzivatel muze chtit prehrat nove nahranou zvukovou nahravku v jedne aktivite.
        Task task = dm.getTask(taskId);

        String taskRecordingName = task.getRecordingName();
        String taskRecordingPath = Environment.getExternalStorageDirectory() + "/MultiList/Recordings/" + taskRecordingName + ".3gp";

        try {
            if (!taskRecordingName.equals("")) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(taskRecordingPath);
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
