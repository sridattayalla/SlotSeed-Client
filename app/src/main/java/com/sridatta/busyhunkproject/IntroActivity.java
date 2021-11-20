package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sridatta.busyhunkproject.intro.AppIntroFragment;
import com.sridatta.busyhunkproject.intro.SeedsFragment;
import com.sridatta.busyhunkproject.intro.ShareIntroFragment;

import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity {
    ViewPager viewPager;
    LinearLayout linearLayout;
    TextView title;
    Button skip, gotit;
    private ImageView[] dots;
    int slide_pages=3, current_page=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        //initiating ui
        viewPager = findViewById(R.id.intro_viewpager);
        linearLayout = findViewById(R.id.slider_dots);
        skip = findViewById(R.id.skip);
        gotit = findViewById(R.id.gotit);
        title = findViewById(R.id.toolbar_title);
        //title
        String first = "Slot";
        String next = "<font color = '#388E3C'>Seed</font>";
        title.setText(Html.fromHtml("<b>" + first + next + "</b>"));
        //setting adapter
        viewPager.setAdapter(new pagerAdapter(getSupportFragmentManager()));

        //setting dots
        dots=new ImageView[slide_pages];
        for (int i=0;i<slide_pages;i++){
            dots[i]=new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_not_selected_layout));

            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(4,0,4,0);
            linearLayout.addView(dots[i],params);
        }

        /*intially setting first dot as selected*/
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_setlected_layout));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i=0;i<slide_pages;i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_not_selected_layout));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_setlected_layout));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //skip onClick
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make intro visited
                SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor=preferences.edit();
                editor.putInt("VISITED_INTRO",1);
                editor.commit();
                Intent loginIntent = new Intent(IntroActivity.this, Login_Register.class);
                startActivity(loginIntent);
                finish();
            }
        });

        //got it onClick
        gotit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_page=viewPager.getCurrentItem();
                if(current_page==slide_pages-1){
                    //make intro visited
                    SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putInt("VISITED_INTRO",1);
                    editor.commit();
                    Intent loginIntent = new Intent(IntroActivity.this, Login_Register.class);
                    startActivity(loginIntent);
                    finish();
                }
                else {
                    viewPager.setCurrentItem(current_page + 1, true);
                }
            }
        });

    }


    class pagerAdapter extends FragmentPagerAdapter {
        public pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return new AppIntroFragment();
                case 1:return new SeedsFragment();
                case 2:return new ShareIntroFragment();
                default:return new AppIntroFragment();
            }
        }

        @Override
        public int getCount() {
            return slide_pages;
        }
    }
}
