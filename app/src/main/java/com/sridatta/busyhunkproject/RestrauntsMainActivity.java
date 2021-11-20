package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.airbnb.lottie.LottieAnimationView;

public class RestrauntsMainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    LottieAnimationView lottieAnimationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restraunts_main);
        //initiating ui
        lottieAnimationView = findViewById(R.id.loading_anim);lottieAnimationView.playAnimation();
        bottomNavigationView = findViewById(R.id.rest_main_activity_bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.restraunt);

        //bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.salon:
                        Intent restrauntIntent = new Intent(RestrauntsMainActivity.this, MainActivityLatest.class);
                        startActivity(restrauntIntent);
                        finish();
                        break;
                    case R.id.car_wash:
                        Intent carIntent = new Intent(RestrauntsMainActivity.this, CarServiceMainActivity.class);
                        startActivity(carIntent);
                        finish();
                        break;
                }
                return false;
            }
        });
    }
}
