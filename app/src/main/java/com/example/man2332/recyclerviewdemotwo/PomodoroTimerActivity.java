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

public class PomodoroTimerActivity extends AppCompatActivity {
    String TAG = "STag";
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro_timer);
        Log.d(TAG, "onCreate: MAIN");

        TopicDBHelper topicDBHelper = new TopicDBHelper(this);
        db = topicDBHelper.getWritableDatabase();

        mEditTextInput = findViewById(R.id.edit_text_input);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);

        mButtonSet = findViewById(R.id.button_set);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);

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

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
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
    }

    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer() {



        mSysStartTimeMillis = SystemClock.elapsedRealtime();

        mEndTime = SystemClock.elapsedRealtime() + mTimeLeftInMillis;

        ResultReceiver resultReceiver = new ResultReceiver(new Handler());
        //-start service
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra("mTimeLeftInMillis", mTimeLeftInMillis);
        //serviceIntent.putExtra("receiver",resultReceiver);//somehow it works yay
        ContextCompat.startForegroundService(this, serviceIntent);

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                //mElapsedTimeInSecs += 1;//each second that pasts, add one second to variable
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
        addTime();
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
        sendElapsedTime();
    }

    private void sendElapsedTime() {
    }

    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
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
        } else {
            mEditTextInput.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
            mButtonStartPause.setText("Start");

            if (mTimeLeftInMillis < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
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
//        Gson gson = new Gson();
//        String json = gson.toJson(timerService);
//        editor.putString("MyObject", json);

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
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("addTime","sss");
//        setResult(Activity.RESULT_OK, returnIntent);
        //Toast.makeText(getApplicationContext(),"HEEEEIIII", Toast.LENGTH_LONG).show();
        Intent data = new Intent();
        String test = "TEST123";
        data.setData(Uri.parse(test));
        setResult(Activity.RESULT_OK,data);
//
//        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        mSysStartTimeMillis = prefs.getLong("mSysStartTimeMillis", 0);

//        Gson gson = new Gson();
//        String json = prefs.getString("MyObject", "");
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
            }
            mElapsedTimeInSecs = -1;//reset time
        }
    }



    public class ResultReceiver extends android.os.ResultReceiver implements Parcelable {
        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public ResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(TAG, "onReceiveResult: "+mElapsedTimeInSecs);
            super.onReceiveResult(resultCode, resultData);
            mElapsedTimeInSecs = resultData.getLong("timeElapsed");
            Log.d(TAG, "onReceiveResult: "+mElapsedTimeInSecs);

        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


}
