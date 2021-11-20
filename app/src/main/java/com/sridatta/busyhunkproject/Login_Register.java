package com.sridatta.busyhunkproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.poovam.pinedittextfield.LinePinField;
import com.poovam.pinedittextfield.PinField;
//import com.sridatta.busyhunkproject.sms.SmsListener;
//import com.sridatta.busyhunkproject.sms.SmsReceiver;

import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Login_Register extends AppCompatActivity {
    //ui elements
    Button send_otp,resend_otp,proceed,sign_in;
    CardView otp_card,phone_num_card;
    EditText mobile_num;
    LinePinField otp;
    TextView signInSignUpTextView, numberCount, mobile_num_entered, privacy_policy;
    Timer timer;
    final long DELAY_MS=1000;
    final long PERIOD_MS=200;
    String signInSignUp = "SignIn/SignUp", random_text = "psdftgrt$w@!*-^BNs";
    int cursor_position = 0;//cursor position of signInSignUp textview
    ProgressDialog pd;
    LocationManager locationManager;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    FirebaseAuth auth;
    CountDownTimer ct;
    long startTime=120000;
    String verification_code;
    boolean userExist=false, otp_Verified = false;

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    sendSMS();
                }
                else{ Toast.makeText(getApplicationContext(),"You should have to enter otp manually",Toast.LENGTH_SHORT).show();
                sendSMS();}
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login__register);
        //initiating ui elements
        pd=new ProgressDialog(this);
        privacy_policy = findViewById(R.id.privacy_poicy);
        //otpcard
        mobile_num_entered = findViewById(R.id.mobile_num_entered);
        //verify=findViewById(R.id.verify);
        resend_otp=findViewById(R.id.resend_otp);
        otp_card=findViewById(R.id.otp_card);
        otp=findViewById(R.id.otp);
        //otp.addTextChangedListener(OtpWatcher);
        //phone_num card
        signInSignUpTextView = findViewById(R.id.textView4);
        numberCount = findViewById(R.id.textView75);
        send_otp=findViewById(R.id.send_otp);
        mobile_num=findViewById(R.id.emaillayout);
        mobile_num.addTextChangedListener(phoneNumWatcher);
        phone_num_card=findViewById(R.id.phone_num_card);

        //type signInSignUp
        typeSignInSignUp();


        //send otp on click
        send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make otpverifie false
                otp_Verified = false;
                //checking sms permissions
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.RECEIVE_SMS)
//                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
//                            android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
//                        requestPermissions(new String[]{android.Manifest.permission.RECEIVE_SMS,
//                                android.Manifest.permission.READ_SMS,
//                                Manifest.permission.INTERNET}, 10);
//                        return;
//                    }
//                    else{
//                        sendSMS();
//                    }
//                } else {
//                    sendSMS();
//                }
                /*remove this sendSMS() if you want to use auto read sms feature*/
                sendSMS();

            }
        });


        //resend otp
        resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_otp.performClick();
            }
        });

        //verify otp
        otp.setOnTextCompleteListener(new PinField.OnTextCompleteListener() {
            @Override
            public boolean onTextComplete (@NotNull String enteredText) {
                verifyOtp(enteredText);
                return false; // Return true to keep the keyboard open else return false to close the keyboard
            }
        });
//        verify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                verifyOtp(otp.getText().toString());
//            }
//        });

        //privacy policy onClick
        privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://busyhunkproject.firebaseapp.com/");
                Intent privacy_policyIntent = new Intent(Intent.ACTION_VIEW, uri);
                privacy_policyIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(privacy_policyIntent);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://busyhunkproject.firebaseapp.com/")));
                }
            }
        });

        //inititating firebase objects
        auth=FirebaseAuth.getInstance();
        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                //Toast.makeText(getApplicationContext(),"Verified",Toast.LENGTH_LONG).show();
                Log.d("datta_text","credentials are "+phoneAuthCredential);
                if(!otp_Verified){
                    pd.setMessage("Verifying OTP...");
                    pd.show();
                    signInWithCredintials(phoneAuthCredential);
                    //make otp verified to true that will stop auto sms retrival otp verification
                    otp_Verified = true;
                }

            }


            @Override
            public void onVerificationFailed(FirebaseException e) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(),"Verification failed :(",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verification_code=s;
                pd.dismiss();
                //otp time counter
                startTime=120000;
                /*disable button*/resend_otp.setEnabled(false);
                ct=new CountDownTimer(startTime,1000) {
                    @Override
                    public void onTick(long l) {
                        startTime=l;
                        int minutes=(int)(startTime/1000/60);
                        int seconds=(int)(startTime/1000)%60;
                        String timeFormatted=String.format("%02d:%02d",minutes,seconds);
                        resend_otp.setText("Otp can resend in "+timeFormatted);

                    }

                    @Override
                    public void onFinish() {
                        resend_otp.setText("Resend Otp");
                        resend_otp.setEnabled(true);
                    }
                }.start();
                //remove phone num card visibility
                //set otp card vicibility
                //set entered phone number text
                otp_card.setVisibility(View.VISIBLE);
                /*aimation for otp card entry*/otp_card.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.options_to_main_enter));
                phone_num_card.setVisibility(View.GONE);
                mobile_num_entered.setText("Verification code was sent to your mobile number "+ mobile_num.getText().toString());

            }
            
        };

    }

    //method to typing animation of SignIn/SignUp
    void typeSignInSignUp(){
        final Handler handler = new Handler();
        final Runnable type = new Runnable() {
            @Override
            public void run() {
                if(cursor_position > 0 && cursor_position < signInSignUp.length() - 1){
                    String curr_text = signInSignUpTextView.getText().toString();
                    String concated_text = curr_text.substring(0, cursor_position);
                    Log.d("datta_text", concated_text + signInSignUp.charAt(cursor_position));
                    String text_to_add = signInSignUp.charAt(cursor_position) + curr_text.substring(cursor_position + 1);
                    signInSignUpTextView.setText(concated_text + text_to_add);
                    cursor_position++;
                }
                else if(cursor_position == 0){
                    String curr_text = signInSignUpTextView.getText().toString();
                    Log.d("datta_text", signInSignUp.charAt(cursor_position) +  curr_text.substring(cursor_position + 1));
                    signInSignUpTextView.setText(signInSignUp.charAt(cursor_position) + curr_text.substring(cursor_position + 1));
                    cursor_position++;
                }
                else if(cursor_position == signInSignUp.length() - 1){
                    String curr_text = signInSignUpTextView.getText().toString();
                    String concated_text = curr_text.substring(0, cursor_position);
                    Log.d("datta_text", concated_text + signInSignUp.charAt(cursor_position));
                    signInSignUpTextView.setText(concated_text + signInSignUp.charAt(cursor_position));
                    cursor_position++;
                }
            }
        };
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //if cursor position is exceeding cancel timer else call type method
                //increment cursor position
                if(cursor_position < signInSignUp.length()) {
                    handler.post(type);
                }
                else {
                    cancel();
                }
            }
        }, DELAY_MS, PERIOD_MS);
    }

    //phone num watcher
    private final TextWatcher phoneNumWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            //cout textview
            numberCount.setText(Integer.toString(s.length()) + "/10");
            if(s.length()>=10){
                send_otp.setEnabled(true);
                send_otp.setBackground(getDrawable(R.drawable.login_button_primry_shape));
            }
            else{
                send_otp.setEnabled(false);
                send_otp.setBackground(getDrawable(R.drawable.login_button_inactive_shape));
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    //otp watcher
    /*private final TextWatcher OtpWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length

            if(s.length()>=6){
                verify.setEnabled(true);
                verify.setBackground(getDrawable(R.drawable.login_button_primry_shape));
            }
            else{
                verify.setEnabled(false);
                verify.setBackground(getDrawable(R.drawable.login_button_inactive_shape));
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };*/

    //send sms
    void sendSMS(){
        pd.setTitle("Please Wait");
        pd.setMessage("Verifying mobile number.. ");
        pd.setCancelable(false);
        pd.show();
        String phone_num="+91"+mobile_num.getText().toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone_num,60, TimeUnit.SECONDS,this,callbacks
        );


        //sms auto retrival , this block of code not reqire if auto retrival option not use
//        SmsReceiver.bindListener(new SmsListener() {
//            @Override
//            public void messageReceived(String messageText) {
//                Log.d("Text",messageText);
//                /*for ux, setting text in otp edit text*/otp.setText(messageText);
//                if(!otp_Verified){
//                    verifyOtp(messageText);
//                    otp_Verified = true;
//                }
//            }
//        });
    }

    public void verifyOtp(String otp){
        pd.setMessage("Verifying OTP...");
        pd.show();
        String OTP=otp;
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verification_code,OTP);
        if(credential!=null){
            signInWithCredintials(credential);}
    }

    //sign in method
    public void signInWithCredintials(PhoneAuthCredential credential){
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d("datta_text",task.toString());
                    //checking user already registered or not
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Get new Instance ID token
                                                    String token = task.getResult().getToken();
                                                    DatabaseReference Tokenreference= FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("device_token");
                                                    Tokenreference.setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                                                SharedPreferences.Editor editor=preferences.edit();
                                                                editor.putInt("IS_REGISTERED",1);
                                                                //saving gender, name, location, image url offline
                                                                editor.putString("gender", dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("gender").getValue().toString());
                                                                editor.putString("name", dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").getValue().toString());
                                                                editor.putString("location", dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location").getValue().toString());
                                                                try {
                                                                    editor.putString("image_url", dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("thumb_url").getValue().toString());
                                                                }catch (Exception e){}
                                                                editor.commit();
                                                                pd.dismiss();
                                                                Intent mainActivityintent = new Intent(Login_Register.this,MainActivityLatest.class);
                                                                startActivity(mainActivityintent);
                                                                finish();
                                                            }
                                                            else{
                                                                Toast.makeText(getApplicationContext(),"Failed :(",Toast.LENGTH_SHORT).show();
                                                                pd.dismiss();
                                                            }
                                                        }
                                                    });
                                                }
                                                else{
                                                    Log.w("datta_text", "getInstanceId failed", task.getException());
                                                    pd.dismiss();
                                                }

                                            }
                                        });

                            }
                            else{
                                pd.dismiss();
                                Intent RegisterActivityintent=new Intent(Login_Register.this,RegisterActivity.class);
                                RegisterActivityintent.putExtra("PHONE_NUMBER",mobile_num.getText().toString());
                                startActivity(RegisterActivityintent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                    });
                }
                else{
                    pd.dismiss();
                    //Toast.makeText(getApplicationContext(),"Incorrect Otp/There is some problem with your Internet Connection",Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),"Some problem encountered with your account :(" ,Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}
