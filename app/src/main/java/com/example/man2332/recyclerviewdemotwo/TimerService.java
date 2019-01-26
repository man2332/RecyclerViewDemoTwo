package com.example.man2332.recyclerviewdemotwo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import static com.example.man2332.recyclerviewdemotwo.App.CHANNEL_ID;

public class TimerService extends Service {
    private String TAG = "STag";

    private CountDownTimer countDownTimer;
    private long mTimeLeftInMillis;
    private long timeElapsed = 0;

    private ResultReceiver resultReceiver;


    private static TimeUpListener mlistener;
    public TimerService(){
        //mlistener = null;
    }
    public interface TimeUpListener{
        void sendTime(long timeElapsed);
    }
    public void setTimeUpListener(TimeUpListener listener){
        mlistener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        //resultReceiver =intent.getParcelableExtra("receiver");

        if(countDownTimer == null){
            mTimeLeftInMillis = intent.getLongExtra("mTimeLeftInMillis",600000);
            countDownTimer = new CountDownTimer(mTimeLeftInMillis,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.d(TAG, "onTick: "+timeElapsed);
                    timeElapsed += 1;
                }

                @Override
                public void onFinish() {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    Log.d(TAG, "onFinish: "+timeElapsed);
                    mlistener.sendTime(timeElapsed);
                    //Intent serviceIntent = new Intent(getApplicationContext(), TimerService.class);
                    //stopService(serviceIntent);//call onDestroy()
//                Bundle bundle = new Bundle();
//                bundle.putLong("timeElapsed",timeElapsed);
//                resultReceiver.send(1,bundle);
                }
            };
            countDownTimer.start();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        //TODO: the notification should show the countdown time on the notification
        //TODO: clicking the notification should bring up the promodoro timer activity
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Example Service")
                .setContentText("Timer Is running#@@")
                .setSmallIcon(R.drawable.ic_touch_app)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
//        Bundle bundle = new Bundle();
//        bundle.putLong("timeElapsed",timeElapsed);
//        resultReceiver.send(1,bundle);
        if (countDownTimer != null) {
            Log.d(TAG, "onDestroy: DESTORY!!!");
            countDownTimer.cancel();
        }
        Log.d(TAG, "onDestroy: "+timeElapsed);
        mlistener.sendTime(timeElapsed);
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
