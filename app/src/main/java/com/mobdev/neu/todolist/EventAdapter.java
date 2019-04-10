package com.mobdev.neu.todolist;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {


    private List<TodoEvent> mEvents;

    public EventAdapter(List<TodoEvent> events) {
        mEvents = events;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View eventView = inflater.inflate(R.layout.item_event, parent, false);

        ViewHolder viewHolder = new ViewHolder(eventView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        TodoEvent event = mEvents.get(position);
        TextView textView = viewHolder.eventNameTextView;
        textView.setText(event.getName());
        viewHolder.eventTimeTextView.setText(event.getTime());
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        String currentTime = sdf.format(cal.getTime());
        if (currentTime.compareTo(event.getTime()) > 0) {
            viewHolder.eventNameTextView.setTextColor(Color.RED);
            viewHolder.eventTimeTextView.setTextColor(Color.RED);
        } else {
            viewHolder.eventNameTextView.setTextColor(Color.BLACK);
            viewHolder.eventTimeTextView.setTextColor(Color.BLACK);
        }

    }


    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        public TextView eventNameTextView;
        public TextView eventTimeTextView;
        public TodoEvent event;


        public ViewHolder(View eventView) {

            super(eventView);
            eventNameTextView = (TextView) eventView.findViewById(R.id.event_name);
            eventTimeTextView = (TextView) eventView.findViewById(R.id.event_time);
//            editButton = (Button) eventView.findViewById(R.id.delete_button);
//            editButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                }
//            });
            eventView.setOnClickListener(this);
            eventView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            event = mEvents.get(getLayoutPosition());
            Intent myIntent = new Intent(view.getContext(),TodoActivity.class);
            myIntent.putExtra("event",event);
            myIntent.putExtra("position", getLayoutPosition());
            view.getContext().startActivity(myIntent);
        }

        @Override
        public boolean onLongClick(View view) {
            event = mEvents.get(getLayoutPosition());
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    view.getContext());

            // set title
            alertDialogBuilder.setTitle("Delete this event");
            alertDialogBuilder
                    .setMessage("Do you want to delete this event?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {


                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            String key = MainActivity.removeEvent(getLayoutPosition());
                            database.getReference("todoList").child(key).removeValue();
                            // now, we want to update any (list)views attached to our MyAdapter
                            // this will let the ListView update itself
                            mEvents.remove(event);
                            notifyDataSetChanged();

                        }
                    })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });
            alertDialogBuilder.show();
            return true;
        }
    }
}
