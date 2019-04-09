package com.mobdev.neu.todolist;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TodoActivity extends AppCompatActivity implements OnMapReadyCallback {


    boolean isEdit = false;
    int position = MainActivity.countEvent();
    TextView selectTime;
    String latitude, longitude;

    private GoogleMap mMap;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private LatLng mDefaultLocation;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        selectTime = (TextView) findViewById(R.id.select_time);
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
            latitude = event.getLatitude();
            longitude = event.getLongitude();
            if (event.getLatitude() != null) {
                mDefaultLocation = new LatLng(Double.parseDouble(event.getLatitude()),
                        Double.parseDouble(event.getLongitude()));

            }

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
                newEvent.setLatitude(latitude);
                newEvent.setLongitude(longitude);
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


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        if (mDefaultLocation != null) {
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                latitude = Double.toString(latLng.latitude);
                longitude = Double.toString(latLng.longitude);
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker

                Geocoder geocoder;
                List<Address> addresses = new ArrayList<>();
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                postalCode = (postalCode == null) ? "" : postalCode;
                address = (address == null) ? "unknown" : address;
                city = (city == null) ? "" : city;
                country = (country == null) ? "unknown" : country;
                knownName = (knownName == null) ? "" : knownName;
                state = (state == null) ? "unknown" : state;


                markerOptions.title(knownName + "\n" + address + "\n" + city + ", " + state + ", " + country
                        + ", " + postalCode);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

            }
        });
    }
}
        //showCurrentPlace();
        // Prompt the user for permission.
//        getLocationPermission();
////
////        // Turn on the My Location layer and the related control on the map.
////        updateLocationUI();
////
//        // Get the current location of the device and set the position of the map.
//        getDeviceLocation();











