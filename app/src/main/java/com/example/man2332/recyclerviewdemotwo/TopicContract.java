package com.example.man2332.recyclerviewdemotwo;

import android.provider.BaseColumns;

public class TopicContract {
    public TopicContract() {
    }

    public static final class TopicEntry implements BaseColumns {
        public static final String TABLE_NAME = "TopicList";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TOTAL_TIME = "totaltime";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
