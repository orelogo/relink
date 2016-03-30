package com.orelogo.relink;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ListActivity {

    private DBAdapter db;    // database adapter for interacting with database

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
        db = new DBAdapter(this); // assign database adapter
        db.open();
        loadListView();
    }

        @Override
    protected void onPause() {
        super.onPause();
        db.close();
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

        Cursor cursor = db.getAllRows();

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

        Cursor cursor = db.getAllRows(); // get all rows from database

        // adapter to populate ListView with database data, each item follows the list_item layout
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item, cursor, fromColumns, toViews, 0);

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
     * Get the amount of time remaining (in years, months, weeks, days) until you need to connect
     * with contact.
     *
     * @param nextConnect epoch time when to connect
     * @return time countdown when to connect
     */
    private String getTimeRemaining(long nextConnect) {

        String connectCountdown = "";

        int y = 0;  // number of years, months, weeks, days
        int m = 0;
        int w = 0;
        int d = 0;

        // amount of days remaining from right now
        int daysRemaining = (int) Math.floor(
                (nextConnect - System.currentTimeMillis()) / 86_400_000);

        // extract number of years, months, and weeks
        if (daysRemaining >= 365) {
            y = daysRemaining / 365;
            daysRemaining = daysRemaining % 365;
        }
        if (daysRemaining >= 30) {
            m = daysRemaining / 30;
            daysRemaining = daysRemaining % 30;
        }
        if (daysRemaining >= 7) {
            w = daysRemaining / 7;
            daysRemaining = daysRemaining % 7;
        }
        if (daysRemaining >= 1) {
            d = daysRemaining;
        }

        // build string of time remaining
        if (y > 0) {
            connectCountdown += y + "y ";
        }
        if (m > 0) {
            connectCountdown += m + "m ";
        }
        if (w > 0) {
            connectCountdown += w + "w ";
        }
        if (d > 0) {
            connectCountdown += d + "d ";
        }

        return connectCountdown.trim();
    }

}
