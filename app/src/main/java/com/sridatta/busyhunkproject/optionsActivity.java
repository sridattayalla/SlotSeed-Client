package com.sridatta.busyhunkproject;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class optionsActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener , View.OnClickListener {
    private static final String SHOW ="show",HIDE="hide",INTERNET="INTERNET",LOADING="loading",DETAILS="details" ;
    Toolbar toolbar;
    private ImageView location_icon;
    private Button retry;
    LottieAnimationView animationView;
    private TextView loadingText,name,location,internet_fail_text,wallet_cash;
    CircleImageView profileImage;
    String UID;
    private View name_location_sep;
    private ConstraintLayout profile_layout,share_layout,wallet_layout,booking_layout,favourite_layout,about_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        //firebase uid
        UID=FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        if(UID==null||UID.length()==0){LogOut();}
        //initiating ui
        toolbar=findViewById(R.id.toolbar2);
        animationView=findViewById(R.id.loading_anim);
        loadingText=findViewById(R.id.loadingText);
        name=findViewById(R.id.name);
        location=findViewById(R.id.location);
        location_icon=findViewById(R.id.location_icon);
        name_location_sep=findViewById(R.id.name_loc_sep);
        internet_fail_text=findViewById(R.id.internet_fail_text);
        retry=findViewById(R.id.retry); retry.setOnClickListener(this);
        profileImage=findViewById(R.id.profile_image); profileImage.setOnClickListener(this);
        profile_layout=findViewById(R.id.profile_layout); profile_layout.setOnClickListener(this);
        share_layout=findViewById(R.id.share_layout);share_layout.setOnClickListener(this);
        wallet_layout=findViewById(R.id.wallet);wallet_layout.setOnClickListener(this);
        booking_layout=findViewById(R.id.booking_layout);booking_layout.setOnClickListener(this);
        favourite_layout=findViewById(R.id.favoutites_layout);favourite_layout.setOnClickListener(this);
        about_layout=findViewById(R.id.about_layout);about_layout.setOnClickListener(this);
        wallet_cash=findViewById(R.id.textView11);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_white));
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //getting details
        GetDetails();

        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                overridePendingTransition(R.anim.options_to_main_enter, R.anim.options_to_main_exit);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void LogOut(){
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("IS_REGISTERED",0);
        editor.commit();
        Intent login_register_intent=new Intent(optionsActivity.this,Login_Register.class);
        login_register_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login_register_intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.options_to_main_enter, R.anim.options_to_main_exit);
    }

    private void setIt(String ui,String command){
        switch (ui){
            case DETAILS:
                if(command.equals(SHOW)){
                    setIt(INTERNET,HIDE);
                    setIt(LOADING,HIDE);
                name.setVisibility(View.VISIBLE);
                location.setVisibility(View.VISIBLE);
                profileImage.setVisibility(View.VISIBLE);
                location_icon.setVisibility(View.VISIBLE);
                name_location_sep.setVisibility(View.VISIBLE);
            }
                if(command.equals(HIDE)){
                    name.setVisibility(View.INVISIBLE);
                    location.setVisibility(View.INVISIBLE);
                    profileImage.setVisibility(View.INVISIBLE);
                    location_icon.setVisibility(View.INVISIBLE);
                    name_location_sep.setVisibility(View.INVISIBLE);
                }
                break;

            case INTERNET:
                if(command.equals(SHOW)){
                    setIt(DETAILS,HIDE);
                    setIt(LOADING,HIDE);
                    internet_fail_text.setVisibility(View.VISIBLE);
                    retry.setVisibility(View.VISIBLE);}
                if(command.equals(HIDE)){
                    internet_fail_text.setVisibility(View.GONE);
                    retry.setVisibility(View.GONE);}
                break;

            case LOADING:
                if(command.equals(SHOW)){
                    setIt(DETAILS,HIDE);
                    setIt(INTERNET,HIDE);
                    animationView.setVisibility(View.VISIBLE);
                    animationView.playAnimation();
                    loadingText.setVisibility(View.VISIBLE);}
                if(command.equals(HIDE)){animationView.pauseAnimation();
                    animationView.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);}
                    break;
        }
    }

    void GetDetails(){
        setIt(DETAILS,SHOW);
//        checkConnection();
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
                                profileImage.setImageDrawable(getResources().getDrawable(R.drawable.boy));
                        }
                    }
                    if(thumb_url!=null){
                        if(gender.equals(getResources().getString(R.string.male))){

                            Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy).into(profileImage);
                        }
                        else{
                            Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.girl).into(profileImage);
                        }
                    }
                    //setting name and location
                    name.setText(temp_name);
                    location.setText(temp_location);

                    setIt(DETAILS,SHOW);
                }
                else{
                    Log.d("project_error_finder","logout at line 173 optionsActivity");
                    LogOut();}

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
                 wallet_cash.setText(dataSnapshot.getValue().toString());
             }
             else{
                 wallet_cash.setText("opps!");
             }
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {
             Log.d("datta_text","error in options actvty while getiing wallet details: "+databaseError.toString());
             wallet_cash.setText("opps!");
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
                Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.boy).into(profileImage);
            } else {
                Picasso.with(getApplicationContext()).load(thumb_url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.girl).into(profileImage);
            }
        }
    }

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        if(!isConnected){setIt(INTERNET,SHOW);}
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(!isConnected){setIt(INTERNET,SHOW);}
    }

    @Override
    public void onClick(View view) {
        Log.d("test click","clicked");
        switch (view.getId()){
            case R.id.profile_image:Intent profile_intent1=new Intent(optionsActivity.this,ProfileActivity.class);
                Pair[] pair1=new Pair[3];
                pair1[0]=new Pair(profileImage,"profile_image");
                pair1[1]=new Pair(name,"name");
                pair1[2]=new Pair(location,"location");
                ActivityOptions options1=ActivityOptions.makeSceneTransitionAnimation(optionsActivity.this,pair1);
                startActivity(profile_intent1,options1.toBundle());
                break;
            case R.id.profile_layout: Intent profile_intent=new Intent(optionsActivity.this,ProfileActivity.class);
                Pair[] pair2=new Pair[3];
                pair2[0]=new Pair(profileImage,"profile_image");
                pair2[1]=new Pair(name,"name");
                pair2[2]=new Pair(location,"location");
                ActivityOptions options2=ActivityOptions.makeSceneTransitionAnimation(optionsActivity.this,pair2);
                startActivity(profile_intent,options2.toBundle());
                break;
            case R.id.share_layout:startActivity(new Intent(optionsActivity.this,ShareActivity.class)); break;
            case R.id.wallet:Intent wallet_intent=new Intent(optionsActivity.this,WalletActivity.class);
                Pair pair=new Pair(wallet_cash,"wallet_cash");
                ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(optionsActivity.this,pair);
                startActivity(wallet_intent,options.toBundle());
                break;
            case R.id.booking_layout:startActivity(new Intent(optionsActivity.this,BookingsActivity.class)); break;
            case R.id.favoutites_layout:startActivity(new Intent(optionsActivity.this,FavouritesActivity.class)); break;
            case R.id.about_layout:startActivity(new Intent(optionsActivity.this,AboutActivity.class)); break;
            case R.id.retry: GetDetails(); break;
        }
    }
}
