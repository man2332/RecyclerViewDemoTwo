package com.example.man2332.recyclerviewdemotwo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AddTopicDialog.AddTopicDialogListener {
    private ArrayList<Item> itemArrayList;//stores all the data for our RecyclerView

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        createItemList();
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);//dont change recyclerview's size
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MyAdapter(itemArrayList);//create & give adapter it's data
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

    private void createItemList() {
        itemArrayList = new ArrayList<>();

        itemArrayList.add(new Item("Java", "20hours"));
        itemArrayList.add(new Item("Math", "15hours"));
        itemArrayList.add(new Item("Chemistry", "14hours"));
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
                //show popup dialog for creating a new topic-name
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
        //add new topic to itemArrayList with the given name
        itemArrayList.add(new Item(name, "0HR"));
    }
    //-user clicks on menu option->add timer which adds a new topic to the itemArrayList
}
