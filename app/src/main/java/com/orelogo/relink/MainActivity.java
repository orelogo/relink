package com.orelogo.relink;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    DBAdapter db = new DBAdapter(this); // assign database adapter
    SimpleCursorAdapter adapter; // adapter to populate ListView with database data
    private static final int CONTACTS_LOADER = 0; // identifies loader being used

    static final long YEAR_MS = 31_557_600_000L; // milliseconds in an average year (assuming 365.25 days)
    // milliseconds in an average month (assuming 365.25/12 days)
    static final long MONTH_MS = 2_629_800_000L;
    static final int WEEK_MS = 604_800_000; // milliseconds in a week
    static final int DAY_MS = 86_400_000;   // milliseconds in a day

    // time scale constants
    static final char DAYS = 'd';
    static final char WEEKS = 'w';
    static final char MONTHS = 'm';
    static final char YEARS = 'y';

    // for passing contact information in an intent
    static final String ROW_ID = "com.orelogo.relink.ROW_ID";
    static final String NAME = "com.orelogo.relink.NAME";
    static final String LAST_CONNECT = "com.orelogo.relink.LAST_CONNECT";
    static final String NEXT_CONNECT = "com.orelogo.relink.NEXT_CONNECT";
    static final String CONNECT_INTERVAL = "com.orelogo.relink.CONNECT_INTERVAL";
    static final String TIME_SCALE = "com.orelogo.relink.TIME_SCALE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadListView();
        // initiate loader for database query
        getLoaderManager().restartLoader(CONTACTS_LOADER, null, this);

    }

        @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case CONTACTS_LOADER:
                // create new cursor loader to querying database
                return new CursorLoader(this) {
                    @Override
                    public Cursor loadInBackground() {
                        db.open();
                        Cursor cursor =  db.getAllRows();    // cursor with all rows
                        db.close();
                        return cursor;
                    }
                };
            default:
                // an invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data); // change cursor when query complete
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }

    /**
     * For testing: View database.
     *
     * @param view button pressed
     */
    public void viewDatabase(View view) {

        TextView databaseView = (TextView) findViewById(R.id.database_view);
        String databaseItems = "";

        db.open();
        Cursor cursor = db.getAllRows();
        db.close();

        if (cursor != null && cursor.getCount() > 0) {
            do {
                String name = cursor.getString(DBAdapter.COL_NAME_INDEX);
                long lastConnect = cursor.getLong(DBAdapter.COL_LAST_CONNECT_INDEX);
                long nextConnectMillis = cursor.getLong(DBAdapter.COL_NEXT_CONNECT_INDEX);

                Date nextConnectDate = new Date(nextConnectMillis);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String nextConnect = sdf.format(nextConnectDate);

                databaseItems += name + ", " + nextConnect + "\n";
            } while (cursor.moveToNext());

            databaseView.setText(databaseItems);
        }
        else {
            databaseView.setText("The table is empty");
        }
        cursor.close();
    }

    /**
     * For testing: clear database table.
     *
     * @param view button pressed
     */
    public void clearTable(View view) {
        db.open();
        db.deleteAll();
        db.close();
        getLoaderManager().restartLoader(CONTACTS_LOADER, null, this);
        TextView databaseView = (TextView) findViewById(R.id.database_view);
        databaseView.setText("Cleared table!");
    }

    /**
     * Load add contact activity.
     *
     * @param view view that was clicked
     */
    public void addContact(View view) {
        Intent intent = new Intent(this, AddContact.class);
        startActivity(intent);
    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Cursor cursor = (Cursor) getListView().getItemAtPosition(position);

        // get information about contact
        int rowId = cursor.getInt(DBAdapter.COL_ID_INDEX);
        String name = cursor.getString(DBAdapter.COL_NAME_INDEX);
        long lastConnect = cursor.getLong(DBAdapter.COL_LAST_CONNECT_INDEX);
        long nextConnect = cursor.getLong(DBAdapter.COL_NEXT_CONNECT_INDEX);
        double connectInterval = cursor.getDouble(DBAdapter.COL_CONNECT_INTERVAL_INDEX);
        char timeScale = cursor.getString(DBAdapter.COL_TIME_SCALE_INDEX).charAt(0);

        cursor.close();

        Intent intent = new Intent(this, EditContact.class);

        // pass contact information into intent
        intent.putExtra(ROW_ID, rowId);
        intent.putExtra(NAME, name);
        intent.putExtra(LAST_CONNECT, lastConnect);
        intent.putExtra(NEXT_CONNECT, nextConnect);
        intent.putExtra(CONNECT_INTERVAL, connectInterval);
        intent.putExtra(TIME_SCALE, timeScale);

        startActivity(intent);
    }

    /**
     * Load list view.
     */
    private void loadListView() {

        // variables for generating ListView via SimpleCursorAdapter
        String[] fromColumns = {DBAdapter.COL_NAME, DBAdapter.COL_NEXT_CONNECT};
        int[] toViews = {R.id.name, R.id.next_connect};

        // adapter to populate ListView with database data, each item follows the main list_item
        // layout
        adapter = new SimpleCursorAdapter(this,
                R.layout.main_list_item, null, fromColumns, toViews, 0);

        // modify data from database to display time remaining to next connect
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.next_connect) {
                    int colIndex = cursor.getColumnIndex(DBAdapter.COL_NEXT_CONNECT);
                    long nextConnect = cursor.getLong(colIndex); // time of next connect

                    TextView nextConnectCountdown = (TextView) view;
                    // get time remaining from unix time
                    String timeRemaining = getTimeRemaining(nextConnect);
                    nextConnectCountdown.setText(timeRemaining);
                    return true;
                }
                return false;
            }
        });

        ListView listView = getListView();
        listView.setAdapter(adapter);
    }

    /**
     * Get the amount of time remaining (in years, months, weeks, or days) until you need to
     * connect with contact.
     *
     * @param nextConnect time when to connect, in unix time
     * @return time remaining when to connect
     */
    private String getTimeRemaining(long nextConnect) {
        double timeRemaining = 0; // number of years, months, weeks, or days remaining
        char timeScale = 'x';     // scale of time, ie. y year, m month, w week, or d day

        // amount of milliseconds remaining from right now
        double msRemaining = nextConnect - System.currentTimeMillis();

        // extract number of years, months, and weeks
        if (msRemaining >= YEAR_MS) {
            timeScale = YEARS;
            timeRemaining = (msRemaining / YEAR_MS);
            // round to ones column
            timeRemaining = Math.round(timeRemaining);
        }
        else if (msRemaining >= MONTH_MS) {
            timeScale = MONTHS;
            timeRemaining = (msRemaining / MONTH_MS);
            timeRemaining = Math.round(timeRemaining);
            if (timeRemaining >= 12){
                timeRemaining = 1;
                timeScale = YEARS;
            }
        }
        else if (msRemaining >= WEEK_MS) {
            timeScale = WEEKS;
            timeRemaining = (msRemaining / WEEK_MS);
            timeRemaining = Math.round(timeRemaining);
        }
        else if (msRemaining >= DAY_MS) {
            timeRemaining = (msRemaining / DAY_MS);
            timeScale = DAYS;
            timeRemaining = Math.round(timeRemaining);
            if (timeRemaining >= 7){
                timeRemaining = 1;
                timeScale = WEEKS;
            }
        }

        // build string
        String timeRemainingFinal = "due"; // default value
        if (timeRemaining > 0) {           // if there is time remaining
            timeRemainingFinal = (int) timeRemaining + " " + timeScale;
        }

        return timeRemainingFinal;
    }

}
