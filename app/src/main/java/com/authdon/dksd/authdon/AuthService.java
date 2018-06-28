package com.authdon.dksd.authdon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
                            ///TODO send back intents.
                            System.out.println(message.getHeader("destination") + ": " + message.getContent());
                        }
                    });
                }
            }
        };
        periodicallyTurnOnLocationData.scheduleWithFixedDelay(reconnectToServer, 1, 1, TimeUnit.MINUTES);
        return ret;
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
