package com.example.man2332.recyclerviewdemotwo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TopicDBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "topiclist.db";
    public static final int DB_VERSION = 1;

    public TopicDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TABLE = "CREATE TABLE "+
                TopicContract.TopicEntry.TABLE_NAME + " ("+
                TopicContract.TopicEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                TopicContract.TopicEntry.COLUMN_NAME + " TEXT NOT NULL, "+
                TopicContract.TopicEntry.COLUMN_TOTAL_TIME + " INTEGER NOT NULL, "+
                TopicContract.TopicEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"+
                ");";

        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //ummm.............
        db.execSQL("DROP TABLE IF EXISTS "+ TopicContract.TopicEntry.TABLE_NAME);
        onCreate(db);
    }
}
