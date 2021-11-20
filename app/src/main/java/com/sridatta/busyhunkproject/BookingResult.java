package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sridatta.busyhunkproject.cancel.CancelBooking;
import com.sridatta.busyhunkproject.cancel.CancelBookingFragment;
import com.sridatta.busyhunkproject.cancel.CancelUnableDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BookingResult extends AppCompatActivity implements CancelBookingFragment.showLoading, CancelBooking.cancelResult {
    private String TID = null, PROVIDER_NAME, ADDRESS;
    Boolean back_enabled = true;
    ConstraintLayout loading_layout, booking_layout, internet_layout;
    TextView status_textView, tid_textview, transaction_date_textview, direct_pay, wallet_pay,
            provider_name, provider_address, total_charge_textview, total_span_textview, start_time_textView, date_textView, note, promocode_textview;
    final String BOOKING="BOOING",INTERNET="INTERNET",LOADING="LOADING";
    HashMap<String, SalonServicesActivity.service> serviceHashMap = new HashMap<>();
    LayoutInflater layoutInflater;
    RecyclerView recyclerView;
    CustomServiceAdapter adapter = new CustomServiceAdapter();
    Toolbar toolbar;
    ImageView imageViewQrCode;
    Button home, cancel_booking;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_result);
        //inflater
        layoutInflater = LayoutInflater.from(getApplicationContext());
        //intent
        TID = getIntent().getStringExtra("tid");
        PROVIDER_NAME =getIntent().getStringExtra("name");
        ADDRESS = getIntent().getStringExtra("address");
        back_enabled = getIntent().getBooleanExtra("back_enabled", true);
        //Toast.makeText(getApplicationContext(), TID, Toast.LENGTH_SHORT).show();
        if(TID==null || TID.equals("")){
            finish();
        }


        //initiating ui
        note = findViewById(R.id.textView80);
        imageViewQrCode = findViewById(R.id.qrCode);
        cancel_booking =  findViewById(R.id.cancel_booking);
        direct_pay = findViewById(R.id.direct_pay);
        wallet_pay = findViewById(R.id.wallet_pay);
        loading_layout = findViewById(R.id.loading_layout);
        booking_layout = findViewById(R.id.booking_layout);
        internet_layout = findViewById(R.id.internet_layout);
        home = findViewById(R.id.button2);
        provider_name = findViewById(R.id.salon_name); provider_name.setText(PROVIDER_NAME);
        provider_address = findViewById(R.id.address); provider_address.setText(ADDRESS);
        status_textView = findViewById(R.id.textView37);
        tid_textview = findViewById(R.id.textView55);tid_textview.setText(TID);
        transaction_date_textview = findViewById(R.id.textView51);
        start_time_textView = findViewById(R.id.start_time);
        total_charge_textview = findViewById(R.id.total_charge);
        total_span_textview = findViewById(R.id.total_span);
        date_textView = findViewById(R.id.date);
        recyclerView = findViewById(R.id.selected_services_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new CustomServiceAdapter();
        recyclerView.setAdapter(adapter);
        toolbar = findViewById(R.id.toolbar_title);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //initially setting loading layout
        setLayout(LOADING);

        //back to home home onclicl
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main_activity_intent = new Intent(BookingResult.this, MainActivityLatest.class);
                main_activity_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(main_activity_intent);
            }
        });
        get_n_setDetails();

        //cancel onclick
        cancel_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CancelBookingFragment cancelBookingFragment = new CancelBookingFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                cancelBookingFragment.setStyle(android.app.DialogFragment.STYLE_NO_FRAME,R.style.DialogFragmentTheme);
                cancelBookingFragment.show(ft, "dialog");
               /* FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                CancelBookingFragment cancelBookingFragment = new CancelBookingFragment();
                cancelBookingFragment.setStyle(DialogFragment.STYLE_NO_FRAME,R.style.DialogFragmentTheme);
                cancelBookingFragment.show(ft, "dialog");*/
            }
        });

    }

    @Override
    public void onBackPressed() {
        //disabling back button if user come from booking
        if(!back_enabled){
            Intent main_activity_intent = new Intent(BookingResult.this, MainActivityLatest.class);
            main_activity_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main_activity_intent);
        }
        else {
            super.onBackPressed();
        }
    }

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

    private void get_n_setDetails() {
        DatabaseReference transaction_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+TID);
        transaction_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //getting status and setting color according to it
                String status = dataSnapshot.child("status").getValue().toString();
                if(status.equals(getResources().getString(R.string.failed))){
                    //make text color red and dont put qrcode and seeds text
                    status_textView.setTextColor(getResources().getColor(R.color.red400));
                    status_textView.setText("Transaction Failed");
                    note.setVisibility(View.GONE);
                    cancel_booking.setVisibility(View.GONE);
                }
                else if(status.equals(getResources().getString(R.string.successful))){
                    status_textView.setText("Booking Successful");
                    //qrcode generation
                    try {
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.encodeBitmap(TID, BarcodeFormat.QR_CODE, 400, 400);
                        imageViewQrCode.setImageBitmap(bitmap);
                    } catch(Exception e) {

                    }
                }
                else if(status.equals(getResources().getString(R.string.completed))){
                    status_textView.setText("Booking Completed");
                    status_textView.setTextColor(getResources().getColor(R.color.blue800));
                    note.setVisibility(View.GONE);
                    cancel_booking.setVisibility(View.GONE);
                }
                else if(status.equals(getResources().getString(R.string.missed))){
                    status_textView.setText("Booking Missed");
                    status_textView.setTextColor(getResources().getColor(R.color.yellow800));
                    note.setVisibility(View.GONE);
                    cancel_booking.setVisibility(View.GONE);
                }
                else if(status.equals(getResources().getString(R.string.canceled))){
                    status_textView.setText("Booking Canceled");
                    status_textView.setTextColor(getResources().getColor(R.color.red500));
                    note.setVisibility(View.GONE);
                    cancel_booking.setVisibility(View.GONE);
                }

                //date
                String time_stamp = dataSnapshot.child("time_stamp").getValue().toString();
                DateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
                Date result_date = new Date(Long.parseLong(time_stamp));
                String date = sdf.format(result_date);
                transaction_date_textview.setText("on "+date);

                //payment
                String wallet_cash = dataSnapshot.child("wallet_cash").getValue().toString();
                String total_charge = dataSnapshot.child("t_charge").getValue().toString();
                int DIRECTPAY = Integer.parseInt(total_charge) - Integer.parseInt(wallet_cash);
                wallet_pay.setText("₹" + wallet_cash);
                direct_pay.setText("₹" + Integer.toString(DIRECTPAY));


                //summary
                total_charge_textview.setText("₹" + total_charge);
                double total_time = Double.parseDouble(dataSnapshot.child("t_span").getValue().toString());
                String hour = Integer.toString((int) total_time);
                String min = Integer.toString((int) ((total_time - (int) total_time) * 60));
                if ((int) total_time <= 0) {
                    total_span_textview.setText(min + " mins");
                } else if ((int) ((total_time - (int) total_time) * 60) == 0) {
                    total_span_textview.setText(hour + " hour");
                } else {
                    total_span_textview.setText(hour + " hours " + min + " mins");
                }

                double SELECTED_TIME = Double.parseDouble(dataSnapshot.child("booked_time").getValue().toString());
                //setting start time
                String noon = "AM";
                if ((int) SELECTED_TIME > 11) {
                    noon = "PM";
                    start_time_textView.setText(Integer.toString((int) (SELECTED_TIME - 12)) + ":" + Integer.toString((int) ((SELECTED_TIME - (int) SELECTED_TIME) * 60)) + noon);
                } else {
                    start_time_textView.setText(Integer.toString((int) (SELECTED_TIME)) + ":" + Integer.toString((int) ((SELECTED_TIME - (int) SELECTED_TIME) * 60)) + noon);
                }
                //date
                String SELECTED_DATE = dataSnapshot.child("booked_date").getValue().toString();
                String selected_date = SELECTED_DATE.substring(6) + " " + months[Integer.parseInt(SELECTED_DATE.substring(4, 6)) - 1] + " " + SELECTED_DATE.substring(0,4);
                date_textView.setText(selected_date);

                //services
                DataSnapshot services_snapshot = dataSnapshot.child("services");
                for(DataSnapshot dataSnapshot1 : services_snapshot.getChildren()){
                    String service_name = dataSnapshot1.getKey().toString();
                    String charge = dataSnapshot1.child("curr_charge").getValue().toString();
                    String span = dataSnapshot1.child("span").getValue().toString();
                    SalonServicesActivity.service service = new SalonServicesActivity.service(null, null, service_name, charge, charge, span, 0, 0, 0, 0, true);
                    serviceHashMap.put(service_name, service);
                    //
                    adapter.notifyDataSetChanged();
                    setLayout(BOOKING);
                }

                //cancel button setup
                setCancelPossibility(Long.parseLong(SELECTED_DATE), SELECTED_TIME);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text", "erro while getting transaction details in result activity: "+ databaseError.toString() );
                setLayout(INTERNET);
            }
        });
    }

    //this method set cancel button visibility based on time from now to booking
    void setCancelPossibility(long bookedDate, double bookedTime){
        //initially make visibility gone
//        cancel_booking.setVisibility(View.GONE);
        //make a string in sdf
        //convert string to time stamp
        String booked_time;
        if((int)bookedTime < 10){
            booked_time = "0"+Integer.toString((int)bookedTime) + " " + Double.toString((bookedTime - (int)bookedTime)*60);
        }
        else{
            booked_time = Integer.toString((int)bookedTime) + " " + Double.toString((bookedTime - (int)bookedTime)*60);

        }
        if(bookedTime - (int)bookedTime == 0){
            if((int)bookedTime < 10){
                booked_time = "0" + Integer.toString((int)bookedTime) + " 00";
            }
        }
        String booked_date_time = Long.toString(bookedDate) + " " + booked_time;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH mm");
        Date mDate = null;
        try {
            mDate = sdf.parse(booked_date_time);
            final long bookedTimeInMilliseconds = mDate.getTime();
            //getting offset time
            DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference().child(".info/serverTimeOffset");
            offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long offset = dataSnapshot.getValue(Long.class);
                    long estimatedServerTimeMs = System.currentTimeMillis() + offset;
                    //cancelation possible before 6 hours
                    if(bookedTimeInMilliseconds - estimatedServerTimeMs > 21600){
//                        Toast.makeText(getApplicationContext(),Integer.toString((int)(bookedTimeInMilliseconds - estimatedServerTimeMs)), Toast.LENGTH_SHORT ).show();
                        cancel_booking.setVisibility(View.VISIBLE);
                        cancel_booking.requestFocus();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void setLayout(String s){
        allGone();
        switch (s){
            case INTERNET:internet_layout.setVisibility(View.VISIBLE);break;
            case LOADING:loading_layout.setVisibility(View.VISIBLE);break;
            case BOOKING:booking_layout.setVisibility(View.VISIBLE);break;
        }
    }

    void allGone(){
        booking_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.GONE);
        internet_layout.setVisibility(View.GONE);
    }

    @Override
    public void load() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        loadingDialogFragment.setStyle(android.app.DialogFragment.STYLE_NO_FRAME,R.style.DialogFragmentTheme);
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(ft, "dialog");
        new CancelBooking().get_n_setDetails(TID, BookingResult.this);
    }

    @Override
    public void result(int response) {
        loadingDialogFragment.dismiss();
        if(response == 0){
            CancelUnableDialog cancelUnableDialog = new CancelUnableDialog();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            cancelUnableDialog.show(ft, "dialog");
            loadingDialogFragment.dismiss();
        }else if(response == 1) {
            Toast.makeText(getApplicationContext(), "Booking canceled", Toast.LENGTH_SHORT).show();
            cancel_booking.setVisibility(View.GONE);
            loadingDialogFragment.dismiss();
        }
        else{
            Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
            loadingDialogFragment.dismiss();
        }
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

}
