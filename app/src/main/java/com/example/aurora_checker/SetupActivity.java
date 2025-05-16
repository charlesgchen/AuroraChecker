package com.example.aurora_checker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.aurora_checker.R;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AlarmManager alarmManager;
    private PendingIntent alarmPendingIntent;


    // --- Runtime Permission Handling for POST_NOTIFICATIONS (Android 13+) ---
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notification permission denied. Alerts may not work.", Toast.LENGTH_LONG).show();
                }
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted
                Log.d(TAG, "POST_NOTIFICATIONS permission already granted.");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show an educational UI to the user
                Toast.makeText(this, "Please grant notification permission for aurora alerts.", Toast.LENGTH_LONG).show();
                // You might show a dialog here explaining why it's needed.
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Directly request the permission
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    // --- End of Runtime Permission Handling ---

    // --- Runtime Permission Handling for SCHEDULE_EXACT_ALARM (Android 12+) ---
    private void checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please grant permission to schedule exact alarms for timely aurora alerts.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                // intent.setData(Uri.fromParts("package", getPackageName(), null)); // Optional
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ACTION_REQUEST_SCHEDULE_EXACT_ALARM: " + e.getMessage());
                    Toast.makeText(this, "Could not open exact alarm settings.", Toast.LENGTH_SHORT).show();
                }
            } else if (alarmManager != null && alarmManager.canScheduleExactAlarms()){
                Log.d(TAG, "SCHEDULE_EXACT_ALARM permission already granted or not needed.");
            }
        }
    }
    // --- End of Runtime Permission Handling for SCHEDULE_EXACT_ALARM ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_activity); // Assuming you have a layout from the Kotlin example

        // Create notification channel (can also be done in Application class)
        // NotificationHelper.createNotificationChannel(this); // Already done in MyApplication

        // Ask for notification permission on Android 13+
        askNotificationPermission();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        //set alarm every day to check if an aurora is likely
        Instant currentTime = Instant.now();
        Instant nextAlarm = currentTime.plus(1, ChronoUnit.DAYS);


        long triggerTime = nextAlarm.toEpochMilli();
        LocalDateTime ldt = LocalDateTime.ofInstant(currentTime, ZoneId.systemDefault());

        scheduleAuroraAlarm(triggerTime);
        Toast.makeText(this, String.format(Locale.getDefault(),
                        "Aurora check scheduled for %02d:%02d", ldt.getHour(), ldt.getMinute()),
                Toast.LENGTH_SHORT).show();
    }

    public void scheduleAuroraAlarm(long triggerAtMillis) {
        Intent intent = new Intent(this, AuroraNotification.class);
        intent.setAction(AuroraNotification.ACTION_AURORA_ALERT);

        int requestCode = 0; // Use a unique request code if you have multiple alarms
        int pendingIntentFlags;
        pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;


        alarmPendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                pendingIntentFlags
        );

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null, cannot schedule alarm.");
            Toast.makeText(this, "Error: Could not access Alarm Service.", Toast.LENGTH_SHORT).show();
            return;
        }


        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, alarmPendingIntent);
        Log.d(TAG, "Alarm scheduled (pre-S) for " + triggerAtMillis);

    }


    @Override
    protected void onResume() {
        super.onResume();
        // Re-check exact alarm permission if the user was sent to settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null) {
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d(TAG, "Exact alarm permission is granted onResume.");
                } else {
                    Log.d(TAG, "Exact alarm permission is NOT granted onResume.");
                    // You might want to update UI or re-prompt if necessary
                }
            }
        }
    }
}

