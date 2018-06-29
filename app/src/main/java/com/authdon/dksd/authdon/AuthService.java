package com.authdon.dksd.authdon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by dscottdawkins on 5/3/17.
 */

public class AuthService extends Service {

    private PushService pushService = new PushService();
    private RegisterUserReceiver registerUserReceiver = new RegisterUserReceiver();
    private ScheduledExecutorService periodicallyTurnOnLocationData = Executors.newSingleThreadScheduledExecutor();

    private String email;
    private String phone;
    private volatile boolean reconnect = false;

    private final Runnable runnableConnect = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i("RunAD", "Checking if I should connect socket?, reconnect?: " + pushService.isConnected() + " , " + reconnect);
                Log.i("RunAD", "Checking with data : " + email + " , " + phone);
                if (!pushService.isConnected() || reconnect /*&& email != null*/) {
                    reconnect = false;
                    pushService.connect(new StompMessageListener() {
                        @Override
                        public void onMessage(final StompMessage message) {
                            System.out.println(message.getHeader("destination") + ": " + message.getContent());
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Intent bata = new Intent(getName());
                                        bata.putExtra("ts", System.currentTimeMillis());
                                        bata.putExtra("msg", message.getContent());
                                        sendBroadcast(bata);
                                    } catch (NoSuchElementException ep) {
                                        //NOOP
                                    } catch (Exception ep) {
                                        Log.e(getName(), "Error handling message: ", ep);
                                    } catch (Throwable t) {
                                        Log.e(getName(), "Error throwable handling message: ", t);
                                    }
                                }
                            };
                        }
                    });
                    if (pushService.isConnected()) {
                        pushService.send("Email:" + email + "," + "Phone: " + phone, new StompMessageListener() {
                            @Override
                            public void onMessage(StompMessage message) {
                                Log.i("RgrRsp", "Received a registration response: " + message.getContent());
                            }
                        });
                    }
                }
            } catch (Exception ep) {
                Log.e("Svc", "Failed to connect ", ep);
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        IntentFilter intentFilter = new IntentFilter("RegisterUser");
        registerReceiver(registerUserReceiver, intentFilter);
        periodicallyTurnOnLocationData.scheduleWithFixedDelay(runnableConnect, 10, 10, TimeUnit.SECONDS);
        return ret;
    }

    //TODO now we get a message from the service and we send back the data intent here and process?
    private void sendNotification(String message, String title) {
        try {
            Intent intent = new Intent(this, AuthService.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Intent secondActivityIntent = new Intent(this, AuthService.class);
            secondActivityIntent.putExtra("ts", System.currentTimeMillis());
            secondActivityIntent.putExtra("Answer", "Yes");
            PendingIntent secondActivityPendingIntent = PendingIntent.getActivity(this, 1 , secondActivityIntent, PendingIntent.FLAG_ONE_SHOT);

            Intent thirdActivityIntent = new Intent(this, AuthService.class);
            secondActivityIntent.putExtra("ts", System.currentTimeMillis());
            secondActivityIntent.putExtra("Answer", "No");
            PendingIntent thirdActivityPendingIntent = PendingIntent.getActivity(this, 2 , thirdActivityIntent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    //.setSmallIcon(R.drawable.ic_3d_rotation_white_36dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .addAction(0, "Approve", secondActivityPendingIntent)
                    .addAction(0, "Deny", thirdActivityPendingIntent)
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
        unregisterReceiver(registerUserReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String getName() {
        return "AuthSrvc";
    }

    public class RegisterUserReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            try {
                if ("RegisterUser".equals(intent.getAction())) {
                    String eml = intent.getStringExtra("Email");
                    String phn = intent.getStringExtra("Phone");
                    if (!eml.equals(email) || !(phn.equals(phone))) {
                        reconnect = true;
                        email = eml;
                        phone = phn;
                        runnableConnect.run();
                    }
                    Log.i("Asvc", "Email: " + email + " Phone: " + phone);
                }
            } catch (Throwable t) {
                Log.e("AuthSrvc", "Error processing Intent!: ", t);
            }
        }
    }

}


/*

public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private final String mphone;

    UserLoginTask(String email, String phone) {
        mEmail = email;
        mphone = phone;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Intent bata = new Intent("RegisterAuthDom");
            bata.putExtra("ts", System.currentTimeMillis());
            bata.putExtra("msg", "{registration message I want the service to send}");
            sendBroadcast(bata);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mAuthTask = null;
        //showProgress(false);

        if (success) {
            finish();
        } else {
            mPhoneNumberView.setError(getString(R.string.error_incorrect_phone));
            mPhoneNumberView.requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        mAuthTask = null;
        //showProgress(false);
    }
}


 */