package com.example.workoutapp;

import android.provider.BaseColumns;

public class Activity {

    private Activity() {}

    public static final class ActivityEntry implements BaseColumns {
        public static final String TABLE_NAME = "activityList";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AMOUNT = "duration";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
