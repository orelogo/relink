package com.orelogo.relink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class DBAdapter {
    public DBAdapter(Context context) {
        dbHelper = new DBHelper(context);
    }

    public static final String TABLE_NAME = "contacts";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";  // contact name
    public static final String COL_LAST_CONNECT = "lastConnect"; // date of last connection
    public static final String COL_NEXT_CONNECT = "nextConnect"; // date of next connection

    public static final int COL_ID_INDEX = 0;
    public static final int COL_NAME_INDEX = 1;  // contact name
    public static final int COL_LAST_CONNECT_INDEX = 2; // date of last connection
    public static final int COL_NEXT_CONNECT_INDEX = 3; // date of next connection

    public static final String[] ALL_COL = new String[] {
            COL_ID, COL_NAME, COL_LAST_CONNECT, COL_NEXT_CONNECT};

    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + TABLE_NAME + " (" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_NAME + " TEXT, " +
        COL_LAST_CONNECT + " DATE, " +
        COL_NEXT_CONNECT + " DATE" + ");";

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS" + TABLE_NAME;

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertRow(String name, long lastConnect, long nextConnect) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_LAST_CONNECT, lastConnect);
        values.put(COL_NEXT_CONNECT, nextConnect);

        return db.insert(TABLE_NAME, null, values);
    }

    public boolean deleteRow(long rowId) {
        String where = COL_ID + "=" + rowId;
        return db.delete(TABLE_NAME, where, null) != 0;
    }

    public void deleteAll() {
        db.delete(TABLE_NAME, null, null);
    }

    public Cursor getAllRows() {
        Cursor c = db.query(true, TABLE_NAME, ALL_COL, null, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "relink.db";

        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }

}
