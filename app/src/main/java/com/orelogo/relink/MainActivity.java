package com.orelogo.relink;

import android.app.ListActivity;
import android.content.Intent;
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


public class MainActivity extends ListActivity {

    DBAdapter db = new DBAdapter(this); // assign database adapter
    Cursor cursor;                      // cursor for database information
    static final long YEAR_MS = 31_557_600_000L; // milliseconds in an average year (assuming 365.25 days)
    // milliseconds in an average month (assuming 365.25/12 days)
    static final long MONTH_MS = 2_629_800_000L;
    static final int WEEK_MS = 604_800_000; // milliseconds in a week
    static final int DAY_MS = 86_400_000;   // milliseconds in a day

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
    }

        @Override
    protected void onPause() {
        super.onPause();
        db.close();
        cursor.close();
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

    /**
     * For testing: View database.
     *
     * @param view button pressed
     */
    public void viewDatabase(View view) {

        TextView databaseView = (TextView) findViewById(R.id.database_view);
        String databaseItems = "";

        cursor = db.getAllRows();

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
        db.deleteAll();
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

    /**
     * Load list view.
     */
    private void loadListView() {
        // variables for generating ListView via SimpleCursorAdapter
        String[] fromColumns = {DBAdapter.COL_NAME, DBAdapter.COL_NEXT_CONNECT};
        int[] toViews = {R.id.name, R.id.next_connect};

        db.open();
        cursor = db.getAllRows();    // get all rows from database

        // adapter to populate ListView with database data, each item follows the main list_item
        // layout
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.main_list_item, cursor, fromColumns, toViews, 0);

        // modify data from database to display time remaining to next connect
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.next_connect) {
                    int colIndex = cursor.getColumnIndex(DBAdapter.COL_NEXT_CONNECT);
                    long nextConnect = cursor.getLong(colIndex); // time of next connect

                    TextView nextConnectCountdown = (TextView) view;
                    nextConnectCountdown.setText(getTimeRemaining(nextConnect));
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
            timeRemaining = (msRemaining / YEAR_MS);
            timeScale = 'y';
        }
        else if (msRemaining >= MONTH_MS) {
            timeRemaining = (msRemaining / MONTH_MS);
            timeScale = 'm';
        }
        else if (msRemaining >= WEEK_MS) {
            timeRemaining = (msRemaining / WEEK_MS);
            timeScale = 'w';
        }
        else if (msRemaining >= DAY_MS) {
            timeRemaining = (msRemaining / DAY_MS);
            timeScale = 'd';
        }

        // round to single decimal place
        timeRemaining = Math.round(timeRemaining * 10) / 10.0;

        // build string
        String timeRemainingFinal = "due"; // default value
        if (timeRemaining > 0) {         // if there is time remaining
            timeRemainingFinal = timeRemaining + " " + timeScale;
        }

        return timeRemainingFinal;
    }

}
