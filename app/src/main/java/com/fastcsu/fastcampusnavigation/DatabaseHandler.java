package com.fastcsu.fastcampusnavigation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

/**
 * Created by Dawid on 11/22/16.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "neomed.db";
    private static final String TABLE_NAME = "firstfloor";

    private static final String COLUMN_ID = "id";
    private static final String FLOOR_NUMBER = "floor";
    private static final String ROOM_NUMBERS = "roomnumbers";
//    private static final String NUMBER_OF_ROOMS = "numberofrooms";
//    private static final String ACCESS_POINTS = "accesspoints";
//    private static final String NUMBER_OF_ACCESS_POINTS = "numberofaccesspoints";

    SQLiteDatabase db;

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTO INCREMENT, "
                + FLOOR_NUMBER + " TEXT, "
                + ROOM_NUMBERS + " TEXT"
                + ");";

        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP_TABLE_IF_EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertRoom(Room room) {
        ContentValues values = new ContentValues();
        values.put(FLOOR_NUMBER, "1");
        values.put(ROOM_NUMBERS, room.getNumber());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public String dbToString() {
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE 1";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {
            if(cursor.getString(cursor.getColumnIndex(ROOM_NUMBERS)) !=null) {
                dbString += cursor.getString(cursor.getColumnIndex(ROOM_NUMBERS));
                dbString += "\n";
            }
            cursor.moveToNext();
        }

        db.close();
        return dbString;
    }
}
