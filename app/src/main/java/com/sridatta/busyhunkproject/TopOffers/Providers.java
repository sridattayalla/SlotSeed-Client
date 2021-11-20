package com.sridatta.busyhunkproject.TopOffers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sridatta.busyhunkproject.ProvidersActivity;
import com.sridatta.busyhunkproject.R;

public class Providers extends Fragment {

    String gender;
    View v;
    ImageView layout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.top_offers_single_layout,container,false);
        //initiating ui
        layout = v.findViewById(R.id.image_holder);
        layout.setImageResource(R.drawable.welcome_poster);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        final float dpWidth  = outMetrics.widthPixels;
        final float modified_height = dpWidth/16*9; //16 : 9 formfactor
        layout.getLayoutParams().height = (int)modified_height;
        layout.requestLayout();
        layout.setScaleType(ImageView.ScaleType.FIT_XY);

        //onclick
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent all_intent=new Intent(getActivity(),ProvidersActivity.class);
                //getting gender
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                gender = preferences.getString("gender", getResources().getString(R.string.male));
                all_intent.putExtra("gender",gender);
                startActivity(all_intent);
            }
        });
        return v;
    }

    void getGender(){
        DatabaseReference genderRef= FirebaseDatabase.getInstance().getReference().child("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+"/gender");
        genderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               try{gender=dataSnapshot.getValue().toString();}
               catch (Exception e){
                   Log.d("datta_text","Exception in getting gender in top offers "+e.toString());
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
