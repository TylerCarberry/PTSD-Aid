package me.tylercarberry.ptsd;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            // Do something for lollipop and above versions
            Notification noti = new Notification.Builder(context)
                    .setContentTitle("PTSD")
                    .setAutoCancel(true)
                    .setContentText("You haven't checked your symptoms of PTSD recently")
                    .setSmallIcon(R.drawable.ncadd)
                    .setContentIntent(contentIntent)
                            //.setLargeIcon(aBitmap)
                    .build();

            notificationManager.notify(R.string.alarm_service_label, noti);


        }
    }
}