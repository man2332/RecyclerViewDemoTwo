package com.example.man2332.recyclerviewdemotwo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AddTopicDialog.AddTopicDialogListener {
    private static final String TAG = "MTag";//, MyAdapter.OnItemClickListener
    //private ArrayList<Topic> topicArrayList;//stores all the data for our RecyclerView

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //for using a database
    private SQLiteDatabase db;
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //createItemList();
        setUpRecyclerView();
    }

    private void startPomodoro(int id){
        //mCursor.getPosition();

        Intent intent = new Intent(getApplicationContext(), PomodoroTimerActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtra("rowId",id);
        Log.d(TAG, "startPomodoro: 1: Row Id: "+id);
        //bundle.putInt("rowId", id);
        startActivityForResult(intent, 1, bundle);
    }

    private void setUpRecyclerView() {
        //create a db
        TopicDBHelper topicDBHelper = new TopicDBHelper(this);
        db = topicDBHelper.getWritableDatabase();
        mCursor = getAllTopics();

        mRecyclerView = findViewById(R.id.recyclerView);
        //mRecyclerView.setHasFixedSize(true);//dont change recyclerview's size
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MyAdapter(this, mCursor);//create & give adapter it's data
        //set up the recyclerView with it's layout manager and adapter
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int id) {
                //empty for now
                //Intent intent = new Intent(getApplicationContext(), PomodoroTimerActivity.class);
                //startActivityForResult(intent, 1);
                startPomodoro(id);
            }

//            @Override
//            public void onEditClick(int position) {
//
//            }
        });
//        mAdapter.onBind = new MyAdapter.OnBindCallback() {
//            @Override
//            public void onViewBound(MyAdapter.ViewHolder viewHolder, int position) {
//
//            }
//        };

        mRecyclerView.setAdapter(mAdapter);



    }
    //***************************Database stuff*****************************************************
    //-return all topics in the database
    private Cursor getAllTopics() {
        return db.query(//.query() returns a cursor obj
                TopicContract.TopicEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                TopicContract.TopicEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }
    //***************************MENU***************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_topic:
                //show popup dialog for creating a new topicName-name
                openAddTopicDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //********************************ADD TOPIC DIALOG**********************************************
    public void openAddTopicDialog(){
        AddTopicDialog addTopicDialog = new AddTopicDialog();
        addTopicDialog.show(getSupportFragmentManager(),"addtopic dialog");
    }
    @Override
    public void addTopic(String name) {
        //add a new topic to the db
        if(name.trim().length() != 0){
            ContentValues contentValues = new ContentValues();
            contentValues.put(TopicContract.TopicEntry.COLUMN_NAME, name);
            contentValues.put(TopicContract.TopicEntry.COLUMN_TOTAL_TIME, 0);

            db.insert(TopicContract.TopicEntry.TABLE_NAME,
                    null,
                    contentValues);
            mAdapter.swapCursor(getAllTopics());
        }


    }
    //-user clicks on menu option->add timer which adds a new topic to the db


    @Override
    protected void onResume() {
        mAdapter.swapCursor(getAllTopics());
        super.onResume();
    }

    public void addTime(String name, String time){

        if(time.trim().length() != 0){
            Integer timeInt = Integer.parseInt(time);
            ContentValues contentValues = new ContentValues();
            contentValues.put(TopicContract.TopicEntry.COLUMN_TOTAL_TIME, timeInt);

            db.update(TopicContract.TopicEntry.TABLE_NAME,
                    contentValues,
                    TopicContract.TopicEntry.COLUMN_NAME+"="+name,
                    null
            );
        }
    }
    //*********************************VIEW ITEM CLICK LISTENER*************************************
//    @Override
//    public void onItemClick(int position) {
//        Intent intent = new Intent(this, PomodoroTimerActivity.class);
//        startActivityForResult(intent, 1, new Bundle());
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            Log.d(TAG, "onActivityResult: 1");
            if(resultCode == Activity.RESULT_OK){
                Log.d(TAG, "onActivityResult: 3");
                String timeLeft = data.getData().toString();
                Toast.makeText(getApplicationContext(),timeLeft,Toast.LENGTH_LONG).show();
            }
        }
        Log.d(TAG, "onActivityResult: 2");

    }
}
