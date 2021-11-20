package com.sridatta.busyhunkproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity implements ForceUpdateChecker.OnUpdateNeededListener {
    FirebaseUser user;
    int registered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        check();
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();

    }

    private void check() {
        //shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPreferences.getInt("VISITED_INTRO",0) == 0){
            Intent introIntent = new Intent(SplashScreen.this, IntroActivity.class);
            startActivity(introIntent);
            finish();
        }
        else{
            checkUserSignIn();
        }
    }

    private void checkUserSignIn(){
        //shared preferences
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        registered=sharedPreferences.getInt("IS_REGISTERED",0);
        //initiating auth
        user=FirebaseAuth.getInstance().getCurrentUser();
        //two step checking and allowing
        if(user!=null&&registered==1){
            Intent mainActivityIntent=new Intent(SplashScreen.this,MainActivityLatest.class);
            startActivity(mainActivityIntent);
            finish();
        }
        else{
            FirebaseAuth.getInstance().signOut();
            registered=0;
            user=null;
            Intent LoginIntent=new Intent(SplashScreen.this,Login_Register.class);
            startActivity(LoginIntent);
            finish();
        }
    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue reposting.")
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
