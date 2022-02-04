package com.stardust.autojs.core.image.capture;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.stardust.autojs.R;

public class CaptureForegroundService extends Service {

    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = CaptureForegroundService.class.getName() + ".foreground";
    private final String TAG = "CaptureForegrdService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        startForeground(NOTIFICATION_ID, buildNotification());
        GlobalScreenCapture.getInstance().notifyStarted();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, buildNotification());
        GlobalScreenCapture.getInstance().notifyStarted();
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? FLAG_IMMUTABLE : 0;
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ScreenCaptureRequestActivity.class), flags);
        Log.d(TAG, "buildNotification: start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Recording", NotificationManager.IMPORTANCE_MIN);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Recording")
                .setContentText("本通知为截图权限需要")
                .setSmallIcon(R.drawable.autojs_material)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setChannelId(CHANNEL_ID)
                .setVibrate(new long[0])
                .setDefaults(Notification.DEFAULT_ALL | NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        CharSequence name = "Recoding";
        String description = "Recoding";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        channel.enableLights(false);
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalScreenCapture.getInstance().foregroundServiceDown();
        stopForeground(true);
    }
}
