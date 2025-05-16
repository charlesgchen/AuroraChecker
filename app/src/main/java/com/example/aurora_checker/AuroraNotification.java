package com.example.aurora_checker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.content.ContextCompat;

public class AuroraNotification extends BroadcastReceiver {


    public static final String ACTION_AURORA_ALERT = "com.example.aurora_checker.ACTION_AURORA_ALERT";
    private static final String CHANNEL_ID = "aurora_notification_channel";
    private static final int NOTIFICATION_ID = 456;

    @Override
    public void onReceive(Context context, Intent intent) {
        AuroraBackend aurora = new AuroraBackend();
        if (context != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed to show notification (and potentially check conditions)
                    if(aurora.isLikely()) {
                        createNotificationChannel(context);
                        showNotification(context);
                    }
                } else {
                    // Permission not granted, log or handle accordingly (don't show notification)
                    android.util.Log.w("AuroraNotification", "Notification permission not granted");
                }
            } else {
                // On older versions, permission is granted, show notification
                if(aurora.isLikely()) {
                    createNotificationChannel(context);
                    showNotification(context);
                }
            }

            // If you need to fetch data and conditionally show the notification,
            // you would do that here, calling showNotification() based on the data.
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Aurora Notifications";
            String description = "Notifications related to aurora events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted (or on older versions), proceed to show notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon
                    .setContentTitle("Aurora Alert!")
                    .setContentText("An aurora event might be visible!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            // Permission is not granted, log a warning or handle accordingly
            android.util.Log.w("AuroraNotification", "Attempted to show notification without POST_NOTIFICATIONS permission.");
            // You might choose to do nothing here, or potentially try to re-request permission
            // (though as discussed before, requesting from a BroadcastReceiver is not ideal).
        }

    }
}