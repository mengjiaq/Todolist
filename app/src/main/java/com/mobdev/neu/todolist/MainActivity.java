package com.mobdev.neu.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static ArrayList<TodoEvent> events;
    private static Map<TodoEvent, String> map;
    private static EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btnAddItem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddItem(view);
            }
        });

        RecyclerView rvEvents = (RecyclerView) findViewById(R.id.rvEvents);

        if (events == null) {
            events = new ArrayList<>();
            map = new HashMap<>();
        }

        adapter = new EventAdapter(events);

        rvEvents.setAdapter(adapter);

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("todoList").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        events.clear();
                        map.clear();
                        Log.w("TodoApp", "getUser:onCancelled " + dataSnapshot.toString());
                        Log.w("TodoApp", "count = " + String.valueOf(dataSnapshot.getChildrenCount()) + " values " + dataSnapshot.getKey());
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            TodoEvent todo = data.getValue(TodoEvent.class);
                            events.add(todo);
                            map.put(todo, data.getKey());
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("TodoApp", "getUser:onCancelled", databaseError.toException());
                    }


                });
    }
//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sort_by_time) {
            Collections.sort(events, (a, b)->(a.getTime().compareTo(b.getTime())));

            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_name) {
            Collections.sort(events, (a, b)->(a.getName().compareTo(b.getName())));

            adapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
//
////        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
////        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

    public void onAddItem(View view) {
        startActivity(new Intent(MainActivity.this, TodoActivity.class));
    }

    public static void addEvent(TodoEvent event, String key) {
        events.add(event);
        map.put(event, key);
    }

    public static String editEvent(TodoEvent event, int pos) {
        TodoEvent oldEvent = events.get(pos);
        oldEvent.setName(event.getName());
        oldEvent.setNote(event.getNote());
        oldEvent.setTime(event.getTime());

        return map.get(oldEvent);


    }

    public static String removeEvent(int pos) {
        TodoEvent oldEvent = events.get(pos);
        return map.get(oldEvent);


    }

    public static int countEvent() {
        return events.size();
    }

//    public static TodoEvent getEvent(int pos) {
//        return events.get(pos);
//    }
}
