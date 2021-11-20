package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sridatta.busyhunkproject.provider_details.Details;
import com.sridatta.busyhunkproject.provider_details.Services;
import com.sridatta.busyhunkproject.provider_top_images.image1;
import com.sridatta.busyhunkproject.provider_top_images.image2;
import com.sridatta.busyhunkproject.provider_top_images.image3;
import com.sridatta.busyhunkproject.provider_top_images.image4;

import java.util.Timer;
import java.util.TimerTask;

public class ProviderActivity extends AppCompatActivity {
    Toolbar toolbar;
    public String PROVIDER_UID, GENDER;
    private String PROVIDER_NAME, REFFERAL_REWARD_CATEGORY;
    ViewPager image_viewpager,details_viewPager;
    TabLayout details_tabLayout;
    private ImageView[] dots;
    private LinearLayout dots_layout;
    private top_images_interface mtop_images_interface;
    int current_page=0,top_gallery_pages=4;
    Timer timer;
    final long DELAY_MS=500;
    final long PERIOD_MS=4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        //intent
        PROVIDER_UID=getIntent().getStringExtra("uid");
        Log.d("datta_text",PROVIDER_UID);
        GENDER=getIntent().getStringExtra("gender");
        PROVIDER_NAME=getIntent().getStringExtra("name");
        REFFERAL_REWARD_CATEGORY = getIntent().getStringExtra("referral_reward_category");
        //initiating ui
        details_tabLayout=findViewById(R.id.tabLayout2);
        details_viewPager=findViewById(R.id.details_viewpager);
        image_viewpager=findViewById(R.id.top_offers_view_pager);
       /*to prevent destroting of fragments*/ image_viewpager.setOffscreenPageLimit(4);
        toolbar=findViewById(R.id.provider_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(PROVIDER_NAME);

        //setting adapter and tablayout for details
        details_viewPager.setAdapter(new ProviderDetailsAdapter(getSupportFragmentManager()));
        details_tabLayout.setupWithViewPager(details_viewPager);
        //setting adapter for images
        image_viewpager.setAdapter(new imageAdapter(getSupportFragmentManager()));
        //dots
        dots=new ImageView[top_gallery_pages];
        dots_layout=findViewById(R.id.slider_dots);
        for (int i=0;i<top_gallery_pages;i++){
            dots[i]=new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_not_selected_layout));

            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(4,0,4,0);
            dots_layout.addView(dots[i],params);
        }

        /*intially setting first dot as selected*/
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_setlected_layout));

        image_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i=0;i<top_gallery_pages;i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_not_selected_layout));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_setlected_layout));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



//        //booking onClick
//        booking_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent salon_services_intent=new Intent(ProviderActivity.this,SalonServicesActivity.class);
//                salon_services_intent.putExtra("uid",PROVIDER_UID);
//                salon_services_intent.putExtra("name",PROVIDER_NAME);
//                salon_services_intent.putExtra("gender",GENDER);
//                startActivity(salon_services_intent);
//            }
//        });

        //starting slide show
        startTopOfferSlideShow();
    }

    private void startTopOfferSlideShow(){
        timer=new Timer();
        final Handler handler=new Handler();
        final Runnable scroll=new Runnable() {
            @Override
            public void run() {
                current_page=image_viewpager.getCurrentItem();
                if(current_page==top_gallery_pages-1){
                    current_page=-1;
                }
                image_viewpager.setCurrentItem(current_page+1,true);
            }
        };
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(scroll);
            }
        },DELAY_MS,PERIOD_MS);
    }

    public void showServices(View v){
        Intent salon_services_intent=new Intent(ProviderActivity.this, SalonServicesActivity.class);
        salon_services_intent.putExtra("uid",PROVIDER_UID);
        salon_services_intent.putExtra("gender",GENDER);
        salon_services_intent.putExtra("name",PROVIDER_NAME);
        startActivity(salon_services_intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface top_images_interface{
        void onDataReceived(String provider_uid);

    }

    public void setAboutDataListener(top_images_interface listener) {
        this.mtop_images_interface = listener;
    }

    class imageAdapter extends FragmentPagerAdapter{
        public imageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:return new image1().getInstance(PROVIDER_UID);
                case 1:return new image2().getInstance(PROVIDER_UID);
                case 2:return new image3().getInstance(PROVIDER_UID);
                case 3:return new image4().getInstance(PROVIDER_UID);
                default:return new image1().getInstance(PROVIDER_UID);
            }

        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    class ProviderDetailsAdapter extends FragmentPagerAdapter{

        String[] titles={"Services","Details"};
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
        public ProviderDetailsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:return new Services().getInstance(PROVIDER_UID,GENDER,PROVIDER_NAME, REFFERAL_REWARD_CATEGORY);
                case 1:return new Details().getInstance(PROVIDER_UID,GENDER,PROVIDER_NAME);
                default:return new Services().getInstance(PROVIDER_UID,GENDER,PROVIDER_NAME, REFFERAL_REWARD_CATEGORY);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
