package com.example.janganlupasholat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AdzanWorker extends Worker {

    private static final String CHANNEL_ID = "ADZAN_CHANNEL";

    public AdzanWorker(@NonNull Context context,
                       @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        String title = getInputData().getString("title");
        String message = getInputData().getString("message");

        showNotification(title, message);

        return Result.success();
    }

    private void showNotification(String title, String message) {

        NotificationManager manager =
                (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notifikasi Adzan",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Pengingat waktu sholat");

            // gunakan suara adzan dari raw
            channel.setSound(
                    android.net.Uri.parse("android.resource://" +
                            getApplicationContext().getPackageName() +
                            "/" + R.raw.adzan),
                    new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
            );

            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
