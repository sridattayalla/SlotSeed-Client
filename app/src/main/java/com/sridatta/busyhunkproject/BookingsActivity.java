package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sridatta.busyhunkproject.models.booking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;


public class BookingsActivity extends AppCompatActivity implements BookingsFilterBottomsheetDialog.BookingsFilter{
    Toolbar toolbar;
    String UID, gender, FILTER;
    LottieAnimationView loading_anim;
    RecyclerView recyclerView;
    LayoutInflater layoutInflater;
    BookingAdapter adapter = new BookingAdapter();
    LinkedHashMap<String, booking> bookings_hashmap = new LinkedHashMap<>();
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    String EMPTY = "EMPTY", LOADED = "LOADED";
    ConstraintLayout empty_layout, loaded_layout;
    Button show_providers;
    final int SUCCESSFUL = 1, FAILED = 2, COMPLETED = 3, MISSED = 4, CANCELED = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);
        //firebase auth
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //layout inflater
        layoutInflater = LayoutInflater.from(this);
        //initiating ui
        show_providers = findViewById(R.id.show_providers);
        empty_layout  = findViewById(R.id.empty_layout);
        loaded_layout = findViewById(R.id.loaded_layout);
        loading_anim = findViewById(R.id.bookings_loading_anim);loading_anim.playAnimation();
        toolbar=findViewById(R.id.toolbar10);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        //show_providers onClicl
        show_providers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent all_intent=new Intent(BookingsActivity.this,ProvidersActivity.class);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                gender = preferences.getString("gender", getResources().getString(R.string.male));
                all_intent.putExtra("gender",gender);
                startActivity(all_intent);
            }
        });
        //
        get_n_setDetails();
    }

    private void get_n_setDetails() {
        loading_anim.setVisibility(View.VISIBLE);
            DatabaseReference booking_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/");
            Query booking_query = booking_ref.child(UID).orderByChild("time_stamp").limitToLast(200);
            booking_query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot booking_dataSnapshot) {
                    if(booking_dataSnapshot.getValue() == null){
                        show(EMPTY);
                    }else{
                        for (final DataSnapshot dataSnapshot : booking_dataSnapshot.getChildren()) {
                            try {
                                //Log.d("datta_text", dataSnapshot.child("time_stamp").getValue().toString());
                                //date
                                final String key_date = dataSnapshot.child("booked_date").getValue().toString();
                                String booking_month = months[Integer.parseInt(key_date.substring(4, 6)) - 1];
                                final String booking_date = key_date.substring(6) + " " + booking_month + " " + key_date.substring(0, 4);
                                //time
                                double SELECTED_TIME = dataSnapshot.child("booked_time").getValue(Double.class);
                                final String time;
                                //setting start time
                                String noon = "AM";
                                if ((int) SELECTED_TIME > 11) {
                                    noon = "PM";
                                    time = Integer.toString((int) (SELECTED_TIME - 12)) + ":" + Integer.toString((int) ((SELECTED_TIME - (int) SELECTED_TIME) * 60)) + noon;
                                } else {
                                    time = Integer.toString((int) (SELECTED_TIME)) + ":" + Integer.toString((int) ((SELECTED_TIME - (int) SELECTED_TIME) * 60)) + noon;
                                }

                                DatabaseReference provider_ref = FirebaseDatabase.getInstance().getReference().child("providers/salons/" + dataSnapshot.child("provider_uid").getValue().toString());
                                provider_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot provider_dataSnapshot) {
                                        Log.d("datta_text", dataSnapshot.child("time_stamp").getValue().toString());
                                        bookings_hashmap.put(key_date + dataSnapshot.getKey().toString(), new booking(provider_dataSnapshot.child("name").getValue().toString(), provider_dataSnapshot.child("address").getValue().toString(),
                                                dataSnapshot.child("time_stamp").getValue().toString(), dataSnapshot.child("status").getValue().toString(), booking_date + ", " + time, "₹ "+dataSnapshot.child("t_charge").getValue().toString(), dataSnapshot.getKey().toString()));
                                        loading_anim.setVisibility(View.GONE);
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        loading_anim.setVisibility(View.GONE);
                                    }
                                });

                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    loading_anim.setVisibility(View.GONE);
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.booking_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.filter :  BookingsFilterBottomsheetDialog dialog = new BookingsFilterBottomsheetDialog();
                Bundle args = new Bundle();
                Toast.makeText(getApplicationContext(), FILTER, Toast.LENGTH_SHORT).show();
                args.putString("filter", FILTER);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "dialog");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void selectedFilter(String filter) {
        FILTER = filter;
        Log.d("datta_text", filter);
    }

    class BookingAdapter extends RecyclerView.Adapter{
        View v;
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType){
                case FAILED:return new FailureViewHolder(layoutInflater.inflate(R.layout.booking_single_failed, parent, false));
                case SUCCESSFUL:return new SuccessViewHolder(layoutInflater.inflate(R.layout.booking_single_success, parent, false));
                case COMPLETED:return new CompleteViewHolder(layoutInflater.inflate(R.layout.booking_single_completed, parent, false));
                case MISSED:return new MissedViewHolder(layoutInflater.inflate(R.layout.booking_single_missed, parent, false));
                case CANCELED:return new CancelledViewHolder(layoutInflater.inflate(R.layout.booking_single_canclled, parent, false));
            }
           return new FailureViewHolder(layoutInflater.inflate(R.layout.booking_single_failed, parent, false));
        }


        @Override
        public int getItemViewType(int position) {
            ArrayList<String> keys = new ArrayList<>(bookings_hashmap.keySet());
            booking mbooking = bookings_hashmap.get(keys.get( keys.size() - position - 1));
            String status = mbooking.getStatus();
            if(status.equals(getResources().getString(R.string.failed))){
                return FAILED;
            }
            else if(status.equals(getResources().getString(R.string.successful))){
                return SUCCESSFUL;
            }
            else if(status.equals(getResources().getString(R.string.completed))){
                return COMPLETED;
            }
            else if(status.equals(getResources().getString(R.string.missed))){
                return MISSED;
            }
            else if(status.equals(getResources().getString(R.string.canceled))){
                return CANCELED;
            }
            return FAILED;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ArrayList<String> keys = new ArrayList<>(bookings_hashmap.keySet());
            booking mbooking = bookings_hashmap.get(keys.get( keys.size() - position - 1));
            String status = mbooking.getStatus();
            if(status.equals(getResources().getString(R.string.failed))){
                ((FailureViewHolder)holder).setDetails(mbooking);
            }
            else if(status.equals(getResources().getString(R.string.successful))){
                ((SuccessViewHolder)holder).setDetails(mbooking);
            }
            else if(status.equals(getResources().getString(R.string.completed))){
                ((CompleteViewHolder)holder).setDetails(mbooking);
            }
            else if(status.equals(getResources().getString(R.string.missed))){
                ((MissedViewHolder)holder).setDetails(mbooking);
            }
            else if(status.equals(getResources().getString(R.string.canceled))){
                ((CancelledViewHolder)holder).setDetails(mbooking);
            }
        }

        @Override
        public int getItemCount() {
            return bookings_hashmap.size();
        }
    }

    class SuccessViewHolder extends RecyclerView.ViewHolder{
        View v;
        public SuccessViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setDetails(final booking booking) {
            final TextView salon_name = v.findViewById(R.id.salon_name);
            TextView date__n_time = v.findViewById(R.id.date_n_time);
            //TextView status_textView = v.findViewById(R.id.status);
            TextView booked_on = v.findViewById(R.id.booked_on);
            TextView charge =v.findViewById(R.id.money);
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            Date result_date = new Date(Long.parseLong(booking.getBooked_on()));
            String date = sdf.format(result_date);

            salon_name.setText(booking.getSalon_name());
            date__n_time.setText(booking.getBooked_date());
            booked_on.setText(date);
            charge.setText(booking.getCharge());
            v.setTag(booking.getTid());

            //status
            String status = booking.getStatus();
           /* if(status.equals(getResources().getString(R.string.failed))){
                //make text color red and dont put qrcode and seeds text
                status_textView.setTextColor(getResources().getColor(R.color.red400));
                status_textView.setText("Transaction Failed");
            }
            else if(status.equals(getResources().getString(R.string.successful))){
                status_textView.setText("Booking Successful");
            }
            else if(status.equals(getResources().getString(R.string.completed))){
                status_textView.setText("Booking Completed");
                status_textView.setTextColor(getResources().getColor(R.color.blue800));
            }
            else if(status.equals(getResources().getString(R.string.missed))){
                status_textView.setText("Booking Missed");
                status_textView.setTextColor(getResources().getColor(R.color.yellow800));
            }
            else if(status.equals(getResources().getString(R.string.canceled))){
                status_textView.setText("Booking Canceled");
                status_textView.setTextColor(getResources().getColor(R.color.red500));
            }*/
            //onClick
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent booking_result_intent = new Intent(BookingsActivity.this, BookingResult.class);
                    booking_result_intent.putExtra("tid", booking.getTid());
                    booking_result_intent.putExtra("name", salon_name.getText().toString());
                    booking_result_intent.putExtra("address", booking.getAddress());
                    startActivity(booking_result_intent);
                }
            });
        }
    }

    class FailureViewHolder extends RecyclerView.ViewHolder{
        View v;
        public FailureViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setDetails(final booking booking) {
            final TextView salon_name = v.findViewById(R.id.salon_name);
            TextView date__n_time = v.findViewById(R.id.date_n_time);
            //TextView status_textView = v.findViewById(R.id.status);
            TextView booked_on = v.findViewById(R.id.booked_on);
            TextView charge =v.findViewById(R.id.money);
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            Date result_date = new Date(Long.parseLong(booking.getBooked_on()));
            String date = sdf.format(result_date);

            salon_name.setText(booking.getSalon_name());
            date__n_time.setText(booking.getBooked_date());
            booked_on.setText(date);
            charge.setText(booking.getCharge());
            v.setTag(booking.getTid());

            //status
            String status = booking.getStatus();
           /* if(status.equals(getResources().getString(R.string.failed))){
                //make text color red and dont put qrcode and seeds text
                status_textView.setTextColor(getResources().getColor(R.color.red400));
                status_textView.setText("Transaction Failed");
            }
            else if(status.equals(getResources().getString(R.string.successful))){
                status_textView.setText("Booking Successful");
            }
            else if(status.equals(getResources().getString(R.string.completed))){
                status_textView.setText("Booking Completed");
                status_textView.setTextColor(getResources().getColor(R.color.blue800));
            }
            else if(status.equals(getResources().getString(R.string.missed))){
                status_textView.setText("Booking Missed");
                status_textView.setTextColor(getResources().getColor(R.color.yellow800));
            }
            else if(status.equals(getResources().getString(R.string.canceled))){
                status_textView.setText("Booking Canceled");
                status_textView.setTextColor(getResources().getColor(R.color.red500));
            }*/
            //onClick
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent booking_result_intent = new Intent(BookingsActivity.this, BookingResult.class);
                    booking_result_intent.putExtra("tid", booking.getTid());
                    booking_result_intent.putExtra("name", salon_name.getText().toString());
                    booking_result_intent.putExtra("address", booking.getAddress());
                    startActivity(booking_result_intent);
                }
            });
        }
    }

    class MissedViewHolder extends RecyclerView.ViewHolder{
        View v;
        public MissedViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setDetails(final booking booking) {
            final TextView salon_name = v.findViewById(R.id.salon_name);
            TextView date__n_time = v.findViewById(R.id.date_n_time);
            //TextView status_textView = v.findViewById(R.id.status);
            TextView booked_on = v.findViewById(R.id.booked_on);
            TextView charge =v.findViewById(R.id.money);
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            Date result_date = new Date(Long.parseLong(booking.getBooked_on()));
            String date = sdf.format(result_date);

            salon_name.setText(booking.getSalon_name());
            date__n_time.setText(booking.getBooked_date());
            booked_on.setText(date);
            charge.setText(booking.getCharge());
            v.setTag(booking.getTid());

            //status
            String status = booking.getStatus();
           /* if(status.equals(getResources().getString(R.string.failed))){
                //make text color red and dont put qrcode and seeds text
                status_textView.setTextColor(getResources().getColor(R.color.red400));
                status_textView.setText("Transaction Failed");
            }
            else if(status.equals(getResources().getString(R.string.successful))){
                status_textView.setText("Booking Successful");
            }
            else if(status.equals(getResources().getString(R.string.completed))){
                status_textView.setText("Booking Completed");
                status_textView.setTextColor(getResources().getColor(R.color.blue800));
            }
            else if(status.equals(getResources().getString(R.string.missed))){
                status_textView.setText("Booking Missed");
                status_textView.setTextColor(getResources().getColor(R.color.yellow800));
            }
            else if(status.equals(getResources().getString(R.string.canceled))){
                status_textView.setText("Booking Canceled");
                status_textView.setTextColor(getResources().getColor(R.color.red500));
            }*/
            //onClick
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent booking_result_intent = new Intent(BookingsActivity.this, BookingResult.class);
                    booking_result_intent.putExtra("tid", booking.getTid());
                    booking_result_intent.putExtra("name", salon_name.getText().toString());
                    booking_result_intent.putExtra("address", booking.getAddress());
                    startActivity(booking_result_intent);
                }
            });
        }
    }

    class CompleteViewHolder extends RecyclerView.ViewHolder{
        View v;
        public CompleteViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setDetails(final booking booking) {
            final TextView salon_name = v.findViewById(R.id.salon_name);
            TextView date__n_time = v.findViewById(R.id.date_n_time);
            //TextView status_textView = v.findViewById(R.id.status);
            TextView booked_on = v.findViewById(R.id.booked_on);
            TextView charge =v.findViewById(R.id.money);
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            Date result_date = new Date(Long.parseLong(booking.getBooked_on()));
            String date = sdf.format(result_date);

            salon_name.setText(booking.getSalon_name());
            date__n_time.setText(booking.getBooked_date());
            booked_on.setText(date);
            charge.setText(booking.getCharge());
            v.setTag(booking.getTid());

            //status
            String status = booking.getStatus();
           /* if(status.equals(getResources().getString(R.string.failed))){
                //make text color red and dont put qrcode and seeds text
                status_textView.setTextColor(getResources().getColor(R.color.red400));
                status_textView.setText("Transaction Failed");
            }
            else if(status.equals(getResources().getString(R.string.successful))){
                status_textView.setText("Booking Successful");
            }
            else if(status.equals(getResources().getString(R.string.completed))){
                status_textView.setText("Booking Completed");
                status_textView.setTextColor(getResources().getColor(R.color.blue800));
            }
            else if(status.equals(getResources().getString(R.string.missed))){
                status_textView.setText("Booking Missed");
                status_textView.setTextColor(getResources().getColor(R.color.yellow800));
            }
            else if(status.equals(getResources().getString(R.string.canceled))){
                status_textView.setText("Booking Canceled");
                status_textView.setTextColor(getResources().getColor(R.color.red500));
            }*/
            //onClick
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent booking_result_intent = new Intent(BookingsActivity.this, BookingResult.class);
                    booking_result_intent.putExtra("tid", booking.getTid());
                    booking_result_intent.putExtra("name", salon_name.getText().toString());
                    booking_result_intent.putExtra("address", booking.getAddress());
                    startActivity(booking_result_intent);
                }
            });
        }
    }

    class CancelledViewHolder extends RecyclerView.ViewHolder{
        View v;
        public CancelledViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setDetails(final booking booking) {
            final TextView salon_name = v.findViewById(R.id.salon_name);
            TextView date__n_time = v.findViewById(R.id.date_n_time);
            //TextView status_textView = v.findViewById(R.id.status);
            TextView booked_on = v.findViewById(R.id.booked_on);
            TextView charge =v.findViewById(R.id.money);
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            Date result_date = new Date(Long.parseLong(booking.getBooked_on()));
            String date = sdf.format(result_date);

            salon_name.setText(booking.getSalon_name());
            date__n_time.setText(booking.getBooked_date());
            booked_on.setText(date);
            charge.setText(booking.getCharge());
            v.setTag(booking.getTid());

            //status
            String status = booking.getStatus();
           /* if(status.equals(getResources().getString(R.string.failed))){
                //make text color red and dont put qrcode and seeds text
                status_textView.setTextColor(getResources().getColor(R.color.red400));
                status_textView.setText("Transaction Failed");
            }
            else if(status.equals(getResources().getString(R.string.successful))){
                status_textView.setText("Booking Successful");
            }
            else if(status.equals(getResources().getString(R.string.completed))){
                status_textView.setText("Booking Completed");
                status_textView.setTextColor(getResources().getColor(R.color.blue800));
            }
            else if(status.equals(getResources().getString(R.string.missed))){
                status_textView.setText("Booking Missed");
                status_textView.setTextColor(getResources().getColor(R.color.yellow800));
            }
            else if(status.equals(getResources().getString(R.string.canceled))){
                status_textView.setText("Booking Canceled");
                status_textView.setTextColor(getResources().getColor(R.color.red500));
            }*/
            //onClick
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent booking_result_intent = new Intent(BookingsActivity.this, BookingResult.class);
                    booking_result_intent.putExtra("tid", booking.getTid());
                    booking_result_intent.putExtra("name", salon_name.getText().toString());
                    booking_result_intent.putExtra("address", booking.getAddress());
                    startActivity(booking_result_intent);
                }
            });
        }
    }



    void show(String state){
        if(state.equals(EMPTY)){
            loaded_layout.setVisibility(View.GONE);
            empty_layout.setVisibility(View.VISIBLE);
        }
    }

    /*class booking{
       String salon_name, address, booked_on, status, booked_date, charge, tid;

        public booking(String salon_name, String address, String booked_on, String status, String booked_date, String charge, String tid) {
            this.salon_name = salon_name;
            this.address = address;
            this.booked_on = booked_on;
            this.status = status;
            this.booked_date = booked_date;
            this.charge = charge;
            this.tid = tid;
        }

        public String getSalon_name() {
            return salon_name;
        }

        public String getAddress() {
            return address;
        }

        public String getBooked_on() {
            return booked_on;
        }

        public String getStatus() {
            return status;
        }

        public String getBooked_date() {
            return booked_date;
        }

        public String getCharge() {
            return charge;
        }

        public String getTid() {
            return tid;
        }
    }*/
}
