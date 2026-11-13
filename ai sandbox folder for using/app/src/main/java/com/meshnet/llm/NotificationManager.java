package com.meshnet.llm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 * Notification Manager for AI responses and system events
 */
public class NotificationManager {
    private static NotificationManager notificationManager;
    private static Context context;
    private static final String CHANNEL_ID = "llm_response_channel";
    private static final String CHANNEL_NAME = "LLM Response Notifications";

    public static void initialize(Context ctx) {
        context = ctx;
        notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            );
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showResponseReadyNotification(String modelName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Response Ready!")
            .setContentText("Your AI response has been generated. Tap to view.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(new NotificationCompat.BigTextStyle()
                .setBigText("AI response ready for model: " + modelName));

        notificationManager.notify(1, builder.build());
    }

    public static void showLoadingNotification(String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Processing...")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(2, builder.build());
    }

    public static void showErrorNotification(String error) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Error")
            .setContentText(error)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true);

        notificationManager.notify(3, builder.build());
    }
}
