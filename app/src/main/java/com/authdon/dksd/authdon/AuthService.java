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

    private ALoggerFactory aLoggerFactory = new ALoggerFactoryImpl();
    private LocationFeature locationFeature;
    private ScheduledExecutorService periodicallyTurnOnLocationData = Executors.newSingleThreadScheduledExecutor();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);

        final ALoggerFactory aLoggerFactory = new ALoggerFactoryImpl();

        TimeProvider timeProvider = new TimeProviderImpl();
        DataWriter csvDataWriter = new CsvDataWriter("CsvDataWriter", this, getFilesDir(), timeProvider, aLoggerFactory);

        DistanceProvider distanceProvider = new DistanceProviderImpl();
        SlopeCalculator simpleHistoryElevationSlopeCalculator = new SimpleHistoryElevationProvider(distanceProvider);
        locationFeature = new LocationFeature("LocationFeature", this, csvDataWriter, simpleHistoryElevationSlopeCalculator, aLoggerFactory);
        Collection<AppFeature> features = new ArrayList<>();
        features.add(locationFeature);

        Runnable checkForMovement = new Runnable() {
            @Override
            public void run() {
                if (locationFeature.getMotionDetected()) {
                    aLoggerFactory.getLogger("Sensor").info("CHKMov", "Motion underway, continuing");
                    return;
                }
                if (locationFeature.getRequestedUpdates()) {
                    locationFeature.deregisterFromUpdates();
                    aLoggerFactory.getLogger("Sensor").info("CHKMov", "De-Registering for location updates");
                    return;
                }
                locationFeature.registerForUpdates();
                aLoggerFactory.getLogger("Sensor").info("CHKMov", "Registering for location updates");
            }
        };
        periodicallyTurnOnLocationData.scheduleWithFixedDelay(checkForMovement, 1, 1, TimeUnit.MINUTES);
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
