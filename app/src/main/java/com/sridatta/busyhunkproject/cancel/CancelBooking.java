package com.sridatta.busyhunkproject.cancel;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sridatta.busyhunkproject.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CancelBooking {
    cancelResult cancel_result;
    Activity activity;
    int NOTPOSSIBLE = 0, DONE = 1, FAILED = -1, WALLET_CASH;
    String SELECTED_DATE;

    public void get_n_setDetails(final String TID, final Activity activity) {
        //activity
        this.activity = activity;
        //database
        DatabaseReference transaction_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+TID);
        transaction_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //time
                double SELECTED_TIME = Double.parseDouble(dataSnapshot.child("booked_time").getValue().toString());
                //date
                SELECTED_DATE = dataSnapshot.child("booked_date").getValue().toString();
                //wallet cash
                WALLET_CASH = Integer.parseInt(dataSnapshot.child("wallet_cash").getValue().toString());
                //status
                String status = dataSnapshot.child("status").getValue().toString();
                //cancel button setup
                if(status.equals(activity.getResources().getString(R.string.successful))){
                    checkCancelPossibility(Long.parseLong(SELECTED_DATE), SELECTED_TIME, TID);
                }else {
                    sendResponse(NOTPOSSIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text", "erro while getting transaction details in CancelBooking Class: "+ databaseError.toString() );
            }
        });
    }

    //this method set cancel button visibility based on time from now to booking
    void checkCancelPossibility(long bookedDate, double bookedTime, final String TID){
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
                       cancel(TID);
                    }
                    else{
                       sendResponse(NOTPOSSIBLE);
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

    private void cancel(final String tid) {
        DatabaseReference transaction_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+tid);
        transaction_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //time
                double SELECTED_TIME = Double.parseDouble(dataSnapshot.child("booked_time").getValue().toString());
                //date
                String SELECTED_DATE = dataSnapshot.child("booked_date").getValue().toString();
                //
                String provider_uid = dataSnapshot.child("provider_uid").getValue().toString();

                //remove availability
                removeAvailability(tid, provider_uid, SELECTED_DATE, SELECTED_TIME);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text", "erro while getting transaction details in CancelBooking Class: "+ databaseError.toString() );
            }
        });
    }

    private void removeAvailability(final String tid, String provider_uid, String selected_date, double selected_time) {
        final DatabaseReference availRef = FirebaseDatabase.getInstance().getReference().child("availability/salons/" + selected_date + "/" +provider_uid);
        availRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2: dataSnapshot1.getChildren()){
                        if(dataSnapshot2.getKey().toString().equals(tid)){
                            availRef.child(dataSnapshot1.getKey().toString() + "/" + dataSnapshot2.getKey().toString()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        changeTransStatus(tid);
                                    }
                                    else{
                                        sendResponse(FAILED);
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void changeTransStatus(final String tid) {
        DatabaseReference transaction_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+tid+"/status");
        transaction_ref.setValue(activity.getResources().getString(R.string.canceled)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    requestCancellation(tid);
                }
                else {
                    sendResponse(FAILED);
                }
            }
        });
    }

    private void requestCancellation(String tid) {
        DatabaseReference cancel_ref = FirebaseDatabase.getInstance().getReference().child("cancellations/salons/" + SELECTED_DATE + "/" + tid);
        HashMap<String, String> cancel = new HashMap<>();
        cancel.put("uid",FirebaseAuth.getInstance().getUid().toString() );
        cancel.put("status", activity.getResources().getString(R.string.pending));
        cancel_ref.setValue(cancel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    sendResponse(DONE);
                }else{
                    sendResponse(FAILED);
                }
            }
        });
    }

    void sendResponse(int response){
        cancel_result = (cancelResult) activity;
        cancel_result.result(response);
    }
    public interface cancelResult{
        void result(int response);
    }

}
