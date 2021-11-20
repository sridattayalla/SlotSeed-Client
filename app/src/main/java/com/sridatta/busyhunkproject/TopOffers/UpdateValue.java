package com.sridatta.busyhunkproject.TopOffers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateValue {
    String UID = FirebaseAuth.getInstance().getUid().toString();
    boolean is_location_update_success = true;
    public boolean updateLocation(final String location, final Context context){
        DatabaseReference location_ref = FirebaseDatabase.getInstance().getReference().child("users/"+UID+"/location");
        location_ref.setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    is_location_update_success = false;
                }else{
                    SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putString("location", location);
                    editor.commit();
                }
            }
        });
        return is_location_update_success;
    }
}
