package com.mobdev.neu.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TodoActivity extends AppCompatActivity {


    boolean isEdit = false;
    int position = MainActivity.countEvent();
    TextView selectTime;
    TextView selectLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selectTime = (TextView) findViewById(R.id.select_time);
        selectLocation = (TextView) findViewById(R.id.select_location);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/mm/dd HH:mm");
        selectTime.setText(sdf.format(cal.getTime()));
        selectTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int day = mcurrentTime.get(Calendar.DAY_OF_MONTH);
                int month = mcurrentTime.get(Calendar.MONTH);
                int year = mcurrentTime.get(Calendar.YEAR);
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TodoActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        selectTime.setText(selectTime.getText()+ " " + (selectedHour > 9?selectedHour:"0"+selectedHour)
                                +":"+selectedMinute);
                    }
                }, hour, minute,true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();


                // date picker dialog
                DatePickerDialog mDatePicker = new DatePickerDialog(TodoActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                selectTime.setText(year + "/" + ((monthOfYear + 1) > 9?
                                        (monthOfYear + 1):"0"+(monthOfYear + 1)) + "/"+dayOfMonth);
                            }
                        }, year, month, day);
                mDatePicker.show();


            }
        });


        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            TodoEvent event = (TodoEvent) intent.getExtras().getSerializable("event");
            EditText name = (EditText) findViewById(R.id.event_name);
            EditText note = (EditText) findViewById(R.id.event_note);
            name.setText(event.getName());
            note.setText(event.getNote());
            selectTime.setText(event.getTime());
            isEdit = true;
            position = (int) intent.getExtras().get("position");
        }

        FloatingActionButton fab = findViewById(R.id.btnConfirm);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = ((EditText) findViewById(R.id.event_name)).getText().toString();

                if (name == null || name.length() == 0) {
                    Snackbar.make(view, "The name should not be empty", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                String note = ((EditText) findViewById(R.id.event_note)).getText().toString();
                TodoEvent newEvent = new TodoEvent(name, note, selectTime.getText().toString());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                Map<String, Object> childUpdates = new HashMap<>();
                if (!isEdit) {

                    String key = database.getReference("todoList").push().getKey();
                    childUpdates.put( key, newEvent.toFirebaseObject());
                    MainActivity.addEvent(newEvent, key);

                } else {
                    String key = MainActivity.editEvent(newEvent, position);
                    childUpdates.put( key, newEvent.toFirebaseObject());
                }
                database.getReference("todoList").updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            finish();
                        }
                    }
                });


                startActivity(new Intent(TodoActivity.this, MainActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    int PLACE_PICKER_REQUEST = 1;

    public void Goplacepicker(View view){

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try{
            startActivityForResult(builder.build(TodoActivity.this),PLACE_PICKER_REQUEST);
        }catch(GooglePlayServicesRepairableException e){
            e.printStackTrace();
        }catch(GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        }


    }

    @Override
    protected void onActivityResult(int request, int resultCode, Intent data){

        if(resultCode == PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(TodoActivity.this,data);
                selectLocation.setText(place.getAddress());
            }
        }
    }

}
