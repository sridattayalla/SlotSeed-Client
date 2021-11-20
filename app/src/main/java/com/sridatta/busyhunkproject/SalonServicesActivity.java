package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.igenius.customcheckbox.CustomCheckBox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

public class SalonServicesActivity extends AppCompatActivity implements Serializable, SubcategoryFragment.OnFragmentInteractionListener{
    LayoutInflater layoutInflater;
    LinkedHashMap<String, String> title_hashMap=new LinkedHashMap<>();
    RecyclerView recyclerView;
    String PROVIDER_UID, GENDER, NAME, REFFERAL_REWARD_CATEGORY;
    LinkedHashMap<String, Object> serviceList=new LinkedHashMap<>();
    LinkedHashMap<String, category> categoryList=new LinkedHashMap<>();
    public  HashMap<String,service> SelectedserviceList=new HashMap<>();
    int totalcharge;
    double total_time;
    TextView span, charge;
    Toolbar toolbar;
    Button proceed_button;
    LottieAnimationView loading_anim;
    ViewPager subcat_viewpager;
    TabLayout category_tablayout;
    boolean isToastShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_services);
        //intent
        PROVIDER_UID=getIntent().getStringExtra("uid");
        Log.d("datta_text",PROVIDER_UID);
        GENDER=getIntent().getStringExtra("gender");
        NAME=getIntent().getStringExtra("name");
        REFFERAL_REWARD_CATEGORY = getIntent().getStringExtra("referral_reward_category");
        //testing refferal reward category with toast
        //Toast.makeText(getApplicationContext(), REFFERAL_REWARD_CATEGORY, Toast.LENGTH_SHORT).show();
        //layout inflater
        layoutInflater= LayoutInflater.from(getApplicationContext());
        //initiating ui
        category_tablayout = findViewById(R.id.category_tab);
        subcat_viewpager = findViewById(R.id.subcategory_viewpager);
        loading_anim = findViewById(R.id.progressBar2); loading_anim.playAnimation();
        span=findViewById(R.id.span);
        charge=findViewById(R.id.booked_on);
        proceed_button=findViewById(R.id.proceed);
        toolbar=findViewById(R.id.toolbar5);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(NAME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        recyclerView=findViewById(R.id.container);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        //setting adapter and tablayout for Categories
        subcat_viewpager.setAdapter(new CategoryAdapter(getSupportFragmentManager()));
        category_tablayout.setupWithViewPager(subcat_viewpager);

        //setting adapter
//        parent_adapter=new parentAdapter();
//        recyclerView.setAdapter(parent_adapter);

        //
        //checkDatabseConnection();

        //proceed onClick
        proceed_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(total_time>0){
                    Intent select_time_intent=new Intent(SalonServicesActivity.this,ChooseSalonTimings.class);
                    select_time_intent.putExtra("uid",PROVIDER_UID);
                    select_time_intent.putExtra("gender",GENDER);
                   // select_time_intent.putExtra("offers_list", (Serializable) SelectedofferList);
                    select_time_intent.putExtra("services_list", (Serializable) SelectedserviceList);
                    select_time_intent.putExtra("t_time",total_time);
                    select_time_intent.putExtra("t_charge",totalcharge);
                    select_time_intent.putExtra("referral_reward_category", REFFERAL_REWARD_CATEGORY);
                    startActivity(select_time_intent);
                }
                else {
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.makeText(getApplicationContext(), "Please select a service", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //initially set hair
    }

    @Override
    public void addService(service curr_service) {
        //creating key to place service in selected services list
        String key = curr_service.getService_name();

        total_time=total_time+curr_service.getSpan_in_mins();
        totalcharge=totalcharge+curr_service.getCharge_in_num();

        //time to show
        String hours=Integer.toString((int) total_time);
        Log.d("datta_text", Double.toString(total_time));
        String mins=Integer.toString((int) ((total_time-(int)total_time)*60));

        /*cheking if hours is 0 or not and setting accordingly*/
        if(hours.equals("0")){
            String time_to_show=mins+" mins";
            //updating ui
            span.setText(time_to_show);
        }
        else{
            String time_to_show=hours+" hrs "+mins+" mins";
            //updating ui
            span.setText(time_to_show);
        }
        //setting charge
        charge.setText("₹"+Integer.toString(totalcharge));
        //adding to selected list
        SelectedserviceList.put(key,curr_service);
        //notify
        //no need to notify layout, as layout is updating based on its value(is seleceted or not)
    }

    @Override
    public void removeService(service curr_service) {
        //creating key to place service in selected services list
        String key = curr_service.getService_name();
        //checking if it is not selected prviously or not
        if(SelectedserviceList.containsKey(key)) {
            total_time = total_time - curr_service.getSpan_in_mins();
            totalcharge = totalcharge - curr_service.getCharge_in_num();

            //time to show
            String hours = Integer.toString((int) total_time);
            String mins = Integer.toString((int) ((total_time - (int) total_time) * 60));

            /*cheking if hours is 0 or not and setting accordingly*/
            if (hours.equals("0")) {
                String time_to_show = mins + " mins";
                //updating ui
                span.setText(time_to_show);
            } else {
                String time_to_show = hours + " hrs " + mins + " mins";
                //updating ui
                span.setText(time_to_show);
            }

            //setting charge
            charge.setText("₹" + Integer.toString(totalcharge));

            //removing from selected list
            SelectedserviceList.remove(key);
            //notify

        }
    }

    /*view pager adapter to for categories*/
    class CategoryAdapter extends FragmentPagerAdapter {
        String[] titles={getResources().getString(R.string.hair), getResources().getString(R.string.skin_care), getResources().getString(R.string.manicure), getResources().getString(R.string.pedicure), getResources().getString(R.string.spa)};
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
        public CategoryAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:return new SubcategoryFragment(getResources().getString(R.string.hair), PROVIDER_UID, GENDER);
                case 1:return new SubcategoryFragment(getResources().getString(R.string.skin_care), PROVIDER_UID, GENDER);
                case 2:return new SubcategoryFragment(getResources().getString(R.string.manicure), PROVIDER_UID, GENDER);
                case 3:return new SubcategoryFragment(getResources().getString(R.string.pedicure), PROVIDER_UID, GENDER);
                case 4: return new SubcategoryFragment(getResources().getString(R.string.spa), PROVIDER_UID, GENDER);
                default:return new SubcategoryFragment(getResources().getString(R.string.hair), PROVIDER_UID, GENDER);
            }

        }

        @Override
        public int getCount() {
            return 5;
        }
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

    static class service implements Serializable{
        private String service_name,act_charge,curr_charge,span;
        private int time_hours,charge_in_num;
        private double time_mins,span_in_mins;
        private boolean isSelected;
        private String category, subCategory;


        public service(String category, String Subcategory, String service_name, String act_charge, String curr_charge, String span, int time_hours, int charge_in_num, double time_mins, double span_in_mins, boolean isSelected) {
            this.category=category;
            this.service_name = service_name;
            this.act_charge = act_charge;
            this.curr_charge = curr_charge;
            this.span = span;
            this.time_hours = time_hours;
            this.charge_in_num = charge_in_num;
            this.time_mins = time_mins;
            this.span_in_mins = span_in_mins;
            this.isSelected=isSelected;
            this.subCategory= Subcategory;
        }

        public String getSubCategory() {
            return subCategory;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getService_name() {
            return service_name;
        }

        public void setService_name(String service_name) {
            this.service_name = service_name;
        }

        public String getAct_charge() {
            return act_charge;
        }

        public void setAct_charge(String act_charge) {
            this.act_charge = act_charge;
        }

        public String getCurr_charge() {
            return curr_charge;
        }

        public void setCurr_charge(String curr_charge) {
            this.curr_charge = curr_charge;
        }

        public String getSpan() {
            return span;
        }

        public void setSpan(String span) {
            this.span = span;
        }

        public int getTime_hours() {
            return time_hours;
        }

        public void setTime_hours(int time_hours) {
            this.time_hours = time_hours;
        }

        public int getCharge_in_num() {
            return charge_in_num;
        }

        public void setCharge_in_num(int charge_in_num) {
            this.charge_in_num = charge_in_num;
        }

        public double getTime_mins() {
            return time_mins;
        }

        public void setTime_mins(double time_mins) {
            this.time_mins = time_mins;
        }

        public double getSpan_in_mins() {
            return span_in_mins;
        }

        public void setSpan_in_mins(double span_in_mins) {
            this.span_in_mins = span_in_mins;
        }

        public String getCategory() {
            return category;
        }
    }

    public class category implements Serializable{
        String title;
        TreeMap<String,SubCategory> objectLinkedHashMap;

        public category(String title, TreeMap<String, SubCategory> objectLinkedHashMap) {
            this.title = title;
            this.objectLinkedHashMap = objectLinkedHashMap;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public TreeMap<String, SubCategory> getObjectLinkedHashMap() {
            return objectLinkedHashMap;
        }

    }

    public class SubCategory implements Serializable{
        TreeMap<String, service> servicesHashmap;
        String title;

        public SubCategory(String title, TreeMap<String, service> servicesHashmap) {
            this.title = title;
            this.servicesHashmap = servicesHashmap;
        }

        public TreeMap<String, service> getServicesHashmap() {
            return servicesHashmap;
        }

        public String getTitle() {
            return title;
        }
    }
}
