package com.example.man2332.recyclerviewdemotwo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AddTopicDialog.AddTopicDialogListener {
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
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //empty for now
            }

            @Override
            public void onEditClick(int position) {

            }
        });

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
//    private void createItemList() {
//        topicArrayList = new ArrayList<>();
//
//        topicArrayList.add(new Topic("Java", "20hours"));
//        topicArrayList.add(new Topic("Math", "15hours"));
//        topicArrayList.add(new Topic("Chemistry", "14hours"));
//    }
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
            contentValues.put(TopicContract.TopicEntry.COLUMN_TOTAL_TIME, "0HRS");

            db.insert(TopicContract.TopicEntry.TABLE_NAME,
                    null,
                    contentValues);
            mAdapter.swapCursor(getAllTopics());
        }


    }
    //-user clicks on menu option->add timer which adds a new topic to the db
}
