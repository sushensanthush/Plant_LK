package lk.susa.plant_lk.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import lk.susa.plant_lk.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "login_channel";

    private static boolean isNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("plant_lk_prefs", Context.MODE_PRIVATE);
        return !prefs.getBoolean("notifications_enabled", true);
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public static void showLoginNotification(Context context, String username) {
        if (isNotificationsEnabled(context)) return;

        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Login Successful")
                .setContentText("Welcome " + username)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(1, builder.build());
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public static void showLogoutNotification(Context context, String username) {
        if (isNotificationsEnabled(context)) return;

        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Logout Successful")
                .setContentText("Goodbye " + username)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(2, builder.build());
    }

    public static void showInfoNotification(Context context, int id, String title, String text) {
        SharedPreferences prefs = context.getSharedPreferences("plant_lk_prefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("notifications_enabled", true)) return;
        if (Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "system_channel", "System Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "system_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(id, builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Login Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}