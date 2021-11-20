package com.sridatta.busyhunkproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;


public class RegisterActivity extends AppCompatActivity implements locationSearchDialog.location_interface, LocationPermssionFragment.response, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    TextView name,email,location_text;
    CircleImageView male_image,female_image;
    Button register,login;
    String Sname,Semail,UID,phone_number,gender=null,choosed_location_name=null,REFERRAL_UID,latitude,longitude,LOCATION_NAME;
    TextInputLayout nameLayout,emailLayout;
    Boolean genderChoosen=false,location_choosen=false;
    ProgressDialog pd;
    LocationManager locationManager;
    LocationListener locationListener;
    FusedLocationProviderClient providerClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    LocationPermssionFragment locationPermssionFragment=LocationPermssionFragment.newInstance();
    GoogleApiClient googleApiClient;
    int DISTANCE=40, REWARD_SEEDS = 51;
    String DeviceToekn =null;

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    pd.setTitle(null);
                    pd.setMessage("Please wait while getting coordinates");
                    pd.setCancelable(false);
                    pd.show();
                    buildLocationRequest();
                    buildLocationCallback();
                    providerClient= LocationServices.getFusedLocationProviderClient(this);
                    providerClient= LocationServices.getFusedLocationProviderClient(this);
                    providerClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper());
                }
                else{Toast.makeText(getApplicationContext(),"Allow BusyHunk Manager to get device location",Toast.LENGTH_SHORT).show();}
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
       /* locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            android.support.v4.app.FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            locationPermssionFragment.show(ft,"LocationPermssionFragment");
        }
        else{
            checkLocation();
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        phone_number=getIntent().getStringExtra("PHONE_NUMBER");
        //firebase
        UID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        //initiating ui
        location_text=findViewById(R.id.location);
        male_image=findViewById(R.id.male);
        female_image=findViewById(R.id.female);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        register=findViewById(R.id.register);
        login=findViewById(R.id.login);
        nameLayout=findViewById(R.id.name_text_layout);
        emailLayout=findViewById(R.id.email_input_layout);
        pd = new ProgressDialog(this);

        //location onclick
        location_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment location_dialog = locationSearchDialog.newInstance();
                location_dialog.setStyle(DialogFragment.STYLE_NO_FRAME,R.style.DialogFragmentTheme);
                location_dialog.show(ft, "dialog");
            }
        });
        //male,female image onclick
        male_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                male_image.setBackground(getResources().getDrawable(R.drawable.gender_selected_background));
                female_image.setBackground(null);
                gender=getResources().getString(R.string.male);
                genderChoosen=true;
            }
        });
        female_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                female_image.setBackground(getResources().getDrawable(R.drawable.gender_selected_background));
                male_image.setBackground(null);
                gender=getResources().getString(R.string.female);;
                genderChoosen=true;
            }
        });

        //register onclick
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Sname=name.getText().toString();
                Semail=email.getText().toString();
                if(Sname.length()<1){
                    name.setError("Enter name",getResources().getDrawable(R.drawable.error_red));
                    name.requestFocus();
                }
                else if(!isEmailValid(Semail)){
                    email.setError("inValid Email",getResources().getDrawable(R.drawable.error_red));
                    email.requestFocus();
                }
                else if(!location_choosen){
                    Toast.makeText(getApplicationContext(),"Choose location",Toast.LENGTH_SHORT).show();
                }
                else if(!genderChoosen){
                    Toast.makeText(getApplicationContext(),"Choose gender",Toast.LENGTH_SHORT).show();
                }
                else {
                   if(UID!=null&&UID.length()>0){
                       DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("users").child(UID);
                       HashMap<String,Object> details=new HashMap<>();
                       details.put("name",Sname);
                       if(Semail!=null&&Semail.length()>0){ details.put("email",Semail); }
                       if(phone_number.length()==10){details.put("phone_number",phone_number);}
                       if(!location_text.getText().toString().equals("Choose a Location*")){details.put("location",choosed_location_name);}
                       if(gender!=null){details.put("gender",gender);}
                       if(FirebaseInstanceId.getInstance().getToken()!=null){details.put("device_token",FirebaseInstanceId.getInstance().getToken());}

                       pd.setMessage("Please wait while creating your account...");
                       pd.show();
                       reference.setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                   SharedPreferences.Editor editor=preferences.edit();
                                   editor.putInt("IS_REGISTERED",1);
                                   editor.commit();
                                   /*pd.dismiss();
                                   Intent mainActivityIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                   startActivity(mainActivityIntent);
                                   finish();*/
                                   //rewarding user
                                   RewardUser();
                               }
                               else{
                                   pd.dismiss();
                                   Toast.makeText(getApplicationContext(),"Unfortunately We failed to reach our server,Please Try again!",Toast.LENGTH_SHORT).show();
                                   SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                   SharedPreferences.Editor editor=preferences.edit();
                                   editor.putInt("IS_REGISTERED",0);
                                   editor.commit();
                               }
                           }
                       });

                   }
                   else{
                       FirebaseAuth.getInstance().signOut();
                   }
                }
            }
        });

        //login on click
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login_register_intent=new Intent(RegisterActivity.this,Login_Register.class);
                startActivity(login_register_intent);
                finish();
            }
        });




        //getting refferal uid
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null && deepLink != null&& deepLink.getBooleanQueryParameter("invitedby",false)) {
                            String referrerUid = deepLink.getQueryParameter("invitedby");

                            if(referrerUid!=null){
                                REFERRAL_UID=referrerUid;
                                Log.d("datta_text","referral uid is "+referrerUid);
                                //checking location
                                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                                    android.support.v4.app.FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                                    locationPermssionFragment.show(ft,"LocationPermssionFragment");
                                }
                                else{
                                    checkLocation();
                                }
                            }
                            else{Log.d("datta_text"," ref uid is null");}

                        }
                        else{
                            if(deepLink==null){
                                Log.d("datta_text","deep link not coming");
                            }
                            else if(user==null){
                                Log.d("datta_text","user authentication error");
                            }
                            else{
                                Log.d("datta_text","no parameter called invitedby");
                            }
                            Log.d("datta_text","referrar uid not coming");
                        }
                    }
                });
    }

    void RewardUser(){
                //1.reward user with wallet seeds
                //2. once user rewarded it will check if there is any referer, then referrer will be rewarded
                DatabaseReference user_reward_ref=FirebaseDatabase.getInstance().getReference().child("user_wallet/"+UID);
                HashMap<String,Object> user_wallet=new HashMap<>();
                user_wallet.put("seeds", REWARD_SEEDS);
                HashMap<String,Object> transaction_details=new HashMap<>();
                transaction_details.put("type",getResources().getString(R.string.reward));
                transaction_details.put("action", "Reward from "+getResources().getString(R.string.app_name));
                transaction_details.put("seeds_before",0);
                transaction_details.put("added", REWARD_SEEDS);
                transaction_details.put("time_stamp", ServerValue.TIMESTAMP);
                HashMap<String,Object> user_transaction=new HashMap<>();
                user_transaction.put("signup_reward",transaction_details);
                user_wallet.put("transactions",user_transaction);

                user_reward_ref.setValue(user_wallet).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //reward referrar
                            GetDeviceToken();
                            Log.d("datta_text","Succeed in reward user");
                        }
                        else{
                            Log.d("datta_text","Failed to reward user");
                            pd.dismiss();
                            Intent mainActivityIntent=new Intent(RegisterActivity.this,MainActivityLatest.class);
                            startActivity(mainActivityIntent);
                            finish();
                        }

                    }
                });
    }

    void RewardReferrar(){
        if(REFERRAL_UID!=null&&REFERRAL_UID.length()>0&&DISTANCE<=50){
                    DatabaseReference referrar_reward_ref=FirebaseDatabase.getInstance().getReference().child("referrals/"+REFERRAL_UID+"/"+UID);
                    referrar_reward_ref.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                updateToken();
                                Log.d("datta_text","Succeed in reward referrar");
                                pd.dismiss();
                                Intent mainActivityIntent=new Intent(RegisterActivity.this,MainActivityLatest.class);
                                startActivity(mainActivityIntent);
                                finish();
                            }
                            else {
                                Log.d("datta_text","failed in reward referrar");
                                pd.dismiss();
                                Intent mainActivityIntent=new Intent(RegisterActivity.this,MainActivityLatest.class);
                                startActivity(mainActivityIntent);
                                finish();
                            }

                        }
                    });

        }
        else{
            Log.d("datta_text","No referrar to reward");
            pd.dismiss();
            Intent mainActivityIntent=new Intent(RegisterActivity.this,MainActivityLatest.class);
            startActivity(mainActivityIntent);
            finish();
        }
    }

    private void updateToken() {
        if(DeviceToekn!=null) {
            DatabaseReference toeknRef = FirebaseDatabase.getInstance().getReference().child("used_devices").child(DeviceToekn);
            toeknRef.setValue(DeviceToekn);
        }
    }

    void GetDeviceToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    // Get new Instance ID token
                    DeviceToekn = task.getResult().getToken();
                    Query tokenQuery = FirebaseDatabase.getInstance().getReference().child("used_devices").orderByKey().equalTo(DeviceToekn);
                    tokenQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() == null){
                                RewardReferrar();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }


        });
    }

    //check location is enabled or not
    void checkLocationEnable(){
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks( this)
                    .addOnConnectionFailedListener( this).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                Log.d("datta_text","call back worked");
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        RegisterActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });             }
    }

    private void checkLocation(){
        //checking location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET}, 10);
                return;
            }
            else{
                pd.setTitle(null);
                pd.setMessage("Please wait while getting coordinates");
                pd.setCancelable(false);
                pd.show();
                buildLocationRequest();
                buildLocationCallback();
                providerClient= LocationServices.getFusedLocationProviderClient(this);
                providerClient= LocationServices.getFusedLocationProviderClient(this);
                providerClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
            }
        } else {
            pd.setTitle(null);
            pd.setMessage("Please wait while getting coordinates");
            pd.setCancelable(false);
            pd.show();
            buildLocationRequest();
            buildLocationCallback();
            providerClient= LocationServices.getFusedLocationProviderClient(this);
            providerClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }
    }

    private void buildLocationRequest(){
        locationRequest=new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    private void buildLocationCallback() {
        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location:locationResult.getLocations()){
                    Log.d("datta_text","location coordinates are "+String.valueOf(location.getLatitude())+" "+String.valueOf(location.getLongitude()));
                    latitude = Double.toString(location.getLatitude());
                    longitude = Double.toString(location.getLongitude());

                    //kakinada
                    Location kakinada=new Location("");
                    kakinada.setLatitude(16.9891);
                    kakinada.setLongitude(82.2475);

                    float distanceInMeters = location.distanceTo(kakinada);
                    DISTANCE= (int) (distanceInMeters/1000);

                    //peddapuram
                    Location peddapuram = new Location("");
                    peddapuram.setLatitude(17.0757);
                    peddapuram.setLongitude(82.1360);

                    distanceInMeters = location.distanceTo(peddapuram);
                    if((int) (distanceInMeters/1000) < DISTANCE){
                        DISTANCE = (int) (distanceInMeters/1000);
                    }

                    //intimating user if he is not from provided areas
                    if(DISTANCE>50){
                        Toast.makeText(getApplicationContext(),"Sorry! you are not in our providing locations, your friend will not be rewarded",Toast.LENGTH_LONG).show();
                    }

                    Log.d("datta_text","distance between current location and rajam is "+Integer.toString(DISTANCE)+"km");
                    pd.dismiss();
                    providerClient.removeLocationUpdates(locationCallback);
                    pd.dismiss();

                }
            }
        };
    }

    public static boolean isEmailValid(String target) {
        if(target==null||target.length()==0){return true;}
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.matches();
    }


    @Override
    public void getChoosedLocation(String choosed_location_name) {
        this.choosed_location_name=choosed_location_name;
        location_text.setText(choosed_location_name);
        location_choosen=true;
    }

    private void LogOut(){
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("IS_REGISTERED",0);
        editor.commit();
        startActivity(new Intent(RegisterActivity.this, Login_Register.class));
        finish();
    }

    @Override
    public void returnResponce(int num) {
        if(num==0){

        }
        else if(num==1){
            checkLocationEnable();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
