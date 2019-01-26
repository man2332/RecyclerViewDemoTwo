package com.example.man2332.recyclerviewdemotwo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Locale;
//TODO: convert this to 3 buttons-Pomodoro, ShortBreak & LongBreak
public class PomodoroTimerActivity extends AppCompatActivity {
    String TAG = "MTag";
    private EditText mEditTextInput;
    private TextView mTextViewCountDown;
    private Button mButtonSet;
    private Button mButtonStartPause;
    private Button mButtonReset;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private long mSysStartTimeMillis;
    private long mElapsedTimeInSecs = -1;//use to keep track of time that has passed
    //-its -1 becuz we use onTick to track time, but onTick is called once before timer starts, then again each second-1000 millis
    TimerService timerService = null;


    private int rowId;//used to identify which view item in the recycler view called this activity

    private SQLiteDatabase db;
    private Cursor mCursor;

    //for when user is selecting a certain timer, timers will keep orginal set time values
    private MyTimer pomoTimer;
    private MyTimer shortBreakTimer;
    private MyTimer longBreakTimer;

    private Button mSetPomo;
    private Button mSetSBreak;
    private Button mSetLBreak;

    private TextView mTitleTextView;

    private int goalTimeSecondsCompleted;

    public enum selectedTimer {
            pTimer, sTimer, lTimer
    }
    selectedTimer mSelectedTimer;//default value is pTimer if not set
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro_timer);
        Log.d(TAG, "onCreate: POMO");

        TopicDBHelper topicDBHelper = new TopicDBHelper(this);
        db = topicDBHelper.getWritableDatabase();

        mEditTextInput = findViewById(R.id.edit_text_input);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);

        mButtonSet = findViewById(R.id.button_set);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);


        mSetPomo = findViewById(R.id.pomo_timer);
        mSetPomo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedTimer = selectedTimer.pTimer;
                updateCountDownText();
                updateWatchInterface();
            }
        });
        mSetSBreak = findViewById(R.id.short_break);
        mSetSBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedTimer = selectedTimer.sTimer;
                updateCountDownText();
                updateWatchInterface();
            }
        });
        mSetLBreak = findViewById(R.id.long_break);
        mSetLBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedTimer = selectedTimer.lTimer;
                updateCountDownText();
                updateWatchInterface();
            }
        });

        mTitleTextView = findViewById(R.id.title_text_view);

        Intent intent = getIntent();
        rowId = intent.getIntExtra("rowId", 5);
        //rowId = bundle.getInt("rowId",0);
        //rowId = intent.getIntExtra("rowId", 5);
        Log.d("MTag", "onCreate: rowID IS: "+rowId);

        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mEditTextInput.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(PomodoroTimerActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0) {
                    Toast.makeText(PomodoroTimerActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(millisInput);
                mEditTextInput.setText("");
            }
        });

        mButtonStartPause.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });


        //start broadcast
        //startService(new Intent(this, TimerService.class));
        //set up listener
        timerService = new TimerService();
        timerService.setTimeUpListener(new TimerService.TimeUpListener() {
            @Override
            public void sendTime(long timeElapsed) {
                mElapsedTimeInSecs = timeElapsed;
                addTime();
            }
        });
        //might be recalls after app is destroyed so we need to get data from bundle if there
        // is any - do this later

        mSelectedTimer = selectedTimer.pTimer;

        goalTimeSecondsCompleted = 0;

    }

    private void setTime(long milliseconds) {
        //check which type of timer it is-pomo,s break, l break
        switch(mSelectedTimer){
            case pTimer:
                pomoTimer.setTime(milliseconds);
                pomoTimer.setmStartTimeInMillis(milliseconds);
                pomoTimer.setmTimeLeftInMillis(milliseconds);
                break;
            case sTimer:
                shortBreakTimer.setTime(milliseconds);
                shortBreakTimer.setmStartTimeInMillis(milliseconds);
                shortBreakTimer.setmTimeLeftInMillis(milliseconds);
                break;
            case lTimer:
                longBreakTimer.setTime(milliseconds);
                longBreakTimer.setmStartTimeInMillis(milliseconds);
                longBreakTimer.setmTimeLeftInMillis(milliseconds);
                break;
        }
        mStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer() {
        mSysStartTimeMillis = SystemClock.elapsedRealtime();
        long time = 0;
        switch(mSelectedTimer){
            case pTimer:
                time = pomoTimer.getmTimeLeftInMillis();
                //-start service
                Intent serviceIntent = new Intent(this, TimerService.class);
                serviceIntent.putExtra("mTimeLeftInMillis", time);
                //serviceIntent.putExtra("receiver",resultReceiver);//somehow it works yay
                ContextCompat.startForegroundService(this, serviceIntent);
                break;
            case sTimer:
                time = shortBreakTimer.getmTimeLeftInMillis();
                break;
            case lTimer:
                time = longBreakTimer.getmTimeLeftInMillis();
                break;
        }

        mTimeLeftInMillis = time;
        mEndTime = SystemClock.elapsedRealtime() + mTimeLeftInMillis;
        //shortBreakTimer.setmEndTime(SystemClock.elapsedRealtime() + shortBreakTimer.getmTimeLeftInMillis());

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                //mElapsedTimeInSecs += 1;//each second that pasts, add one second to variable
                switch(mSelectedTimer){
                    case pTimer:
                        pomoTimer.setmTimeLeftInMillis(millisUntilFinished);
                        break;
                    case sTimer:
                        shortBreakTimer.setmTimeLeftInMillis(millisUntilFinished);
                        break;
                    case lTimer:
                        longBreakTimer.setmTimeLeftInMillis(millisUntilFinished);
                        break;
                }
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                updateWatchInterface();
            }
        }.start();

        mTimerRunning = true;
        updateWatchInterface();
    }

    private void pauseTimer() {
        //-stop service
        stopTimerService();


        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateWatchInterface();
    }

    private void stopTimerService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        stopService(serviceIntent);
    }

    private void resetTimer() {
        //-stop service

        mTimeLeftInMillis = mStartTimeInMillis;
        switch(mSelectedTimer){
            case pTimer:
                addTime();
                pomoTimer.setmTimeLeftInMillis();
                break;
            case sTimer:
                shortBreakTimer.setmTimeLeftInMillis();
                break;
            case lTimer:
                longBreakTimer.setmTimeLeftInMillis();
                break;
        }
        updateCountDownText();
        updateWatchInterface();
    }



    private void updateCountDownText() {
//        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
//        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
//        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        String timeLeftFormatted;

        switch(mSelectedTimer){
            case pTimer:
                Log.d(TAG, "updateCountDownText: "+pomoTimer.getSeconds());
                hours = pomoTimer.getHours();
                minutes = pomoTimer.getMinutes();
                seconds = pomoTimer.getSeconds();
                break;
            case sTimer:
                hours = shortBreakTimer.getHours();
                minutes = shortBreakTimer.getMinutes();
                seconds = shortBreakTimer.getSeconds();
                break;
            case lTimer:
                hours = longBreakTimer.getHours();
                minutes = longBreakTimer.getMinutes();
                seconds = longBreakTimer.getSeconds();
                break;
        }

        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }
        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void updateWatchInterface() {
        if (mTimerRunning) {
            mEditTextInput.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
            mSetPomo.setVisibility(View.INVISIBLE);
            mSetSBreak.setVisibility(View.INVISIBLE);
            mSetLBreak.setVisibility(View.INVISIBLE);
        } else {
            mEditTextInput.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
            mButtonStartPause.setText("Start");
            mSetPomo.setVisibility(View.VISIBLE);
            mSetSBreak.setVisibility(View.VISIBLE);
            mSetLBreak.setVisibility(View.VISIBLE);

            if (mTimeLeftInMillis < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }
//
//            if (mTimeLeftInMillis < mStartTimeInMillis) {
//                mButtonReset.setVisibility(View.VISIBLE);
//            } else {
//                mButtonReset.setVisibility(View.INVISIBLE);
//            }
            switch (mSelectedTimer){
                case pTimer:
                    mTitleTextView.setText("POMOTIMER!!!");
                    if(pomoTimer.getmTimeLeftInMillis() < pomoTimer.getmStartTimeInMillis()){
                        mButtonReset.setVisibility(View.VISIBLE);
                    }else{
                        mButtonReset.setVisibility(View.INVISIBLE);
                    }
                    break;
                case sTimer:
                    mTitleTextView.setText("SHORT BREAK!!!");
                    if(shortBreakTimer.getmTimeLeftInMillis() < shortBreakTimer.getmStartTimeInMillis()){
                        mButtonReset.setVisibility(View.VISIBLE);
                    }else{
                        mButtonReset.setVisibility(View.INVISIBLE);
                    }
                    break;
                case lTimer:
                    mTitleTextView.setText("LONG BREAK!!!");
                    if(longBreakTimer.getmTimeLeftInMillis() < longBreakTimer.getmStartTimeInMillis()){
                        mButtonReset.setVisibility(View.VISIBLE);
                    }else{
                        mButtonReset.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }

    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("MTag", "onBackPressed: 1");
        //addTime();
        returnTime();//super.onBackPressed() should be last line, or else other code may not execute
        super.onBackPressed();


    }

    @Override
    protected void onStop() {

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", mStartTimeInMillis);
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.putLong("mSysStartTimeMillis", mSysStartTimeMillis);
        Gson gson = new Gson();
        String json = gson.toJson(pomoTimer);
        editor.putString("pomoTimer", json);
        editor.putString("shortBreakTimer",gson.toJson(shortBreakTimer));
        editor.putString("longBreakTimer",gson.toJson(longBreakTimer));

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }


        super.onStop();
        Log.d("MTag", "onStop: 1");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MTag", "onDestroy: 1");
    }

    private void returnTime() {
        Log.d(TAG, "returnTime: "+goalTimeSecondsCompleted);
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("addTime","sss");
//        setResult(Activity.RESULT_OK, returnIntent);
        //Toast.makeText(getApplicationContext(),"HEEEEIIII", Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        //String test = "TEST123";
        //intent.setData(Uri.parse(test));
        intent.putExtra("goalTimeSecondsCompleted",goalTimeSecondsCompleted);
        setResult(Activity.RESULT_OK,intent);
//
//        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        mSysStartTimeMillis = prefs.getLong("mSysStartTimeMillis", 0);

        Gson gson = new Gson();
        String jsonPomo = prefs.getString("pomoTimer", "");
        String jsonSBreak = prefs.getString("shortBreakTimer", "");
        String jsonLBreak = prefs.getString("longBreakTimer", "");
        if(jsonPomo != "" && jsonSBreak != "" && jsonLBreak != ""){
            Log.d(TAG, "onStart: DATA IN PREFS: pomo: "+jsonPomo+" : "+jsonSBreak+" : "+jsonLBreak);
            pomoTimer = gson.fromJson(jsonPomo, MyTimer.class);
            shortBreakTimer = gson.fromJson(jsonSBreak, MyTimer.class);
            longBreakTimer = gson.fromJson(jsonLBreak, MyTimer.class);
        }else{
            Log.d(TAG, "onStart: STARTING NEW TIMERS");
            pomoTimer = new MyTimer(1500000);//25 mins
            shortBreakTimer = new MyTimer(300000);//5 mins
            longBreakTimer = new MyTimer(600000);//10 mins
        }

//        TimerService obj = gson.fromJson(json, TimerService.class);
//        timerService = obj;

        updateCountDownText();
        updateWatchInterface();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - SystemClock.elapsedRealtime();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }

//    @Override
//    public void sendTime(long timeElapsed) {
//        mElapsedTimeInSecs = timeElapsed;
//    }

    //TODO: add a menu with delete option

    public interface AddTimeListener{
        public void addTime(long mElapsedTimeInMillis);
    }



    //add elapsed time to the item
    public void addTime() {

        //int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        String id = Integer.toString(rowId);

        Cursor cursor = db.query(TopicContract.TopicEntry.TABLE_NAME, new String[]{TopicContract.TopicEntry.COLUMN_TOTAL_TIME},
                TopicContract.TopicEntry._ID + " = ?", new String[]{id},
                null, null, null
        );
        if (cursor.moveToFirst()) {
            Log.d("MTag", "addTime: OKKKKKssssssss : " + cursor.getString(0) + " : ");

        }
        //int timeInDatabase = cursor.getColumnIndex(TopicContract.TopicEntry.COLUMN_TOTAL_TIME);
        int timeInDatabase = cursor.getInt(0);
        //Log.d("MTag", "addTime: OKKKKKssssssss : "+cursor.getString(1) + " : "+
        //cursor.getInt(2)+ " : "+cursor.getString(3));
        Log.d("MTag", "addTime: TOTAL TIME: " + timeInDatabase);
        //if start was pressed
        if (mSysStartTimeMillis != 0) {
            long timeElapsedInMillis = SystemClock.elapsedRealtime() - mSysStartTimeMillis;
            mSysStartTimeMillis = 0;

            int secondsElapsed = (int) (timeElapsedInMillis / 1000);//
            //int minutesElapses = Math.round(secondsElapsed / 60);
            Log.d(TAG, "addTime: "+mElapsedTimeInSecs);
            if (minutes != 0) {
                ContentValues contentValues = new ContentValues();//timeInDatabase+
                //contentValues.put(TopicContract.TopicEntry.COLUMN_TOTAL_TIME, timeInDatabase + secondsElapsed);
                contentValues.put(TopicContract.TopicEntry.COLUMN_TOTAL_TIME, timeInDatabase + mElapsedTimeInSecs);
                db.update(TopicContract.TopicEntry.TABLE_NAME,
                        contentValues,
                        "_id=" + rowId,
                        null
                );

                goalTimeSecondsCompleted += mElapsedTimeInSecs;
            }
            mElapsedTimeInSecs = -1;//reset time
        }
    }

    public class MyTimer{
        private int hours;
        private int minutes;
        private int seconds;
        private long mStartTimeInMillis;
        private long mTimeLeftInMillis;
        private long mEndTime;
        private boolean mTimerRunning;

        public MyTimer(long milliseconds) {
            this.hours = (int) (milliseconds / 1000) / 3600;
            this.minutes = (int) ((milliseconds / 1000) % 3600) / 60;
            this.seconds = (int) (milliseconds / 1000) % 60;
        }

        public void setTime(long milliseconds) {
            this.hours = (int) (milliseconds / 1000) / 3600;
            this.minutes = (int) ((milliseconds / 1000) % 3600) / 60;
            this.seconds = (int) (milliseconds / 1000) % 60;
        }

        public void setmTimeLeftInMillis(long mTimeLeftInMillis) {
            this.mTimeLeftInMillis = mTimeLeftInMillis;
        }

        public boolean ismTimerRunning() {
            return mTimerRunning;
        }

        public void setmTimerRunning(boolean mTimerRunning) {
            this.mTimerRunning = mTimerRunning;
        }



        public long getmStartTimeInMillis() {
            return mStartTimeInMillis;
        }

        public void setmStartTimeInMillis(long mStartTimeInMillis) {
            this.mStartTimeInMillis = mStartTimeInMillis;
        }

        public long getmTimeLeftInMillis() {
            return mTimeLeftInMillis;
        }

        public void setmTimeLeftInMillis() {
            if(mStartTimeInMillis != 0) {
                this.mTimeLeftInMillis = this.mStartTimeInMillis;
            }else{
                Log.d(TAG, "setmTimeLeftInMillis: Need to SET START TIME");
            }
        }

        public long getmEndTime() {
            return mEndTime;
        }

        public void setmEndTime(long mEndTime) {
            this.mEndTime = mEndTime;
        }

        public int getHours() {
            return (int) (mTimeLeftInMillis / 1000) / 3600;
        }

        public void setHours(int hours) {
            this.hours = hours;
        }

        public int getMinutes() {
            return (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        }

        public void setMinutes(int minutes) {
            this.minutes = minutes;
        }

        public int getSeconds() {
            return (int) (mTimeLeftInMillis / 1000) % 60;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }


    }

}
