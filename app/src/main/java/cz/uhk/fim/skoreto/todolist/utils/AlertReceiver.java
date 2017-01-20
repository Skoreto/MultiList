package cz.uhk.fim.skoreto.todolist.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.TaskListsActivity;

/**
 * Reciever pro planovani budoucich notifikaci.
 * Created by Tomas.
 */
public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotification(context, intent.getStringExtra("notifTitle"),
                intent.getStringExtra("notifText"), intent.getStringExtra("notifTicker"),
                intent.getIntExtra("notifId", 1));
    }

    public void createNotification(Context context, String title, String text,
                                   String ticker, int id) {

        // Definuje Intent a akci, kterou s nim provest jinou aplikaci
        // FLAG_UPDATE_CURRENT: Pokud Intent existuje ponechej ho, ale updatuj ho, pokud je potreba
        int requestCode = 0;
        int flag = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode,
                new Intent(context, TaskListsActivity.class), flag);

        // Sestaveni notifikace
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setTicker(ticker)
                .setSmallIcon(R.drawable.ic_event_black_18dp);

        // Definuje Intent, ktery se ma objevit, pokud bylo kliknuto na notifikaci
        notificationBuilder.setContentIntent(pendingIntent);
        // Nastavi defaultni vlastnosti notifikace
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
        // Automaticky zrus notifikaci, pokud je na ni kliknuto v task baru
        notificationBuilder.setAutoCancel(true);

        // Ziskani NotificationManageru, ktery je pouzit k upozorneni uzivatele o udalosti na pozadi
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        // Odesli notifikaci
        notificationManager.notify(id, notificationBuilder.build());
    }

}
