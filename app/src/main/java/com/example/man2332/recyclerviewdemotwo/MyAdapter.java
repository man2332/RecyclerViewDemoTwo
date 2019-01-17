package com.example.man2332.recyclerviewdemotwo;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    //this field holds the data-a list of Items
    //ArrayList<Topic> topicList;//delete this later- no longer used

    //-this shows how to set up a listener
    private OnItemClickListener mlistener;
    //for using SQLite database
    private Context mContext;
    private Cursor mCursor;

    //constructor for adapter
    public MyAdapter(Context mContext, Cursor mCursor){
        this.mContext = mContext;
        this.mCursor = mCursor;
    }
    //interface that will be required to be implemented when used
    public interface OnItemClickListener{
        //when user clicks on card item itself- or clicks on the edit icon
        void onItemClick(int position);
        void onEditClick(int position);
        //this interface tells us that when mlistener is being set(setOnItemClickListener),
        // the object that is passed(OnItemClickListener listener), must implement both onItemClick & onEditClick
    }
    //used to set a listener object to "this" class's listener
    public void setOnItemClickListener(OnItemClickListener listener){
        mlistener = listener;//listener must implement required both interface methods
    }


    //-A RecyclerView contains many ViewHolders-ViewHolders hold the data in each view/card item
    //-The RecyclerView calls the ViewHolder's ctor when it needs to created a view/card item
    //-RecyclerView calls onCreateViewHolder-and onCreateViewHolder calls ViewHolder's ctor
    //-the ViewHolder's ctor constructs each view in the RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        //fields for each view the card contains
        private TextView topicName;
        private TextView totalTime;
        private ImageView editView;
        private ImageView playTimeView;

        //-the ViewHolder ctor gets passed a view(which contains all the view's data)
        // to help construct the view again
        public ViewHolder(View topicView) {
            super(topicView);

            topicName = topicView.findViewById(R.id.topicTextView);
            totalTime = topicView.findViewById(R.id.totalTimeTextView);
            editView = topicView.findViewById(R.id.editView);
            playTimeView = topicView.findViewById(R.id.playTimerView);

            //-when user clicks on the card view itself
            topicView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            //-when user clicks on the edit icon
            editView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //NOT RELALLY SURE ABOUT THIS LINE......
        //but it creates an empty view
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.cardview_item, viewGroup, false);
        //ViewHolder class will construct a ViewHolder using an empty view to use as each item/card view
        return new ViewHolder(view);
    }

    //-get from the database data and give it to the viewholder's views
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
//        viewHolder.topicName.setText(topicList.get(i).getTopic());
//        viewHolder.totalTime.setText(topicList.get(i).getTotalTime());
        //assign data to each view using db
        if(mCursor.moveToPosition(i)){
            String topicName = mCursor.getString(mCursor.getColumnIndex(TopicContract.TopicEntry.COLUMN_NAME));
            String totalTime = mCursor.getString(mCursor.getColumnIndex(TopicContract.TopicEntry.COLUMN_TOTAL_TIME));
            //get the id of each view to use later to delete an item
            long id = mCursor.getLong(mCursor.getColumnIndex(TopicContract.TopicEntry._ID));

            viewHolder.topicName.setText(topicName);
            viewHolder.totalTime.setText(totalTime);
        }


    }

    //-used by RecyclerView-RecyclerView will create as many items as this method returns
    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
    //-basically this method updates to the newest cursor pointer when called
    public void swapCursor(Cursor newCursor){
        if(mCursor != null){
            mCursor.close();//close old cursor
        }
        mCursor = newCursor;
        if(newCursor != null){
            notifyDataSetChanged();
        }
    }

}
