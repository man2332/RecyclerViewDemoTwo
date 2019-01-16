package com.example.man2332.recyclerviewdemotwo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
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


}
