package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.airbnb.lottie.LottieAnimationView;

public class CarServiceMainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    LottieAnimationView lottieAnimationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_service_main);
        //initiating ui
        lottieAnimationView = findViewById(R.id.loading_anim);lottieAnimationView.playAnimation();
        bottomNavigationView = findViewById(R.id.car_main_activity_bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.car_wash);

        //bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.restraunt:
                        Intent restrauntIntent = new Intent(CarServiceMainActivity.this, RestrauntsMainActivity.class);
                        startActivity(restrauntIntent);
                        finish();
                        break;
                    case R.id.salon:
                        Intent carIntent = new Intent(CarServiceMainActivity.this, MainActivityLatest.class);
                        startActivity(carIntent);
                        finish();
                        break;
                }
                return false;
            }
        });
    }
}
