package com.example.man2332.recyclerviewdemotwo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    //this field holds the data-a list of Items
    ArrayList<Item> itemList;
    //constructor for adapter
    public MyAdapter(ArrayList<Item> itemList){
        this.itemList = itemList;
    }
    //-this shows how to set up a listener
    private OnItemClickListener mlistener;


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
        public TextView topic;
        public TextView totalTime;
        public ImageView editView;

        //-the ViewHolder ctor gets passed a view(which contains all the view's data)
        // to help construct the view again
        public ViewHolder(View itemView) {
            super(itemView);

            topic = itemView.findViewById(R.id.topicTextView);
            totalTime = itemView.findViewById(R.id.totalTimeTextView);
            editView = itemView.findViewById(R.id.editView);

            //-when user clicks on the card view itself
            itemView.setOnClickListener(new View.OnClickListener() {
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
        View cardView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_item, viewGroup,false);
        //ViewHolder class will construct a ViewHolder using an empty view to use as each item/card view
        ViewHolder viewHolder = new ViewHolder(cardView);

        return viewHolder;//i think it returns an empty view holder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        //-this method assigns data to each view
        viewHolder.topic.setText(itemList.get(i).getTopic());
        viewHolder.totalTime.setText(itemList.get(i).getTotalTime());
    }

    //-used by RecyclerView-RecyclerView will create as many items as this method returns
    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
