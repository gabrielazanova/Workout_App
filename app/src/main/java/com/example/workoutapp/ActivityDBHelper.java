package com.example.workoutapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ActivityDBHelper extends SQLiteOpenHelper {

    public static  final String DATABASE_NAME = "activitylist.db";
    public static final int DATABASE_VERSION = 1;

    public ActivityDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ACTIVITYLIST_TABLE = "CREATE TABLE " +
               Activity.ActivityEntry.TABLE_NAME + "(" +
                Activity.ActivityEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Activity.ActivityEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                Activity.ActivityEntry.COLUMN_AMOUNT + " LONG NOT NULL, " +
                Activity.ActivityEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        db.execSQL(SQL_CREATE_ACTIVITYLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Activity.ActivityEntry.TABLE_NAME);
        onCreate(db);
    }

    //Convert the database to a hash map
    public LinkedHashMap<Integer, List<String>> databaseToString(){
        LinkedHashMap<Integer, List<String>> map = new LinkedHashMap<>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM "+ Activity.ActivityEntry.TABLE_NAME;
        Cursor c = db.rawQuery(query,null);
        c.moveToFirst();

        int i = 0;
        while(!c.isAfterLast()){
            List<String> l = new ArrayList<>();
            if(c.getString(c.getColumnIndex(Activity.ActivityEntry.COLUMN_NAME))!=null) {
                if(c.getString(c.getColumnIndex(Activity.ActivityEntry.COLUMN_AMOUNT))!=null) {
                    l.add(c.getString(c.getColumnIndex(Activity.ActivityEntry.COLUMN_NAME)));
                    l.add(c.getString(c.getColumnIndex(Activity.ActivityEntry.COLUMN_AMOUNT)));
                    l.add(c.getString(c.getColumnIndex(Activity.ActivityEntry.COLUMN_TIMESTAMP)));
                    map.put(i,l);
                }
            }
            i++;
            c.moveToNext();
        }
        //db.close();
        return map;
    }
}
