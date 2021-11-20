package com.sridatta.busyhunkproject;

import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sridatta.busyhunkproject.TopOffers.OfferOfTheWeek;
import com.sridatta.busyhunkproject.TopOffers.Providers;
import com.sridatta.busyhunkproject.TopOffers.Share;
import com.sridatta.busyhunkproject.TopOffers.UpdateValue;

import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivityLatest extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ForceUpdateChecker.OnUpdateNeededListener, locationSearchDialog.location_interface {
    AppBarLayout appBarLayout;
    ConstraintLayout topOppersconstrainLayout, search_layout;
    ViewPager viewPager;
    LinearLayout dots_layout;
    private ImageView[] dots;
    int current_page=0,top_offer_pages=2;
    Timer timer;
    final long DELAY_MS=500;
    final long PERIOD_MS=5000;
    BottomNavigationView bottomNavigationView;
    NavigationView navigationView;
    CircleImageView profile_image;
    TextView name, location;
    String UID;
    DrawerLayout drawer;
    int checkedNavItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_latest);
        //firebase uid
        UID= FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        //initiating ui
        search_layout = findViewById(R.id.search_layout);
        navigationView = findViewById(R.id.nav_view);
        View hview = navigationView.getHeaderView(0);
        name = hview.findViewById(R.id.name);
        location = hview.findViewById(R.id.location);
        profile_image = hview.findViewById(R.id.profile_image);
        bottomNavigationView  = findViewById(R.id.main_activity_bottom_nav);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); toolbar.setTitle(null);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        appBarLayout = findViewById(R.id.mainactivity_appbarlayout);
        topOppersconstrainLayout = findViewById(R.id.top_offers_constrainlayout);
        viewPager=findViewById(R.id.top_offers_view_pager);
        dots_layout=findViewById(R.id.slider_dots);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //setting user deatils details
        GetDetails();

        //setting adapter
        viewPager.setAdapter(new pagerAdapter(getSupportFragmentManager()));

        //setting dots
        dots=new ImageView[top_offer_pages];
        for (int i=0;i<top_offer_pages;i++){
            dots[i]=new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_not_selected_layout));

            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8,0,8,0);
            dots_layout.addView(dots[i],params);
        }

        /*intially setting first dot as selected*/
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_setlected_layout));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i=0;i<top_offer_pages;i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_not_selected_layout));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.dot_setlected_layout));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //starting slide show
        startTopOfferSlideShow();

        //adjust constraint layout height
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        final float dpWidth  = outMetrics.widthPixels;
        viewPager.getLayoutParams().height = (int)(dpWidth/16*9);
        viewPager.requestLayout();

        /*playing with appbar layout(:*/
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //Toast.makeText(getApplicationContext(), Integer.toString(verticalOffset), Toast.LENGTH_SHORT).show();
                //Log.d("datta_text", Integer.toString(verticalOffset));
                int collapse_layout_height = (int)dpWidth/16*9 + 16 + 50 - toolbar.getLayoutParams().height - 20;
                //Log.d("datta_text", "collapse layout height: " + Integer.toString(collapse_layout_height));
                if(verticalOffset<-collapse_layout_height && toolbar.getBackground()!=getResources().getDrawable((R.color.white))){
                    toolbar.setBackground(getResources().getDrawable((R.color.white)));
//                    int i = (int) (70 * getApplicationContext().getResources().getDisplayMetrics().density + 0.5f);
                    toolbar.getLayoutParams().width = (int)dpWidth;
                    toolbar.requestLayout();
                    toolbar.getMenu().setGroupVisible(R.id.search, false);
                }
                else if(verticalOffset>=-collapse_layout_height && toolbar.getBackground()!=getResources().getDrawable(R.drawable.right_rounded_corners)){
                    toolbar.setBackground(getResources().getDrawable(R.drawable.right_rounded_corners));
                    toolbar.getLayoutParams().width = (int) (70 * getApplicationContext().getResources().getDisplayMetrics().density + 0.5f);
                    toolbar.requestLayout();
                    toolbar.getMenu().setGroupVisible(R.id.search, true);
                }
            }
        });

        //location onClick
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment location_dialog = locationSearchDialog.newInstance();
                location_dialog.setStyle(DialogFragment.STYLE_NO_FRAME,R.style.DialogFragmentTheme);
                location_dialog.show(ft, "dialog");
            }
        });

        //bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.restraunt:
                        Intent restrauntIntent = new Intent(MainActivityLatest.this, RestrauntsMainActivity.class);
                        startActivity(restrauntIntent);
                        finish();
                        break;
                    case R.id.car_wash:
                        Intent carIntent = new Intent(MainActivityLatest.this, CarServiceMainActivity.class);
                        startActivity(carIntent);
                        finish();
                        break;
                }
                return false;
            }
        });

        //price cutter ads
        setPromoAds(getResources().getString(R.string.male));

        //drawer
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                switch (checkedNavItem){
                    case R.id.my_account: Intent profile_intent=new Intent(MainActivityLatest.this,ProfileActivity.class);
                        Pair[] pair2=new Pair[3];
                        pair2[0]=new Pair(profile_image,"profile_image");
                        pair2[1]=new Pair(name,"name");
                        pair2[2]=new Pair(location,"location");
                        ActivityOptions options2=ActivityOptions.makeSceneTransitionAnimation(MainActivityLatest.this,pair2);
                        startActivity(profile_intent,options2.toBundle());
                        break;
                    case R.id.share:startActivity(new Intent(MainActivityLatest.this,ShareActivity.class)); break;
                    case R.id.wallet:Intent wallet_intent=new Intent(MainActivityLatest.this,WalletActivity.class);
                        startActivity(wallet_intent);
                        break;
                    case R.id.bookings:startActivity(new Intent(MainActivityLatest.this,BookingsActivity.class)); break;
                    case R.id.favorites:startActivity(new Intent(MainActivityLatest.this,FavouritesActivity.class)); break;
                    case R.id.about:startActivity(new Intent(MainActivityLatest.this,AboutActivity.class)); break;

                }
                checkedNavItem = -1;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        //search layout onClick
        search_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchActiviytIntent = new Intent(MainActivityLatest.this, searchActivity.class);
                startActivity(searchActiviytIntent);
            }
        });
    }

    @Override
    public void getChoosedLocation(String choosed_location_name) {
        boolean status  = new UpdateValue().updateLocation(choosed_location_name, getApplicationContext());
        if(!status){
            Toast.makeText(getApplicationContext(), "Problem in changing location :(", Toast.LENGTH_SHORT).show();
        }
    }

    class pagerAdapter extends FragmentPagerAdapter {
        public pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:return new Providers();
                case 1:return new Share();
                case 2:return new OfferOfTheWeek();
                default:return new Share();
            }
        }

        @Override
        public int getCount() {
            return top_offer_pages;
        }
    }

    private void setPromoAds(String gender){
        PromocodeAdFragment promocodeAdFragment=new PromocodeAdFragment();
        Bundle args=new Bundle();
        args.putString("gender",gender);
        promocodeAdFragment.setArguments(args);
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.pricecutter_adv_container,promocodeAdFragment).commit();
    }

    private void startTopOfferSlideShow(){
        final Handler handler=new Handler();
        final Runnable scroll=new Runnable() {
            @Override
            public void run() {
                current_page=viewPager.getCurrentItem();
                if(current_page==top_offer_pages-1){
                    current_page=-1;
                }
                viewPager.setCurrentItem(current_page+1,true);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_latest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            Intent searchActiviytIntent = new Intent(MainActivityLatest.this, searchActivity.class);
            startActivity(searchActiviytIntent);
            return true;
        }
        else if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        checkedNavItem = id;
        return true;
    }





    void GetDetails(){
        //setting ofline data
        setOfflineData();
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(UID);
        /*offline capability*/ databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*if data deleted in database unknowingly we have to check is there any thing under user node and logout if not*/
                if(dataSnapshot.hasChild("name")&&dataSnapshot.hasChild("location")){
                    String temp_name=dataSnapshot.child("name").getValue().toString();
                    String temp_location=dataSnapshot.child("location").getValue().toString();
                    String gender=dataSnapshot.child("gender").getValue().toString();
                    Log.d("datta_text","gender is "+gender);
                    String thumb_url=null;
                    try{
                        thumb_url=dataSnapshot.child("thumb_url").getValue().toString();
                    }
                    catch (Exception e){
                        Log.d("datta_text","profile_url error "+e.toString());
                        //setting image by gender
                        if(gender.equals(getResources().getString(R.string.male))){
                            Log.d("datta_text","gender error :)");
                            profile_image.setImageDrawable(getResources().getDrawable(R.drawable.boy));
                        }
                    }
                    if(thumb_url!=null){
                        if(gender.equals(getResources().getString(R.string.male))){

                            Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy).into(profile_image);
                        }
                        else{
                            Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.girl).into(profile_image);
                        }
                    }
                    //setting name and location
                    name.setText(temp_name);
                    location.setText(temp_location);

                }
                else{
                    Log.d("project_error_finder","logout at line 173 optionsActivity"); }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //getting walletcash
        DatabaseReference wallet_ref=FirebaseDatabase.getInstance().getReference().child("user_wallet/"+UID+"/seeds");
        wallet_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    //wallet_cash.setText(dataSnapshot.getValue().toString());
                }
                else{
                    //wallet_cash.setText("opps!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text","error in options actvty while getiing wallet details: "+databaseError.toString());
                //wallet_cash.setText("opps!");
            }
        });
    }

    private void setOfflineData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gender = preferences.getString("gender", getResources().getString(R.string.male));
        String thumb_url = preferences.getString("image_url", null);
        //settig details
        name.setText(preferences.getString("name", getResources().getString(R.string.app_name)));
        location.setText(preferences.getString("location", "Kakinada"));
        if(thumb_url!=null) {
            if (gender.equals(getResources().getString(R.string.male))) {
                Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy).into(profile_image);
            } else {
                Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.girl).into(profile_image);
            }
        }
    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue booking.")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
