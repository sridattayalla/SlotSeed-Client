package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
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

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sridatta.busyhunkproject.models.customDate;
import com.sridatta.busyhunkproject.models.time;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChooseSalonTimings extends AppCompatActivity{
    /*giving some value to selected_timetext that is not between 0 to 24*/
    String PROVIDER_UID,GENDER,SELECTED_TIME_TEXT="26",SELECTED_DATE = null, REFFERAL_REWARD_CATEGORY;
    int total_cost, seat_count=0, DAYS_TO_RESERVE=7;
    boolean today_availability_checked = false;
    double total_time,SELECTED_TIME;
   // HashMap<String,SalonServicesActivity.Offer> offerHashMap=new HashMap();
    HashMap<String,SalonServicesActivity.service> serviceHashMap=new HashMap<>();
    LayoutInflater layoutInflater;
    RecyclerView seats_recycler_view,times_recycler_view;
    List<customDate> dateList=new ArrayList<>();
    List<Integer> seatsList=new ArrayList<>();
    int SELECTED_DATE_POSITION=0,SELECTED_SEAT_POSITION=1;
    private ConstraintLayout date_layout;
    private TextView date_textView, day_textView, month_textView;
    private ImageView forward,backward;
    ConstraintLayout loading_layout,internet_layout,error_layout,time_choose_layout;
    final String TIME="TIME",INTERNET="INTERNET",ERROR="ERROR",LOADING="LOADING",MORNING="MORNING",AFTERNOON="AFTERNOON",EVENING="EVENING";
    long DAY_IN_MILLIS=86400000;
    List<Double> booked_slots =new ArrayList<>();
    Map<Double, time> available_slots=new TreeMap<>();
    SeatsAdapter seatsAdapter=new SeatsAdapter();
    timeAdapter adapter=new timeAdapter();
    Toolbar toolbar;
    LottieAnimationView animationView;
    Button morning_button,afternoon_button,evening_button,floatingActionButton;
    double interval_start_time=6,interval_end_time=12,OPEN_TIME,CLOSE_TIME;
    final double minimum_time_before=3, exception_extended_time=0, time_after_bookings_start = 0.25;
    private final int SET_SEATS = 1, SET_TIMES = 2, SET_TRUE_TIME = 3;
    Button go_back, retry;
    TextView no_slots;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_salon_timings);
        //layout inflater
        layoutInflater= LayoutInflater.from(getApplicationContext());
        //getting intent
        PROVIDER_UID=getIntent().getStringExtra("uid");
        GENDER=getIntent().getStringExtra("gender");
        REFFERAL_REWARD_CATEGORY = getIntent().getStringExtra("referral_reward_category");
     //   offerHashMap= (HashMap<String, SalonServicesActivity.Offer>) getIntent().getSerializableExtra("offers_list");
        serviceHashMap= (HashMap<String, SalonServicesActivity.service>) getIntent().getSerializableExtra("services_list");
        total_cost=getIntent().getIntExtra("t_charge",0);
        total_time=getIntent().getDoubleExtra("t_time",0);
        /*verifing if time or cost is zero if they are zero exit from current activity*/
        if(total_time==0||total_cost==0){
            finish();
        }
        Log.d("datta_text",PROVIDER_UID);
        Log.d("datta_text",GENDER);
        Log.d("datta_text",Integer.toString(serviceHashMap.size()));
        Log.d("datta_text",Integer.toString(total_cost));
        Log.d("datta_text",Double.toString(total_time));

        //initiating ui
        no_slots = findViewById(R.id.no_slots);
        go_back = findViewById(R.id.go_back);
        retry = findViewById(R.id.retry);
        animationView = findViewById(R.id.loading_anim);animationView.playAnimation();
        date_layout=findViewById(R.id.date_layout);
        date_textView=findViewById(R.id.date);
        day_textView=findViewById(R.id.day);
        month_textView=findViewById(R.id.month);
        morning_button=findViewById(R.id.morning);
        afternoon_button=findViewById(R.id.afternoon);
        evening_button=findViewById(R.id.evening);
        forward=findViewById(R.id.forward);
        backward=findViewById(R.id.backward);
        error_layout=findViewById(R.id.error_layout);
        loading_layout=findViewById(R.id.loading_layout);
        internet_layout=findViewById(R.id.internet_layout);
        floatingActionButton=findViewById(R.id.floatingActionButton);
        time_choose_layout=findViewById(R.id.time_choose_layout);
        seats_recycler_view=findViewById(R.id.seats_recycler_view);
        seats_recycler_view.setHasFixedSize(true);
        seats_recycler_view.setLayoutManager(new GridLayoutManager(this,2));
        seats_recycler_view.addItemDecoration(new SpacesItemDecorator(1));
        toolbar=findViewById(R.id.toolbar6);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        times_recycler_view=findViewById(R.id.times);
        times_recycler_view.setHasFixedSize(true);
        times_recycler_view.setLayoutManager(new GridLayoutManager(this,4));
        times_recycler_view.addItemDecoration(new SpacesItemDecorator(1));
        times_recycler_view.setAdapter(adapter);

        //setting layout visibility
        setLayout(LOADING);

        //goback onclick
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //retry onClick
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent select_time_intent=new Intent(ChooseSalonTimings.this,ChooseSalonTimings.class);
                select_time_intent.putExtra("uid",PROVIDER_UID);
                select_time_intent.putExtra("gender",GENDER);
                // select_time_intent.putExtra("offers_list", (Serializable) SelectedofferList);
                select_time_intent.putExtra("services_list", (Serializable) serviceHashMap);
                select_time_intent.putExtra("t_time",total_time);
                select_time_intent.putExtra("t_charge",total_cost);
                startActivity(select_time_intent);
                finish();
            }
        });

        //forward onClick
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SELECTED_DATE_POSITION!=dateList.size()-1){
                    setDate(SELECTED_DATE_POSITION+1);
                   // setTimes();
                }
            }
        });

        //backward onClick
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SELECTED_DATE_POSITION!=0){
                    setDate(SELECTED_DATE_POSITION-1);
                    //setTimes();
                }
            }
        });

        //morning onClick
        morning_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionButton.setVisibility(View.GONE);
                /*giving some random text to selected time text so that it should not in between 0 to 24
                * reason to giving text instead of null is it will convert to double in some usage case at that time
                * it will throw error*/
                SELECTED_TIME_TEXT="25";
                setIntervalOfDay(MORNING);
                //setting times
                checkDatabaseConnection(SET_TIMES);
            }
        });
        //afternoon onClick
        afternoon_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionButton.setVisibility(View.GONE);
                /*giving some random text to selected time text so that it should not in between 0 to 24
                 * reason to giving text instead of null is it will convert to double in some usage case at that time
                 * it will throw error*/
                SELECTED_TIME_TEXT="25";
                setIntervalOfDay(AFTERNOON);
                //setting times
                checkDatabaseConnection(SET_TIMES);
            }
        });
        //evening onClick
        evening_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionButton.setVisibility(View.GONE);
                /*giving some random text to selected time text so that it should not in between 0 to 24
                 * reason to giving text instead of null is it will convert to double in some usage case at that time
                 * it will throw error*/
                SELECTED_TIME_TEXT="25";
                setIntervalOfDay(EVENING);
                //setting times
                checkDatabaseConnection(SET_TIMES);
            }
        });

        //floating actionbuttonOnclick
        //button to proceed
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(total_time>0){
                    if(REFFERAL_REWARD_CATEGORY != null && REFFERAL_REWARD_CATEGORY.length() > 0){
                        if(hasCategory(REFFERAL_REWARD_CATEGORY)) {
                            open_rewardBookNow();
                        }
                        else{
                            open_bookNow();
                        }
                    }
                    else {
                       open_bookNow();
                    }
                }
            }
        });


        //getting seat list
        checkDatabaseConnection(SET_TRUE_TIME);
        checkDatabaseConnection(SET_SEATS);
    }

    void showLayout(String str){
        switch (str){
            case "anim": animationView.setVisibility(View.VISIBLE);
                times_recycler_view.setVisibility(View.GONE);
                no_slots.setVisibility(View.GONE);
                break;
            case "times": animationView.setVisibility(View.GONE);
                times_recycler_view.setVisibility(View.VISIBLE);
                no_slots.setVisibility(View.GONE);
                break;
            case "no_slots": no_slots.setVisibility(View.VISIBLE);
                animationView.setVisibility(View.GONE);
                times_recycler_view.setVisibility(View.GONE);
                break;
        }
    }

    private void checkDatabaseConnection(final int i) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child(".info/connected");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected=dataSnapshot.getValue(Boolean.class);
                if(connected){
                    switch (i){
                        case SET_SEATS : getSeatList(); break;
                        case SET_TIMES : setTimes(); break;
                        case SET_TRUE_TIME: getTrueTime(); break;
                    }
                }
                else {
                    setLayout(INTERNET);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setLayout(INTERNET);
                Log.d("datta_text","onCancelled at connection reference "+databaseError.toString());
            }
        });
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

    void setIntervalOfDay(String s){
        removeAllIntervalOfDay();
        switch (s){
            case MORNING:morning_button.setBackground(getResources().getDrawable(R.drawable.day_background));morning_button.setTextSize(14);
                interval_start_time=6;
                interval_end_time=12;
                break;
            case AFTERNOON:afternoon_button.setBackground(getResources().getDrawable(R.drawable.day_background));afternoon_button.setTextSize(14);
                interval_start_time=12;
                interval_end_time=17;
                break;
            case EVENING:evening_button.setBackground(getResources().getDrawable(R.drawable.day_background));evening_button.setTextSize(14);
                interval_start_time=17;
                interval_end_time=21;
                break;
        }
    }

    void removeAllIntervalOfDay(){
        morning_button.setBackgroundColor(getResources().getColor(R.color.transprent));
        morning_button.setTextSize(12);
        afternoon_button.setBackgroundColor(getResources().getColor(R.color.transprent));
        afternoon_button.setTextSize(12);
        evening_button.setBackgroundColor(getResources().getColor(R.color.transprent));
        evening_button.setTextSize(12);
    }

    void setLayout(String s){
        allGone();
        switch (s){
            case INTERNET:internet_layout.setVisibility(View.VISIBLE);break;
            case LOADING:loading_layout.setVisibility(View.VISIBLE);break;
            case ERROR:error_layout.setVisibility(View.VISIBLE);break;
            case TIME:time_choose_layout.setVisibility(View.VISIBLE);break;
        }
    }

    void allGone(){
        time_choose_layout.setVisibility(View.GONE);
        error_layout.setVisibility(View.GONE);
        loading_layout.setVisibility(View.GONE);
        internet_layout.setVisibility(View.GONE);
    }

    void getTrueTime(){
        //checking present date and time using firebase
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                //load dates
                SimpleDateFormat dayformat=new SimpleDateFormat("EEEE");
                SimpleDateFormat monthformat=new SimpleDateFormat("MMM");
                SimpleDateFormat day_int_format=new SimpleDateFormat("dd");
                SimpleDateFormat month_int_format=new SimpleDateFormat("MM");
                SimpleDateFormat year_int_format=new SimpleDateFormat("yyyy");
                for(int i=0;i<7;i++){
                    long estimatedServerTimeMs = System.currentTimeMillis() + offset + i*DAY_IN_MILLIS;
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                    Date resultdate = new Date(estimatedServerTimeMs);
                    Log.d("datta_text","date from firebase is "+sdf.format(resultdate).toString());
                    String day=dayformat.format(resultdate);
                    String month=monthformat.format(resultdate);
                    String day_int=day_int_format.format(resultdate);
                    String month_int=month_int_format.format(resultdate);
                    String year_int=year_int_format.format(resultdate);
                    //inserting to date arraylist
                    dateList.add(new customDate(Integer.parseInt(day_int),Integer.parseInt(month_int),Integer.parseInt(year_int),day,month));
                    Log.d("datta_text", day+" "+month+" "+day_int+" "+month_int+" "+year_int);
                }
                //initially set date position to 0
                setDate(0);
                //setting layout visibility
                setLayout(TIME);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
                Log.d("datta_text","error is "+error.toString());
                //setting layout visibility
                setLayout(INTERNET);
            }
        });

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

    void getSeatList(){
        //no need of loading here setLayout(LOADING);
        DatabaseReference seats_ref= FirebaseDatabase.getInstance().getReference().child("providers").child("salons").child(PROVIDER_UID).child("seats_count");
        seats_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    int seats = Integer.parseInt(dataSnapshot.getValue().toString());
                    if(seats>0){
                        //clearing seat list
                        seat_count=0;
                        for(int i=1;i<seats+1;i++){
                            seat_count++;
                        }
                        Log.d("datta_text","seat count is "+Integer.toString(seats));
                        setLayout(TIME);
                        //setting seat list
                        /*if seats>1 then seat selection recycler view will show up*/
                        if(seat_count>1){
                            seatsList.clear();
                            //write here to display seats
                            for(int i=1;i<=seat_count;i++){
                                seatsList.add(i);
                            }
                            setSeats();
                        }
                    }
                }
                catch (Exception e){
                    Log.d("datta_text",e.toString());
                    setLayout(INTERNET);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setLayout(INTERNET);
                Log.d("datta_text", "error in getting seats list: "+databaseError.toString());
            }
        });
    }

    void setTimes(){
        //visibility
        showLayout("anim");
        booked_slots.clear();
        available_slots.clear();
        DatabaseReference start_end_time_ref=FirebaseDatabase.getInstance().getReference().child("providers").child("salons").child(PROVIDER_UID);
        start_end_time_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Double open_hour = Double.parseDouble(dataSnapshot.child("time/open_hour").getValue().toString());
                    Double open_min = Double.parseDouble(dataSnapshot.child("time/open_min").getValue().toString());
                    Double close_hour = Double.parseDouble(dataSnapshot.child("time/close_hour").getValue().toString());
                    Double close_min = Double.parseDouble(dataSnapshot.child("time/close_min").getValue().toString());

                    //setting open and close time
                    OPEN_TIME = open_hour + open_min / 60 + time_after_bookings_start;
                    CLOSE_TIME = close_hour + close_min / 60;
                    Log.d("datta_text","open time is "+Double.toString(OPEN_TIME) + "close time is " + Double.toString(CLOSE_TIME));

                    //getBookedSlots
                    customDate curr_custom_date = dateList.get(SELECTED_DATE_POSITION);
                    String curr_month = Integer.toString(curr_custom_date.getMonth_int());
                    if(curr_month.length()==1){
                        curr_month = "0"+curr_month;
                    }
                    String curr_date = Integer.toString(curr_custom_date.getDate_int());
                    if(curr_date.length()==1){
                        curr_date = "0" + curr_date;
                    }
                    SELECTED_DATE = Integer.toString(curr_custom_date.getYear_int()) + curr_month + curr_date;
                    DatabaseReference booked_slots_ref = FirebaseDatabase.getInstance().getReference().child("availability/salons/" + SELECTED_DATE + "/" + PROVIDER_UID + "/s" + Integer.toString(SELECTED_SEAT_POSITION));
                    booked_slots_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot timing_snapshot : dataSnapshot.getChildren()) {
                                double session_start_time = Double.parseDouble(timing_snapshot.child("start_time").getValue().toString());
                                double session_end_time = Double.parseDouble(timing_snapshot.child("end_time").getValue().toString());
                                for (double i = session_start_time; i < session_end_time; i = i + 0.25) {
                                    booked_slots.add(i);
                                    Log.d("datta_text","booked slots are: "+Double.toString(i));
                                }
                            }
                            //close time is adding to booked slot
//                            booked_slots.add(CLOSE_TIME+exception_extended_time);

                            //setting available slots
                            available_slots.clear();
                            if(SELECTED_DATE_POSITION==0){
                                final SimpleDateFormat sdf_min=new SimpleDateFormat("m");
                                final SimpleDateFormat sdf_hrs=new SimpleDateFormat("H");
                                DatabaseReference time_ref=FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                                time_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot timedataSnapshot) {
                                        try{
                                        long offset = timedataSnapshot.getValue(Long.class);
                                        long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                                        Date resultdate = new Date(estimatedServerTimeMs);

                                        int today_correct_hours=Integer.parseInt(sdf_hrs.format(resultdate));
                                        int today_correct_mins =Integer.parseInt(sdf_min.format(resultdate));
                                        /*set interval of the day with reference to today_corrent_hours*/
                                        if(!today_availability_checked){
                                            today_availability_checked = true;
                                            if(today_correct_hours < 12 - minimum_time_before){
                                                setIntervalOfDay(MORNING);
                                            }else if(today_correct_hours < 17 - minimum_time_before){
                                                setIntervalOfDay(AFTERNOON);
                                            }else if(today_correct_hours < 21 - minimum_time_before){
                                                setIntervalOfDay(EVENING);
                                            }else {
                                                /*going to next day if current time exceeds booking hours*/
                                                setDate(1);
                                            }
                                        }
                                        Log.d("datta_text","hours are "+ sdf_hrs.format(resultdate));
                                        Log.d("datta_text","mins are "+ sdf_min.format(resultdate));
                                        //setting times
                                            double num_min=0;
                                            if(today_correct_mins%15==0){
                                                int temp_min=today_correct_mins/15;
                                                num_min=temp_min/4.0;
                                            }
                                            else {
                                                int temp_min=today_correct_mins/15;
                                                num_min=temp_min/4.0+0.25;
                                            }
                                            Double time=today_correct_hours+ num_min;
                                            Log.d("datta_text","current time is "+Double.toString(time));
                                            double time_to_start;
                                            if(OPEN_TIME>time+minimum_time_before){
                                                time_to_start=OPEN_TIME;
                                            }
                                            else{
                                                time_to_start=time+minimum_time_before;
                                            }
                                            for (double i = time_to_start; i <= CLOSE_TIME-total_time+exception_extended_time; i = i + 0.25) {
                                                boolean is_available = true;
                                                for (double j : booked_slots) {
                                                    Log.d("datta_text", Double.toString(j));
                                                    if (i <= j && i + total_time > j) {
                                                        is_available = false;
                                                        break;
                                                    }
                                                }
                                                if (is_available) {
                                                    /*check if available time is between interval or not*/
                                                    if(i>=interval_start_time&&i<=interval_end_time){
                                                        available_slots.put(i, new time(i, false));
                                                        Log.d("datta_text","available time is "+ Double.toString(i));
                                                    }
                                                }else{
                                                    /*check if available time is between interval or not*/
                                                    if(i>=interval_start_time&&i<=interval_end_time){
                                                        available_slots.put(i, new time(i, true));
                                                        Log.d("datta_text","available time is "+ Double.toString(i));
                                                    }
                                                }
                                            }
                                            adapter=new timeAdapter();
                                            times_recycler_view.setAdapter(adapter);
                                            //visibility
                                            showLayout("times");
                                            //checking if there are any available slots
                                            if(available_slots.size()<=0){
                                                //visibility
                                                showLayout("no_slots");
                                            }
                                        }
                                        catch (Exception e){
                                            setLayout(ERROR);
                                            Log.d("datta_text","catch while getting adding avilable slots "+e.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        setLayout(INTERNET);
                                        Log.d("datta_text","erro while getting adding avilable slots "+databaseError.toString());
                                    }
                                });

                            }
                            else {
                                for (double i = OPEN_TIME; i <=CLOSE_TIME-total_time+exception_extended_time; i = i + 0.25) {
                                    boolean is_available = true;
                                    for (double j : booked_slots) {
                                        Log.d("datta_text", Double.toString(j));
                                        if (i <= j && i + total_time > j) {
                                            is_available = false;
                                            break;
                                        }
                                    }
                                    if (is_available) {
                                        //check whether time is in interval or not
                                        if(i>=interval_start_time && i<=interval_end_time){
                                            available_slots.put(i, new time(i, false));
                                            Log.d("datta_text","available time is "+ Double.toString(i));
                                        }
                                    }else {
                                        //check whether time is in interval or not
                                        if(i>=interval_start_time&&i<=interval_end_time){
                                            available_slots.put(i, new time(i, true));
                                            Log.d("datta_text","available time is "+ Double.toString(i));
                                        }
                                    }
                                }
                            }
                            adapter=new timeAdapter();
                            times_recycler_view.setAdapter(adapter);
                            //visibility
                            showLayout("times");
                            //checking if there are any available slots
                            if(available_slots.size()<=0){
                                //visibility
                                showLayout("no_slots");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            setLayout(INTERNET);
                            Log.d("datta_text","erro while getting adding avilable slots "+databaseError.toString());
                        }
                    });

                }
                catch (Exception e){
                    Log.d("datta_text","error occured getting seats");
                    //setting available slots
                    for (double i = OPEN_TIME+minimum_time_before; i < CLOSE_TIME+exception_extended_time; i = i + 0.25) {
                        boolean is_available = true;
                        for (double j : booked_slots) {
                            if (i >= j && i + total_time <= j) {
                                is_available = false;
                            }
                        }
                        if (is_available) {
                            //available_slots.put(i, i);
                            Log.d("datta_text",Double.toString(i));
                        }
                    }
                    adapter=new timeAdapter();
                    times_recycler_view.setAdapter(adapter);
                    //visibility
                    showLayout("times");
                    //checking if there are any available slots
                    if(available_slots.size()<=0){
                        //visibility
                        showLayout("no_slots");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setLayout(INTERNET);
                Log.d("datta_text","erro while getting setting times "+databaseError.toString());
            }
        });

    }

    private void setSeats() {
        seats_recycler_view.setAdapter(seatsAdapter);
    }

    class timeAdapter extends RecyclerView.Adapter<timeViewHolder>{
        View v;
        @Override
        public timeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v=layoutInflater.inflate(R.layout.single_time_button,parent,false);
            return new timeViewHolder(v);
        }

        @Override
        public void onBindViewHolder(timeViewHolder holder, final int position) {
            ArrayList<Double> keys = new ArrayList<>(available_slots.keySet());
            holder.setAvailableTimes(available_slots.get(keys.get(position)));

        }

        @Override
        public int getItemCount() {
            return available_slots.size();
        }
    }

    class timeViewHolder extends RecyclerView.ViewHolder{
        View v;
        public timeViewHolder(View itemView) {
            super(itemView);
            v=itemView;

            final Button time_button=v.findViewById(R.id.time_button);
            time_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    floatingActionButton.setVisibility(View.VISIBLE);
                    SELECTED_TIME_TEXT=time_button.getTag().toString();
                    SELECTED_TIME=Double.parseDouble(time_button.getTag().toString());
                    Log.d("datta_text","choosed time: "+SELECTED_TIME_TEXT);
                    adapter.notifyDataSetChanged();
                }
            });

        }

        void setAvailableTimes(time t){
            double d = t.getTime().doubleValue();
            String hour=Integer.toString((int)d);
            String min=Integer.toString((int) ((d-(int)d)*60));
            /*if min is one lettered word like 0 it will not give a decent look so add another zero after it*/
            if(min.equals("0")){
                min = "00";
            }
            Button time_button=v.findViewById(R.id.time_button);

            String noon="AM";
            if(Integer.parseInt(hour)>=12){noon="PM";}
            if(Integer.parseInt(hour)-12>0){hour=Integer.toString(Integer.parseInt(hour)-12);}
            time_button.setText(hour+":"+min+" "+noon);
            time_button.setTag(Double.toString(d));

            /*setting button state indication ex: if selected: set selected background, if not then it may not be
            * selected so set already booked background, or if available to book set that background*/
            try {
                if (SELECTED_TIME_TEXT.equals(time_button.getTag().toString())) {
                    time_button.setBackground(getResources().getDrawable(R.drawable.time_choosen_background));
                    time_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick,0,0,0);
                    //proceed.setVisibility(View.VISIBLE);
                } else {
                    if(t.getBooked()){
                        /*disable button*/
                        time_button.setEnabled(false);
                        time_button.setBackground(getResources().getDrawable(R.drawable.time_booked_background));
                        time_button.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    }else {
                        time_button.setEnabled(true);
                        time_button.setBackground(getResources().getDrawable(R.drawable.time_not_booked_background));
                        time_button.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    }
                   /* time_button.setBackgroundColor(getResources().getColor(R.color.transprent));
                    time_button.setTextColor(getResources().getColor(R.color.black));
                    time_button.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);*/
                }
            }
            catch (NullPointerException e){
                Log.d("datta_text", e.toString());
            }
        }
    }

    class SeatsAdapter extends RecyclerView.Adapter<SeatsViewHolder>{
        View v;
        @Override
        public SeatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v=layoutInflater.inflate(R.layout.single_seat,parent,false);
            SeatsViewHolder seatsViewHolder=new SeatsViewHolder(v);
            return seatsViewHolder;
        }

        @Override
        public void onBindViewHolder(SeatsViewHolder holder, int position) {
            holder.setSeat(position);
        }

        @Override
        public int getItemCount() {
            return seatsList.size();
        }
    }

    class SeatsViewHolder extends RecyclerView.ViewHolder{
        View v;
        Button seat;
        public SeatsViewHolder(View itemView) {
            super(itemView);
            v=itemView;
            seat=v.findViewById(R.id.seat);
            seat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(SELECTED_SEAT_POSITION!=Integer.parseInt(seat.getTag().toString())){
                        //visibility
                        showLayout("anim");
                        floatingActionButton.setVisibility(View.GONE);
                        /*giving some random text to selected time text so that it should not in between 0 to 24
                         * reason to giving text instead of null is it will convert to double in some usage case at that time
                         * it will throw error*/
                        SELECTED_TIME_TEXT="25";
                        SELECTED_SEAT_POSITION=Integer.parseInt(seat.getTag().toString());
                        seatsAdapter.notifyDataSetChanged();
                        //setting times after changing seat position
                        checkDatabaseConnection(SET_TIMES);
                    }
                }
            });
        }

        public void setSeat(int position) {
            seat=v.findViewById(R.id.seat);
            seat.setTag(position+1);
            seat.setText("Seat "+Integer.toString(position+1));
            if(SELECTED_SEAT_POSITION==position+1){
                seat.setBackground(getResources().getDrawable(R.drawable.seat_choosen_background));
                seat.setTextColor(getResources().getColor(R.color.white));
            }
            else{
                seat.setBackgroundColor(getResources().getColor(R.color.transprent));
                seat.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        }

    }

    void setDate(final int i){
        floatingActionButton.setVisibility(View.GONE);
        /*giving some random text to selected time text so that it should not in between 0 to 24
         * reason to giving text instead of null is it will convert to double in some usage case at that time
         * it will throw error*/
        SELECTED_TIME_TEXT="25";
        SELECTED_DATE_POSITION=i;
        date_textView.setText(Integer.toString(dateList.get(i).getDate_int()));
        day_textView.setText(dateList.get(i).getDay());
        month_textView.setText(dateList.get(i).getMonth());
        //setting times afterchoosing date
        checkDatabaseConnection(SET_TIMES);

        //setting active and inactive colors according to selected date
        if(SELECTED_DATE_POSITION==DAYS_TO_RESERVE-1){
            forward.setImageDrawable(getResources().getDrawable(R.drawable.forward_inactive));
            forward.setEnabled(false);
        }
        else{
            forward.setImageDrawable(getResources().getDrawable(R.drawable.forward_active));
            forward.setEnabled(true);
        }

        if(SELECTED_DATE_POSITION==0){
            today_availability_checked = false;
            backward.setImageDrawable(getResources().getDrawable(R.drawable.backward_inactive));
            backward.setEnabled(false);
        }
        else {
            backward.setImageDrawable(getResources().getDrawable(R.drawable.backward_active));
            backward.setEnabled(true);
        }
    }

    class SpacesItemDecorator extends RecyclerView.ItemDecoration{
        private int space;

        public SpacesItemDecorator(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left=space;
            outRect.right=space;
            outRect.bottom=1;
            outRect.top=1;

        }
    }

    void open_rewardBookNow(){
        Intent select_time_intent = new Intent(ChooseSalonTimings.this, RewardBookNow.class);
        select_time_intent.putExtra("uid", PROVIDER_UID);
        select_time_intent.putExtra("gender", GENDER);
        //select_time_intent.putExtra("offers_list", (Serializable) offerHashMap);
        select_time_intent.putExtra("services_list", (Serializable) serviceHashMap);
        select_time_intent.putExtra("t_time", total_time);
        select_time_intent.putExtra("t_charge", total_cost);
        select_time_intent.putExtra("selected_time", SELECTED_TIME);
        select_time_intent.putExtra("selected_date", SELECTED_DATE);
        select_time_intent.putExtra("selected_seat", SELECTED_SEAT_POSITION);
        select_time_intent.putExtra("referral_reward_category", REFFERAL_REWARD_CATEGORY);
        startActivity(select_time_intent);
    }

    void open_bookNow(){
        Intent select_time_intent = new Intent(ChooseSalonTimings.this, BookNowActivity.class);
        select_time_intent.putExtra("uid", PROVIDER_UID);
        select_time_intent.putExtra("gender", GENDER);
        //select_time_intent.putExtra("offers_list", (Serializable) offerHashMap);
        select_time_intent.putExtra("services_list", (Serializable) serviceHashMap);
        select_time_intent.putExtra("t_time", total_time);
        select_time_intent.putExtra("t_charge", total_cost);
        select_time_intent.putExtra("selected_time", SELECTED_TIME);
        select_time_intent.putExtra("selected_date", SELECTED_DATE);
        select_time_intent.putExtra("selected_seat", SELECTED_SEAT_POSITION);
        startActivity(select_time_intent);
    }

}
