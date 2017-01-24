package cz.uhk.fim.skoreto.todolist.utils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import cz.uhk.fim.skoreto.todolist.R;
import cz.uhk.fim.skoreto.todolist.TaskListsActivity;

/**
 * Service pro obsluhu notifikaci na udalosti typu Geofence.
 * Created by Tomas.
 */
public class GeofenceTransitionService extends IntentService {

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();
    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    public GeofenceTransitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Znovuziskani Geofencing intentu
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Ziskej predane extras pro nastaveni parametru notifikace
        String notifTitle = intent.getStringExtra("notifTitle");
        String notifText = intent.getStringExtra("notifText");
        String notifTicker = intent.getStringExtra("notifTicker");

        // Reseni chyb
        if (geofencingEvent.hasError()) {
            // Zaznamenej popis chyby do logu
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }

        // Ziskej informaci o GeofenceTrasition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Pokud je nastavena transition pro vstup ci vystup z okoli mista ukolu
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Ziskej seznam geofence, ktere vyvolaly notifikaci v danem miste
            List<Geofence> listTriggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Vytvor detailni zpravu o vstupu/vystupu z o okoli mist kterych Geofencu
            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition,
                    listTriggeringGeofences);
            // Odesli detaily o notifikaci jako String a pripoj predana extras z intentu
            sendNotification(geofenceTransitionDetails, notifTitle, notifText, notifTicker);
        }
    }

    /**
     * Metoda vytvori detailni zpravu o obdrzenem Geofence.
     * Ve zprave rozlisi, jestli uzivatel vstupuje/opousti radius mista ukolu.
     */
    private String getGeofenceTrasitionDetails(int geoFenceTransition,
                                               List<Geofence> triggeringGeofences) {
        // Ziskej ID kazdeho geofence, ktery vyvolal notifikaci v danem miste
        ArrayList<String> listTriggeringGeofencesIds = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            listTriggeringGeofencesIds.add(geofence.getRequestId());
        }

        String transitionMessage = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            transitionMessage = "Vstupujete do okolí místa";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            transitionMessage = "Opouštíte okolí místa";

        // Pripoj ID (nazvy) geofence, ktere vyvolaly notifikaci v danem miste
        return transitionMessage + TextUtils.join(", ", listTriggeringGeofencesIds);
    }

    /**
     * Metoda pro sestaveni a zaslani vysledne notifikace
     * @param geofenceTransitionDetails Zprava, zda uzivatel vstupuje/vystupuje z okoli mista ukolu.
     */
    private void sendNotification(String geofenceTransitionDetails, String notifTitle,
                                  String notifText, String notifTicker) {
//        // Intent pro start hlavni TaskListsActivity
//        Intent notificationIntent = TaskListsActivity.makeNotificationIntent(
//                getApplicationContext(), geofenceTransitionDetails
//        );

        // Intent vytvoreny odle alertIntent
        Intent notificationIntent = new Intent(this, AlertReceiver.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TaskListsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Sestaveni notifikace
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_event_black_18dp)
                .setColor(Color.RED)
                .setContentTitle("Okolí úkolu: " + notifTitle)
                .setContentText(geofenceTransitionDetails + " " + notifText)
                .setTicker(notifTicker)
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE
                        | Notification.DEFAULT_SOUND)
                // Automaticky zrus notifikaci, pokud je na ni kliknuto v task baru
                .setAutoCancel(true);


        // Ziskani NotificationManageru, ktery je pouzit k upozorneni uzivatele o udalosti na pozadi
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Odesli notifikaci
        notificatioMng.notify(GEOFENCE_NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Pomocna metoda pro logovani Geofence chyb.
     */
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence neni dostupny";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Prilis mnoho Geofence";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Prilis mnoho pending intentu (Geofence)";
            default:
                return "Neznama chyba Geofence";
        }
    }
}
