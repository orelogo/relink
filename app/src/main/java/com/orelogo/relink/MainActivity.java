package com.orelogo.relink;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * The main activity for the app which displays a list of reminders of who to reconnect with and
 * in how long. A floating button can be used to add a new reminder.
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    DBAdapter db = new DBAdapter(this); // assign database adapter
    SimpleCursorAdapter adapter; // adapter to populate ListView with database data
    private static final int CONTACTS_LOADER = 0; // identifies loader being used

    // for passing reminder information in an intent
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
        setSupportActionBar(toolbar);
        AlarmActivator.setAlarm(this);     // activate alarm for notifications

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
                openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Open SettingsActivity.
     */
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
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
     * Load add reminder activity.
     *
     * @param view view that was clicked
     */
    public void addReminder(View view) {
        Intent intent = new Intent(this, AddReminder.class);
        startActivity(intent);
    }

    /**
     * Load list of current reminder into view.
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

        final ListView listView = (ListView) findViewById(R.id.reminder_list_view);
        listView.setAdapter(adapter);

        // listener for when user clicks on a reminder in the list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                // get information about reminder
                int rowId = cursor.getInt(DBAdapter.COL_ID_INDEX);
                String name = cursor.getString(DBAdapter.COL_NAME_INDEX);
                long lastConnect = cursor.getLong(DBAdapter.COL_LAST_CONNECT_INDEX);
                long nextConnect = cursor.getLong(DBAdapter.COL_NEXT_CONNECT_INDEX);
                double connectInterval = cursor.getDouble(DBAdapter.COL_CONNECT_INTERVAL_INDEX);
                String timeScale = cursor.getString(DBAdapter.COL_TIME_SCALE_INDEX);

                cursor.close();

                Intent intent = new Intent(MainActivity.this, EditReminder.class);

                // pass reminder information into intent
                intent.putExtra(ROW_ID, rowId);
                intent.putExtra(NAME, name);
                intent.putExtra(LAST_CONNECT, lastConnect);
                intent.putExtra(NEXT_CONNECT, nextConnect);
                intent.putExtra(CONNECT_INTERVAL, connectInterval);
                intent.putExtra(TIME_SCALE, timeScale);

                startActivity(intent);
            }
        });
        }

    /**
     * Get the amount of time remaining (in years, months, weeks, or days) until you need to
     * connect with contact.
     *
     * @param nextConnect time when to connect, in unix time
     * @return time remaining when to connect
     * */
    static String getTimeRemaining(long nextConnect) {
        double timeRemaining = 0; // number of years, months, weeks, or days remaining
        String timeScale = "x";     // scale of time, ie. y year, m month, w week, or d day

        // amount of milliseconds remaining from right now
        double msRemaining = nextConnect - System.currentTimeMillis();

        // extract number of years, months, and weeks
        if (msRemaining >= Convert.YEAR_MS) {
            timeScale = Convert.YEARS_CHAR;
            timeRemaining = (msRemaining / Convert.YEAR_MS);
            // round to ones column
            timeRemaining = Math.round(timeRemaining);
        }
        else if (msRemaining >= Convert.MONTH_MS) {
            timeScale = Convert.MONTHS_CHAR;
            timeRemaining = (msRemaining / Convert.MONTH_MS);
            timeRemaining = Math.round(timeRemaining);
            if (timeRemaining >= 12){ // convert more than 12 months to years
                timeRemaining = 1;
                timeScale = Convert.YEARS_CHAR;
            }
        }
        else if (msRemaining >= Convert.WEEK_MS) {
            timeScale = Convert.WEEKS_CHAR;
            timeRemaining = (msRemaining / Convert.WEEK_MS);
            timeRemaining = Math.round(timeRemaining);
        }
        else {
            timeRemaining = (msRemaining / Convert.DAY_MS);
            timeScale = Convert.DAYS_CHAR;
            timeRemaining = Math.round(timeRemaining);
            // don't round <0.5 days to 0, necessary so that notification alert matches notifications due
            if (msRemaining > 0 && timeRemaining == 0) {
                timeRemaining = 1;
            }
            if (timeRemaining >= 7){ // convert more than 7 days to 12 months
                timeRemaining = 1;
                timeScale = Convert.WEEKS_CHAR;
            }
        }

        // build string
        String timeRemainingFinal;
        if (timeRemaining > 0) {           // if there is time remaining
            timeRemainingFinal = (int) timeRemaining + " " + timeScale;
        }
        else {
            timeRemainingFinal = "due";
        }

        return timeRemainingFinal;
    }

    // ------------------------------ Testing -------------------------------------------

//    /**
//     * For testing: View reminders as list of names and due dates.
//     *
//     * @param view button pressed
//     */
//    public void viewDatabase(View view) {
//
//        TextView databaseView = (TextView) findViewById(R.id.database_view);
//        String databaseItems = "";
//
//        db.open();
//        Cursor cursor = db.getAllRows();
//        db.close();
//
//        if (cursor != null && cursor.getCount() > 0) {
//            do {
//                String name = cursor.getString(DBAdapter.COL_NAME_INDEX);
//                long lastConnect = cursor.getLong(DBAdapter.COL_LAST_CONNECT_INDEX);
//                long nextConnectMillis = cursor.getLong(DBAdapter.COL_NEXT_CONNECT_INDEX);
//
//                Date nextConnectDate = new Date(nextConnectMillis);
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                String nextConnect = sdf.format(nextConnectDate);
//
//                databaseItems += name + ", " + nextConnect + "\n";
//            } while (cursor.moveToNext());
//
//            databaseView.setText(databaseItems);
//        }
//        else {
//            databaseView.setText("The table is empty");
//        }
//        cursor.close();
//    }
//
//    /**
//     * For testing: Clear database table of all current reminders.
//     *
//     * @param view button pressed
//     */
//    public void clearTable(View view) {
//        db.open();
//        db.deleteAll();
//        db.close();
//        getLoaderManager().restartLoader(CONTACTS_LOADER, null, this);
//        TextView databaseView = (TextView) findViewById(R.id.database_view);
//        databaseView.setText("Cleared table!");
//    }
//
//    /**
//     * For testing: Activate notification.
//     */
//    public void notify(View view) {
//        Intent intent = new Intent(this, NotificationPublisher.class);
//        sendBroadcast(intent);
//    }

}