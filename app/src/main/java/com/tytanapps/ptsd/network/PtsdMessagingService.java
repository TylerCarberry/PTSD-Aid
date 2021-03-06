package com.tytanapps.ptsd.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.R;

import timber.log.Timber;

/**
 * Handles messages received by Firebase Messaging Service
 */
public class PtsdMessagingService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = 5000;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Timber.d("onMessageReceived() called with: remoteMessage = [%s]", remoteMessage);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notification_action", "unsubscribe");
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .addAction(R.drawable.ic_notifications_off_black_24px, getString(R.string.unsubscribe_news_notifications), pIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message)
                                .setBigContentTitle(title))
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setContentText(message);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("fragment", "news");

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }
}
