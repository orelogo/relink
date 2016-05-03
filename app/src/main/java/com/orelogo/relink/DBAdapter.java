package com.orelogo.relink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database adapter for interacting with databse.
 */
public class DBAdapter {
    public DBAdapter(Context context) {
        dbHelper = new DBHelper(context);
    }

    static final String TABLE_NAME = "reminders";          // table name
    static final String COL_ID = "_id";                   // id column
    static final String COL_NAME = "name";                // contact name
    static final String COL_LAST_CONNECT = "lastConnect"; // date of last connection
    static final String COL_NEXT_CONNECT = "nextConnect"; // date of next connection
    static final String COL_CONNECT_INTERVAL = "connectInterval"; // connect interval
    static final String COL_TIME_SCALE = "timeScale"; // time scale of connect interval

    // column indexes
    static final int COL_ID_INDEX = 0;
    static final int COL_NAME_INDEX = 1;
    static final int COL_LAST_CONNECT_INDEX = 2;
    static final int COL_NEXT_CONNECT_INDEX = 3;
    static final int COL_CONNECT_INTERVAL_INDEX = 4;
    static final int COL_TIME_SCALE_INDEX = 5;


    // all column names
    private static final String[] ALL_COL = new String[] {COL_ID, COL_NAME, COL_LAST_CONNECT,
            COL_NEXT_CONNECT, COL_CONNECT_INTERVAL, COL_TIME_SCALE};

    // SQL code to generate table
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + TABLE_NAME + " (" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_NAME + " TEXT, " +
        COL_LAST_CONNECT + " DATE, " +
        COL_NEXT_CONNECT + " DATE, " +
        COL_CONNECT_INTERVAL + " REAL, " +
        COL_TIME_SCALE + " CHAR(1)" + ");";

    // SQL code to delete table
    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + TABLE_NAME;

    // order rows by next connect time
    private static final String ORDER_NEXT_CONNECT = COL_NEXT_CONNECT + " ASC";

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    /**
     * Open database.
     */
    void open() {
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Close database.
     */
    void close() {
        dbHelper.close();
    }

    /**
     * Get cursor with all rows and columns.
     *
     * @return cursor with all rows and columns
     */
    Cursor getAllRows() {
        Cursor c = db.query(true, TABLE_NAME, ALL_COL, null, null, null, null, ORDER_NEXT_CONNECT, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /**
     * Get cursor with all rows before time, in unix time.
     *
     * @param time time in unix time
     * @return cursor with all rows before time
     */
    Cursor getRowsBefore(long time) {
        // select rows that occur before time
        String selection = COL_NEXT_CONNECT + " < " + time;
        Cursor c = db.query(
                true, TABLE_NAME, ALL_COL, selection, null, null, null, ORDER_NEXT_CONNECT, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /**
     * Insert row into database.
     *
     * @param name contact name
     * @param lastConnect time of last connect, in unix time
     * @param nextConnect time of when to connect next, in unix time
     * @param connectInterval interval of how often to connect
     * @param timeScale time scale of how often to connect
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    long insertRow(String name, long lastConnect, long nextConnect, double connectInterval,
                   String timeScale) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_LAST_CONNECT, lastConnect);
        values.put(COL_NEXT_CONNECT, nextConnect);
        values.put(COL_CONNECT_INTERVAL, connectInterval);
        values.put(COL_TIME_SCALE, timeScale);

        return db.insert(TABLE_NAME, null, values);
    }

    /**
     * Update row based on row ID.
     *
     * @param rowId row ID
     * @param name contact name
     * @param lastConnect time of last connect, in unix time
     * @param nextConnect time of when to connect next, in unix time
     * @param connectInterval interval of how often to connect
     * @param timeScale time scale of how often to connect
     * @return true if at least one row was modified
     */
    boolean updateRow(long rowId, String name, long lastConnect, long nextConnect, double connectInterval,
                      String timeScale) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_LAST_CONNECT, lastConnect);
        values.put(COL_NEXT_CONNECT, nextConnect);
        values.put(COL_CONNECT_INTERVAL, connectInterval);
        values.put(COL_TIME_SCALE, timeScale);

        String where = COL_ID + "=" + rowId; // where clause

        return db.update(TABLE_NAME, values, where, null) != 0;
    }

    /**
     * Delete row.
     *
     * @param rowId row ID
     * @return true if at least one row was deleted
     */
    boolean deleteRow(long rowId) {
        String where = COL_ID + "=" + rowId;
        return db.delete(TABLE_NAME, where, null) != 0;
    }

    /**
     * Delete all rows.
     */
    void deleteAll() {
        db.delete(TABLE_NAME, null, null);
    }

    /**
     * Database helper class for creating, version management, opening and closing.
     */
    private class DBHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 3; // increase value when changing database
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
