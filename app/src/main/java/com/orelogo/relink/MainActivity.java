package com.orelogo.relink;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public DBAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        db = new DBAdapter(this);
        db.open();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    public void addContact(View view) {

        EditText nameField = (EditText) findViewById(R.id.name);
        String name = nameField.getText().toString();

        EditText lastConnectField = (EditText) findViewById(R.id.last_connect);
        int lastConnect = Integer.parseInt( lastConnectField.getText().toString() );

        EditText nextConnectField = (EditText) findViewById(R.id.next_connect);
        int nextConnect = Integer.parseInt(nextConnectField.getText().toString());

        db.insertRow(name, lastConnect, nextConnect);

        TextView databaseView = (TextView) findViewById(R.id.database_view);
        databaseView.setText("Contact added!");

    }

    public void viewDatabase(View view) {

        TextView databaseView = (TextView) findViewById(R.id.database_view);
        String databaseItems = "";

        Cursor cursor = db.getAllRows();

        if (cursor != null && cursor.getCount() > 0) {
            do {
                String name = cursor.getString(DBAdapter.COL_NAME_INDEX);
                int lastConnect = cursor.getInt(DBAdapter.COL_LAST_CONNECT_INDEX);
                int nextConnect = cursor.getInt(DBAdapter.COL_NEXT_CONNECT_INDEX);

                databaseItems += "Name: " + name + ", Last Connect: " + lastConnect + ", Next Conect: " +
                        nextConnect + "\n";
            } while (cursor.moveToNext());

            databaseView.setText(databaseItems);
        }
        else {
            databaseView.setText("The table is empty");
        }
    }

    public void clearTable(View view) {
        db.deleteAll();
        TextView databaseView = (TextView) findViewById(R.id.database_view);
        databaseView.setText("Cleared table!");
    }
}
