package com.fastcsu.fastcampusnavigation;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DestinationActivity extends AppCompatActivity {

    DatabaseHandler dbHandler;
    EditText roomNumber;
    TextView roomTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        roomNumber = (EditText) findViewById(R.id.editDestination);
        roomTest = (TextView) findViewById(R.id.textViewRoomResult);

        dbHandler = new DatabaseHandler(this, null, null, 1);

        Button navigate = (Button) findViewById(R.id.bNavigate);
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Room room = new Room(roomNumber.getText().toString());
                dbHandler.insertRoom(room);
                printDatabase();
            }
        });


        FloatingActionButton previousRooms = (FloatingActionButton) findViewById(R.id.floatingActionButtonLocations);
        previousRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DestinationActivity.this, LocationsActivity.class);
                startActivity(intent);
            }
        });
        printDatabase();


    }

    public void printDatabase() {
        String dbString = dbHandler.dbToString();
        roomTest.setText(dbString);
        roomNumber.setText("");
    }

//    public void onNavigateClicked(View view) {
//        Room room = new Room(roomNumber.getText().toString());
//        dbHandler.insertRoom(room);
//        printDatabase();
//    }

}
