package com.example.man2332.recyclerviewdemotwo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AddTopicDialog extends AppCompatDialogFragment {

    private EditText addET;
    private AddTopicDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_add_topic_dialog, null);
        addET = view.findViewById(R.id.add_topic_edit_text);
        builder.setView(view)
                .setTitle("Add new timer!")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing if user press cancel
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = addET.getText().toString();
                        listener.addTopic(name);//MainActivity->addTopic()
                    }
                });
        return builder.create();
    }
    //-onAttach() - assign MainActivity as the listener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (AddTopicDialogListener) context;
        } catch (ClassCastException e) {
            //if forgot to implement the interface in the host activity
            throw new ClassCastException(context.toString() +
                    "must implement AddTopicDialogListener");
        }
    }
    //MainActivity implements & calls this to create a new topicName
    public interface AddTopicDialogListener{
        void addTopic(String name);
    }
}
