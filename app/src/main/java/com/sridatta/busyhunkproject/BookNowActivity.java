package com.sridatta.busyhunkproject;

import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;



public class BookNowActivity extends AppCompatActivity implements PromocodesFragment.promocodeInterface {
    Toolbar toolbar;
    String PROVIDER_UID, GENDER, UID, SELECTED_DATE, MOBILE_NUM, BOOKING_ID;
    int total_cost, SELECTED_SEAT_POSITION, SERVICE_CHARGE = 9;
    double total_time, SELECTED_TIME, PERCENTAGE_DEDUCTED = 10;
    HashMap<String, SalonServicesActivity.service> serviceHashMap = new HashMap<>();
    private NestedScrollView nestedScrollView;
    private ConstraintLayout loading_layout;
    TextView provider_name, provider_address, total_charge_textview, total_span_textview, start_time_textView, date_textView,
            promocode_textview, wallet_text, total_pay, wallet_pay, direct_pay, rate_cutter_info;
    LayoutInflater layoutInflater;
    CustomServiceAdapter adapter;
    RecyclerView recyclerView;
    ConstraintLayout promocodesLayout;
    String SELECTED_PROMOCODE = null;
    Button promocode_button, open_maps, book_now;
    CustomCheckBox wallet_checkbox;
    boolean is_wallet_inclded = false;
    private final int USE_WALLET_CASH = 1, GENERATE_TID = 2;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    LoadingDialogFragment loadingdialog = new LoadingDialogFragment();
    ProgressBar book_now_progressbar;
    int CUTT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_now);
        //uid
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //inflater
        layoutInflater = LayoutInflater.from(getApplicationContext());
        //getting intent data
        PROVIDER_UID = getIntent().getStringExtra("uid");
        GENDER = getIntent().getStringExtra("gender");
        total_cost = getIntent().getIntExtra("t_charge", 0) + SERVICE_CHARGE;
        total_time = getIntent().getDoubleExtra("t_time", 0);
        SELECTED_TIME = getIntent().getDoubleExtra("selected_time", 0);
        SELECTED_DATE = getIntent().getStringExtra("selected_date");
        SELECTED_SEAT_POSITION = getIntent().getIntExtra("selected_seat", 1);
        serviceHashMap = (HashMap<String, SalonServicesActivity.service>) getIntent().getSerializableExtra("services_list");
        //initiating ui
        book_now_progressbar = findViewById(R.id.progressBar10);
        total_pay = findViewById(R.id.total_pay);
        wallet_pay = findViewById(R.id.wallet_pay);
        direct_pay = findViewById(R.id.direct_pay);
        date_textView = findViewById(R.id.date);
        open_maps = findViewById(R.id.open_maps);
        rate_cutter_info = findViewById(R.id.rate_cutter_info);
        //progressDialog = new ProgressDialog(this);
        promocode_button = findViewById(R.id.button6);
        wallet_text = findViewById(R.id.include_wallet_cash);
        wallet_checkbox = findViewById(R.id.wallet_checkbox);
        book_now = findViewById(R.id.book_now);
        promocode_textview = findViewById(R.id.promocode);
        promocodesLayout = findViewById(R.id.promocodes_layout);
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

        //set promocodes
        promocode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment promocodesFragment = PromocodesFragment.newInstance();
                Bundle args = new Bundle();
                args.putString("gender", GENDER);
                args.putInt("t_charge", total_cost);
                args.putSerializable("services", serviceHashMap);
                promocodesFragment.setArguments(args);
                promocodesFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogFragmentTheme);
                promocodesFragment.show(ft, "dialog");
            }
        });

        //setting summary details
        total_charge_textview.setText("₹" + Integer.toString(total_cost));
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
        //initially set direct pay as total pay
        total_pay.setText("₹" + Integer.toString(total_cost));
        direct_pay.setText("₹" + Integer.toString(total_cost));
        wallet_pay.setText("₹" + Integer.toString(0));

        //wallet chekcbox
        wallet_checkbox.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                if (isChecked) {
                    onSelectedWalletOption();
                } else {
                    total_pay.setText("₹" + Integer.toString(total_cost));
                    direct_pay.setText("₹" + Integer.toString(total_cost));
                    wallet_pay.setText("₹" + Integer.toString(0));
                    is_wallet_inclded = false;
                    Log.d("datta_text", "false");
                }
            }
        });

        wallet_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wallet_checkbox.toggle();
            }
        });

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

    void onSelectedWalletOption(){
        //if user selected wallet option check if sufficient wallet cash is there or not
        //progress dialog start right after astarting this method
        showLoadingFragment();
        DatabaseReference wallet_ref = FirebaseDatabase.getInstance().getReference().child("user_wallet/" + UID + "/seeds");
        wallet_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    int seeds = Integer.parseInt(dataSnapshot.getValue().toString());
                    //int wallet_cash = (int)(seeds * SEED_RATE);
                    int wallet_cash = seeds;
                    //int deduct_cash = total_cost - total_cost % 3;
                    int deduct_cash;
                    if(wallet_cash >(int) (total_cost/(100 /PERCENTAGE_DEDUCTED))){
                        deduct_cash = (int) (total_cost / (100 /PERCENTAGE_DEDUCTED));
                        Log.d("datta_text", ">:"+Integer.toString((int)(total_cost/(100/PERCENTAGE_DEDUCTED))));
                    }else {
                        deduct_cash = wallet_cash;
                        Log.d("datta_text", Integer.toString(wallet_cash));}

                    direct_pay.setText("₹" + Integer.toString(total_cost - deduct_cash));
                    wallet_pay.setText("₹" + Integer.toString(deduct_cash));

                    //set wallet included flags
                    is_wallet_inclded = true;
                    Log.d("datta_text", "true");
                } else {
                    //if any error occur cancel the activity
                    finish();
                }
                //dismiss progress dialog
                dissmissLoadingFragment();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void bookNow(View v) {
        startButtonLoading();
        if (is_wallet_inclded) {
            checkDatabaseConnection(USE_WALLET_CASH);
        } else {
            checkDatabaseConnection(GENERATE_TID);
        }
    }

    private void checkDatabaseConnection(final int i) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    switch (i) {
                        case USE_WALLET_CASH:
                            useWalletCash();
                            break;
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

    //checking money in wallet
    //if money<needed money -> include money along with direct pay
    private void useWalletCash() {
            DatabaseReference wallet_ref = FirebaseDatabase.getInstance().getReference().child("user_wallet/" + UID + "/seeds");
            wallet_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        int deduct_cash = 0;
                        int seeds = Integer.parseInt(dataSnapshot.getValue().toString());
                        int wallet_cash = seeds;
                        if (wallet_cash > (int)(total_cost/(100/PERCENTAGE_DEDUCTED))) {
                            deduct_cash = (int)(total_cost/(100/PERCENTAGE_DEDUCTED));
                            generateTID(wallet_cash, deduct_cash);
                            Log.d("datta_text", "deduct cash is" + Integer.toString(deduct_cash));
                            Log.d("datta_text", "wallet cash is" + Double.toString(wallet_cash));
                        } else {
                            deduct_cash = wallet_cash;
                            generateTID(wallet_cash, deduct_cash);
                        }
                    } else {
                        generateTID(0, 0);
                        Log.d("datta_text", "deduct cash is" + Integer.toString(0));
                        Log.d("datta_text", "wallet cash is" + Integer.toString(0));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    stopButtonLoading();
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.makeText(getApplicationContext(), "Booking Failed", Toast.LENGTH_SHORT).show();
                    Log.d("datta_text", "error in book now actvty while getiing wallet details: " + databaseError.toString());
                    //wallet_cash.setText("opps!");
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
                BOOKING_ID = salt.toString();
                storeTransaction(BOOKING_ID, wallet_cash, deduct_cash, estimatedServerTimeMs);
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
        if (SELECTED_PROMOCODE != null) {
            transaction.put("promo_code", SELECTED_PROMOCODE);
        }
        transaction.put("t_span", total_time);
        transaction.put("t_charge", total_cost);
        transaction.put("status", getResources().getString(R.string.failed));
        transaction.put("gender", GENDER);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
        String date = sdf.format(serverTimeMs);
        //transaction.put("booked_on", date);
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
                                            if (SELECTED_PROMOCODE != null) {
                                                addCashback(tid, wallet_cash, deduct_cash, serverTimeMs);
                                            } else {
                                                if (deduct_cash > 0) {
                                                    debitUser(tid, wallet_cash, deduct_cash, serverTimeMs);
                                                } else {
                                                    DatabaseReference trans_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + UID +"/"+tid + "/status");
                                                    trans_ref.setValue(getResources().getString(R.string.successful)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            openResultActivity(tid);
                                                        }
                                                    });
                                                }
                                            }
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
        cashback.put("status", getResources().getString(R.string.pending));
        cashback.put("promocode", SELECTED_PROMOCODE);
        cashback_ref.setValue(cashback).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (deduct_cash > 0) {
                        debitUser(tid, wallet_cash, deduct_cash, serverTimeMs);
                    } else {
                        DatabaseReference trans_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + UID +"/"+tid + "/status");
                        trans_ref.setValue(getResources().getString(R.string.successful)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                openResultActivity(tid);
                            }
                        });
                    }
                } else {
                    openResultActivity(tid);
                    Log.d("datta_text", "problem to add cashback");
                }
            }
        });
    }

    private void debitUser(final String tid, final double wallet_cash, final int deduct_cash, final long serverTimeMs) {
        //deducting from user
        final DatabaseReference wallet_ref = FirebaseDatabase.getInstance().getReference().child("user_wallet/" + UID);
        //as seed rate by this time is 1.5 minimum seeds that user can use is 20
        // so i want to make wallet cash in 20 multiples
        wallet_ref.child("seeds").setValue((int)(wallet_cash  - deduct_cash)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    HashMap<String, Object> wallet_transaction = new HashMap<>();
                    wallet_transaction.put("seeds_before", wallet_cash);
                    wallet_transaction.put("deducted", deduct_cash);
                    wallet_transaction.put("type", getResources().getString(R.string.salon));
                    wallet_transaction.put("action", "Salon service Booking");
                    wallet_transaction.put("tid", tid);
                    wallet_transaction.put("time_stamp", ServerValue.TIMESTAMP);

                    wallet_ref.child("transactions/" + tid).setValue(wallet_transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                creditProvider(tid, deduct_cash, serverTimeMs);
                                //openResultActivity(tid);
                            } else {
                                openResultActivity(tid);
                            }
                        }
                    });
                } else {
                    openResultActivity(tid);
                    Log.d("datta_text", "error in deducting money from wallet");
                }
            }
        });
    }

    private void creditProvider(final String tid, final int deduct_cash, final long serverTimeMs) {
        final int[] wallet_seeds = {0};
        final DatabaseReference wallet_ref = FirebaseDatabase.getInstance().getReference().child("provider_wallet/" + PROVIDER_UID );
        wallet_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    wallet_seeds[0] = Integer.parseInt(dataSnapshot.child("seeds").getValue().toString());
                }
                wallet_ref.child("seeds").setValue(wallet_seeds[0] + deduct_cash).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, Object> wallet_transaction = new HashMap<>();
                            wallet_transaction.put("seeds_before", wallet_seeds[0]);
                            wallet_transaction.put("added", deduct_cash );
                            wallet_transaction.put("type", "Booking");
                            wallet_transaction.put("action", getResources().getString(R.string.salon));
                            wallet_transaction.put("tid", tid);
                            wallet_transaction.put("uid", UID);
                            wallet_transaction.put("time_stamp", ServerValue.TIMESTAMP);

                            wallet_ref.child("transactions/" + tid).setValue(wallet_transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        DatabaseReference trans_ref = FirebaseDatabase.getInstance().getReference().child("bookings/salon_spa/" + UID +"/"+tid + "/status");
                                        trans_ref.setValue(getResources().getString(R.string.successful)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    openResultActivity(tid);
                                                }
                                            }
                                        });
                                    } else {
                                        openResultActivity(tid);
                                    }

                                }
                            });
                        } else {
                            openResultActivity(tid);
                            Log.d("datta_text", "error in adding money");
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text", "error while getting provider wallet cash");
            }
        });
    }

    private void openResultActivity(String tid) {
        sendSMS();
      //  book_now.doneLoadingAnimation(Color.parseColor("#333639"), BitmapFactory.decodeResource(getResources(), R.drawable.tick));
        Intent booking_result_intent = new Intent(BookNowActivity.this, BookingResult.class);
        Pair[] pair1=new Pair[2];
        pair1[0]=new Pair(provider_name,"provider_name");
        pair1[1]=new Pair(provider_address,"address");
        ActivityOptions options1= ActivityOptions.makeSceneTransitionAnimation(BookNowActivity.this,pair1);
        booking_result_intent.putExtra("back_enabled", false);
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

    @Override
    public void onPromocodeSelect(String promocodeName, String sub_category, int cutt) {
        SELECTED_PROMOCODE = promocodeName;
        promocode_textview.setText(SELECTED_PROMOCODE);
        promocode_button.setText("Change");
        Log.d("datta_text", "selected promocode is " + SELECTED_PROMOCODE);

        //cutting price
        int total_sub_price=0;
        ArrayList<String> keys = new ArrayList<>(serviceHashMap.keySet());
        for(int i=0; i<keys.size(); i++){
            SalonServicesActivity.service  temp_service = serviceHashMap.get(keys.get(i));
            if(temp_service.getSubCategory().equals(sub_category)){
                total_sub_price = total_sub_price + temp_service.getCharge_in_num();
            }
        }
        if(total_sub_price>=cutt){
            total_cost = total_cost - cutt + CUTT;
            //
            CUTT = cutt;
            if (wallet_checkbox.isChecked()) {
                onSelectedWalletOption();
            } else {
                total_pay.setText("₹" + Integer.toString(total_cost));
                direct_pay.setText("₹" + Integer.toString(total_cost));
                wallet_pay.setText("₹" + Integer.toString(0));
                is_wallet_inclded = false;
                Log.d("datta_text", "false");
            }
        }
        //setting text to show reduced cost
        rate_cutter_info.setText("₹"+Integer.toString(cutt)+" was reduced from the total price");
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

    public void sendSMS(){
        DatabaseReference mobile_numref = FirebaseDatabase.getInstance().getReference().child("users/"+FirebaseAuth.getInstance().getUid()+"/phone_number");
        mobile_numref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MOBILE_NUM = dataSnapshot.getValue().toString();
                new sendSMS().execute();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class sendSMS extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... strings) {
            Log.d("datta_text", "went into send sms do in background");
            try {
                // Construct data
                String apiKey = "apikey=" + "BEEWJxdS9w8-zcLCp1lQCU6Ff980adLChVtGTnHYe5";
                String message = "&message=" + "Salon service was booked in "+provider_name.getText().toString()+" on "+date_textView.getText().toString()+" with"+BOOKING_ID;
                String sender = "&sender=" + "SLTSED";
                String numbers = "&numbers=" + "91" + MOBILE_NUM;

                // Send data
                HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
                String data = apiKey + numbers + message + sender;
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
                conn.getOutputStream().write(data.getBytes("UTF-8"));
                final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                final StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    stringBuffer.append(line);
                }
                rd.close();
                Log.d("datta_text",stringBuffer.toString());


            } catch (Exception e) {
                System.out.println("Error SMS "+e);

            }
            return null;
        }
    }

}
