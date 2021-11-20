package com.sridatta.busyhunkproject;

import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import net.igenius.customcheckbox.CustomCheckBox;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class RewardBookNow extends AppCompatActivity {
    Toolbar toolbar;
    String PROVIDER_UID, GENDER, UID, SELECTED_DATE, SELECTED_PROMOCODE = "REFERRAL100", REFERRAL_REWARD_CATEGORY;
    int total_cost, SELECTED_SEAT_POSITION, SERVICE_CHARGE = 9;
    double total_time, SELECTED_TIME;
    HashMap<String, SalonServicesActivity.service> serviceHashMap = new HashMap<>();
    private NestedScrollView nestedScrollView;
    private ConstraintLayout loading_layout;
    TextView provider_name, provider_address, total_charge_textview, total_span_textview, start_time_textView, date_textView, total_pay, direct_pay, note;
    LayoutInflater layoutInflater;
    CustomServiceAdapter adapter;
    RecyclerView recyclerView;
    Button open_maps, book_now;
    private final int GENERATE_TID = 2;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Now", "Dec"};
    LoadingDialogFragment loadingdialog = new LoadingDialogFragment();
    ProgressBar book_now_progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_book_now);
        //uid
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //inflater
        layoutInflater = LayoutInflater.from(getApplicationContext());
        //getting intent data
        REFERRAL_REWARD_CATEGORY = getIntent().getStringExtra("referral_reward_category");
        PROVIDER_UID = getIntent().getStringExtra("uid");
        GENDER = getIntent().getStringExtra("gender");
        total_cost = getIntent().getIntExtra("t_charge", 0) + SERVICE_CHARGE;
        total_time = getIntent().getDoubleExtra("t_time", 0);
        SELECTED_TIME = getIntent().getDoubleExtra("selected_time", 0);
        SELECTED_DATE = getIntent().getStringExtra("selected_date");
        SELECTED_SEAT_POSITION = getIntent().getIntExtra("selected_seat", 1);
        serviceHashMap = (HashMap<String, SalonServicesActivity.service>) getIntent().getSerializableExtra("services_list");
        //initiating ui
        note = findViewById(R.id.note);
        book_now_progressbar = findViewById(R.id.button_progressbar);
        total_pay = findViewById(R.id.total_pay);
        direct_pay = findViewById(R.id.direct_pay);
        date_textView = findViewById(R.id.date);
        open_maps = findViewById(R.id.open_maps);
        //progressDialog = new ProgressDialog(this);
        book_now = findViewById(R.id.book_now);
        start_time_textView = findViewById(R.id.start_time);
        total_charge_textview = findViewById(R.id.total_charge);
        total_span_textview = findViewById(R.id.total_span);
        provider_name = findViewById(R.id.textView38);
        provider_address = findViewById(R.id.textView40);
        nestedScrollView = findViewById(R.id.parent_layout);
        loading_layout = findViewById(R.id.loading_layout);
        toolbar = findViewById(R.id.toolbar12);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.selected_services_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new CustomServiceAdapter();
        recyclerView.setAdapter(adapter);

        //set provider details
        setProviderDetails();

        //setting summary details
        total_charge_textview.setText("₹" + Integer.toString(total_cost - removal_price(REFERRAL_REWARD_CATEGORY)));
        String hour = Integer.toString((int) total_time);
        String min = Integer.toString((int) ((total_time - (int) total_time) * 60));
        if ((int) total_time <= 0) {
            total_span_textview.setText(min + " mins");
        } else if ((int) ((total_time - (int) total_time) * 60) == 0) {
            total_span_textview.setText(hour + " hour");
        } else {
            total_span_textview.setText(hour + " hours " + min + " mins");
        }
        //setting start time
        String noon = "AM";
        if ((int) SELECTED_TIME > 11) {
            noon = "PM";
            start_time_textView.setText(Integer.toString((int) (SELECTED_TIME - 12)) + ":" + Integer.toString((int) ((SELECTED_TIME - (int) SELECTED_TIME) * 60)) + noon);
        } else {
            start_time_textView.setText(Integer.toString((int) (SELECTED_TIME)) + ":" + Integer.toString((int) ((SELECTED_TIME - (int) SELECTED_TIME) * 60)) + noon);
        }
        //date
        String selected_date = SELECTED_DATE.substring(6) + " " + months[Integer.parseInt(SELECTED_DATE.substring(4, 6)) - 1] + " " + SELECTED_DATE.substring(0,4);
        date_textView.setText(selected_date);
        adapter.notifyDataSetChanged();

        //setting payment details
        //initially set direct pay as total pay by reducing 100 rs
        int removal_price = 0;
        removal_price = removal_price(REFERRAL_REWARD_CATEGORY);
        total_pay.setText("₹" + Integer.toString(total_cost - removal_price));
        direct_pay.setText("₹" + Integer.toString(total_cost - removal_price));
        note.setText("you are getting this booking at an effective price of ₹" +Integer.toString(total_cost - removal_price) + " with benifit of ₹" + Integer.toString(removal_price) + " on " + REFERRAL_REWARD_CATEGORY );

        open_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("providers/salons/" + PROVIDER_UID + "/coordinates");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String latitude = dataSnapshot.child("latitude").getValue().toString();
                        String longitude = dataSnapshot.child("longitude").getValue().toString();
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude));
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    int removal_price(String category) {
        int remove = 0;
        ArrayList<String> keys = new ArrayList<>(serviceHashMap.keySet());
        for (int i = 0; i < serviceHashMap.size(); i++) {
            if (serviceHashMap.get(keys.get(i)).getCategory().equals(category)) {
                remove = remove + serviceHashMap.get(keys.get(i)).getCharge_in_num();
            }
        }

        if(remove > 100){
            return 100;
        }
        return remove;
    }

    public boolean hasCategory(String category){
        boolean has_category=true;
        try {
            if (!category.equals(getResources().getString(R.string.no))) {
                has_category = false;
                ArrayList<String> keys = new ArrayList<>(serviceHashMap.keySet());
                for (int i = 0; i < serviceHashMap.size(); i++) {
                    if (serviceHashMap.get(keys.get(i)).getCategory().equals(category)) {
                        has_category = true;
                        break;
                    }
                }
            }
        }catch (Exception e){}
        return has_category;
    }

    public void bookNow(View v) {
        startButtonLoading();
        checkDatabaseConnection(GENERATE_TID);
    }

    private void checkDatabaseConnection(final int i) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    switch (i) {
                        case GENERATE_TID:
                            generateTID(0, 0);
                            break;
                    }
                } else {
                    stopButtonLoading();
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.makeText(getApplicationContext(), "Booking Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text", "onCancelled at connection reference " + databaseError.toString());
            }
        });
    }

    private void generateTID(final double wallet_cash, final int deduct_cash) {
        //getting server time
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                Date resultdate = new Date(estimatedServerTimeMs);
                Log.d("datta_text", "date from firebase is " + sdf.format(resultdate).toString());
                //generating random string
                String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                StringBuilder salt = new StringBuilder();
                Random rnd = new Random();
                while (salt.length() < 11) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String TID = salt.toString();
                storeTransaction(TID, wallet_cash, deduct_cash, estimatedServerTimeMs);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                stopButtonLoading();
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setText("Booking failed!");
                toast.show();
                Log.d("datta_text", "error is " + error.toString());
            }
        });
    }

    private void storeTransaction(final String tid, final double wallet_cash, final int deduct_cash, final long serverTimeMs) {
        HashMap<String, Object> transaction = new HashMap<>();
        HashMap<String, Object> services = new HashMap<>();
        ArrayList<String> keys = new ArrayList<>(serviceHashMap.keySet());
        transaction.put("provider_uid", PROVIDER_UID);
        for (int i = 0; i < serviceHashMap.size(); i++) {
            SalonServicesActivity.service current_service = serviceHashMap.get(keys.get(i));
            HashMap<String, String> single_service = new HashMap<>();
            single_service.put("curr_charge", current_service.getCurr_charge());
            single_service.put("span", current_service.getSpan());
            services.put(current_service.getService_name(), single_service);
        }
        transaction.put("services", services);
        transaction.put("t_span", total_time);
        transaction.put("t_charge", total_cost - removal_price(REFERRAL_REWARD_CATEGORY));
        transaction.put("status", getResources().getString(R.string.failed));
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
//        String date = sdf.format(serverTimeMs);
//        transaction.put("booked_on", date);
        transaction.put("booked_date", SELECTED_DATE);
        transaction.put("booked_time", SELECTED_TIME);
        transaction.put("wallet_cash", deduct_cash);
        transaction.put("time_stamp", ServerValue.TIMESTAMP);

        //DatabaseReference trans_ref = FirebaseDatabase.getInstance().getReference().child("availability/salons/" + SELECTED_DATE + "/" + PROVIDER_UID + "s" + Integer.toString(SELECTED_SEAT_POSITION));
        DatabaseReference trans_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + UID +"/"+tid);
        trans_ref.setValue(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateAvailability(tid, wallet_cash, deduct_cash, serverTimeMs);
                } else {
                    stopButtonLoading();
                    Toast.makeText(getApplicationContext(), "Booking Failed", Toast.LENGTH_SHORT).show();
                    Log.d("datta_text", "failed to make transaction");
                }
            }
        });

    }

    private void updateAvailability(final String tid, final double wallet_cash, final int deduct_cash, final long serverTimeMs) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    final DatabaseReference avail_ref = FirebaseDatabase.getInstance().getReference().child("availability/salons/" + SELECTED_DATE + "/" + PROVIDER_UID + "/s" + Integer.toString(SELECTED_SEAT_POSITION));
                    //checking availability
                    avail_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("datta_text", "checking availability");
                            Log.d("datta_text", "selected time is " + Double.toString(SELECTED_TIME));
                            boolean is_available = true;
                            for (DataSnapshot timing_snapshot : dataSnapshot.getChildren()) {
                                double session_start_time = Double.parseDouble(timing_snapshot.child("start_time").getValue().toString());
                                double session_end_time = Double.parseDouble(timing_snapshot.child("end_time").getValue().toString());
                                for (double i = session_start_time; i < session_end_time; i = i + 0.25) {
                                    Log.d("datta_text", "slot is " + Double.toString(i));
                                    if (SELECTED_TIME == i) {
                                        is_available = false;
                                        Log.d("datta_text", "slot is already booked " + Double.toString(i));
                                        break;
                                    }
                                }
                            }
                            //if available booking slot
                            if (is_available) {
                                HashMap<String, Object> booking_hashmap = new HashMap<>();
                                booking_hashmap.put("start_time", SELECTED_TIME);
                                booking_hashmap.put("end_time", Double.toString(SELECTED_TIME + total_time));
                                booking_hashmap.put("uid", UID);
                                avail_ref.child(tid).setValue(booking_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                                addCashback(tid, wallet_cash, deduct_cash, serverTimeMs);
                                        } else {
                                            openResultActivity(tid);
                                        }
                                    }
                                });
                            }
                            else{
                                openResultActivity(tid);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            stopButtonLoading();
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            openResultActivity(tid);
                            Log.d("datta_text", "database error while getting availability");
                        }
                    });

                } else {
                    stopButtonLoading();
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.makeText(getApplicationContext(), "Booking Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text", "onCancelled at connection reference " + databaseError.toString());
            }
        });


    }

    private void addCashback(final String tid, final double wallet_cash, final int deduct_cash, final long serverTimeMs) {
        DatabaseReference cashback_ref = FirebaseDatabase.getInstance().getReference().child("offer_usage/promo_codes/"+ SELECTED_PROMOCODE + "/" + UID +"/"+ tid);
        HashMap<String, Object> cashback = new HashMap<>();
        cashback.put("status", "pending");
        cashback.put("promocode", SELECTED_PROMOCODE);
        cashback_ref.setValue(cashback).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                        DatabaseReference trans_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + UID +"/"+tid + "/status");
                        trans_ref.setValue(getResources().getString(R.string.successful)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                openResultActivity(tid);
                            }
                        });

                } else {
                    openResultActivity(tid);
                    Log.d("datta_text", "problem to add cashback");
                }
            }
        });
    }


    private void openResultActivity(String tid) {
        //  book_now.doneLoadingAnimation(Color.parseColor("#333639"), BitmapFactory.decodeResource(getResources(), R.drawable.tick));
        Intent booking_result_intent = new Intent(RewardBookNow.this, BookingResult.class);
        Pair[] pair1=new Pair[2];
        pair1[0]=new Pair(provider_name,"provider_name");
        pair1[1]=new Pair(provider_address,"address");
        ActivityOptions options1= ActivityOptions.makeSceneTransitionAnimation(RewardBookNow.this,pair1);
        booking_result_intent.putExtra("tid", tid);
        booking_result_intent.putExtra("name", provider_name.getText().toString());
        booking_result_intent.putExtra("address", provider_address.getText().toString());
        startActivity(booking_result_intent, options1.toBundle());
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setProviderDetails() {
        //after getting provider details get seed rate
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("providers/salons/" + PROVIDER_UID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    provider_name.setText(dataSnapshot.child("name").getValue().toString());
                    provider_address.setText(dataSnapshot.child("address").getValue().toString());
                    //
                } catch (Exception e) {
                    Log.d("datta_text", "error in getting details: " + e.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text", "database error in getting details: " + databaseError.toString());
            }
        });
    }

    class CustomServiceAdapter extends RecyclerView.Adapter<CustomServiceViewHolder> {
        View v;

        @Override
        public CustomServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v = layoutInflater.inflate(R.layout.service_single_layout, parent, false);
            return new CustomServiceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CustomServiceViewHolder holder, int position) {
            ArrayList<String> keys = new ArrayList<>(serviceHashMap.keySet());
            holder.setService(serviceHashMap.get(keys.get(position)));
        }

        @Override
        public int getItemCount() {
            return serviceHashMap.size();
        }
    }

    class CustomServiceViewHolder extends RecyclerView.ViewHolder {
        View v;

        public CustomServiceViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }


        public void setService(SalonServicesActivity.service service) {
            TextView service_name, charge, striked_charge, span;
            service_name = v.findViewById(R.id.service_name);
            charge = v.findViewById(R.id.price);
            striked_charge = v.findViewById(R.id.act_price);
            span = v.findViewById(R.id.span);
            View strike_view = v.findViewById(R.id.view17);

            service_name.setText(service.getService_name());
            charge.setText("₹" + service.getCurr_charge());
            striked_charge.setText("₹" + service.getAct_charge());
            span.setText(service.getSpan());
            if (service.getCurr_charge().equals(service.getAct_charge())) {
                striked_charge.setVisibility(View.GONE);
                strike_view.setVisibility(View.GONE);
            }
        }
    }

    void showLoadingFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        loadingdialog.setStyle(DialogFragment.STYLE_NO_FRAME,R.style.DialogFragmentTheme);
        loadingdialog.show(ft, "dialog");
    }

    void dissmissLoadingFragment(){
        loadingdialog.dismissFragment();
    }

    void startButtonLoading(){
        book_now.setVisibility(View.GONE);
        book_now_progressbar.setVisibility(View.VISIBLE);
    }

    void stopButtonLoading(){
        book_now.setVisibility(View.VISIBLE);
        book_now_progressbar.setVisibility(View.GONE);
    }
}
