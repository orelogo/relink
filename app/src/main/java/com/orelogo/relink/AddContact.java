package com.orelogo.relink;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AddContact extends AppCompatActivity {

    public DBAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
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
    protected void onPause() {
        super.onPause();
        db.close();
    }

    public void addContact(View view) {

        EditText nameField = (EditText) findViewById(R.id.name);
        String name = nameField.getText().toString();

        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        Double connectIntervalMonths = Double.parseDouble ( connectIntervalField.getText().toString() );

        long currentTime =  System.currentTimeMillis();
        long intervalTime = (long) Math.floor(connectIntervalMonths * 2_628_000_000L);
        long nextConnect = currentTime + intervalTime;

        // add name and next connect time to database
        db.insertRow(name, currentTime, nextConnect);

        TextView databaseView = (TextView) findViewById(R.id.database_view);
        databaseView.setText("Contact added!");
    }

}
