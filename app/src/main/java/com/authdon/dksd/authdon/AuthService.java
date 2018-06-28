package com.authdon.dksd.authdon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.cycling.science.trainwithscience.elevation.DistanceProvider;
import com.cycling.science.trainwithscience.elevation.DistanceProviderImpl;
import com.cycling.science.trainwithscience.elevation.SimpleHistoryElevationProvider;
import com.cycling.science.trainwithscience.elevation.SlopeCalculator;
import com.cycling.science.trainwithscience.feature.AppFeature;
import com.cycling.science.trainwithscience.feature.LocationFeature;
import com.cycling.science.trainwithscience.logging.ALoggerFactory;
import com.cycling.science.trainwithscience.logging.ALoggerFactoryImpl;
import com.cycling.science.trainwithscience.writer.CsvDataWriter;
import com.cycling.science.trainwithscience.writer.DataWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by dscottdawkins on 5/3/17.
 */

public class AuthService extends Service {

    private PushService pushService = new PushService();
    private ScheduledExecutorService periodicallyTurnOnLocationData = Executors.newSingleThreadScheduledExecutor();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);

        Runnable reconnectToServer = new Runnable() {
            @Override
            public void run() {
                if (pushService.isConnected()) {
                    pushService.startListening(new StompMessageListener() {
                        @Override
                        public void onMessage(StompMessage message) {
                            ///TODO send back notification intents.
                            System.out.println(message.getHeader("destination") + ": " + message.getContent());
                        }
                    });
                }
            }
        };
        periodicallyTurnOnLocationData.scheduleWithFixedDelay(reconnectToServer, 1, 1, TimeUnit.MINUTES);
        return ret;
    }

    private void sendNotification(String message, String title) {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Intent secondActivityIntent = new Intent(this, SecondActivity.class);
            PendingIntent secondActivityPendingIntent = PendingIntent.getActivity(this, 0 , secondActivityIntent, PendingIntent.FLAG_ONE_SHOT);

            Intent thirdActivityIntent = new Intent(this, ThridActivity.class);
            PendingIntent thirdActivityPendingIntent = PendingIntent.getActivity(this, 0 , thirdActivityIntent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_3d_rotation_white_36dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .addAction(R.drawable.ic_lock_open_cyan_600_24dp,"Login", secondActivityPendingIntent)
                    .addAction(R.drawable.ic_lock_pink_700_24dp,"Register", thirdActivityPendingIntent)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
