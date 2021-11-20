package com.sridatta.busyhunkproject.provider_details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sridatta.busyhunkproject.R;
import com.sridatta.busyhunkproject.SalonServicesActivity;

public class Details extends Fragment {
    View v;
    String PROVIDER_UID,GENDER,NAME;
    Button maps, favourites;
    TextView open_hours, closed_hours, address, about;
    LottieAnimationView loading_anim;
    ConstraintLayout details_layout;
    boolean isFavourite = false;

    public Details getInstance(String provider_uid,String gender,String name){
        PROVIDER_UID=provider_uid;
        GENDER=gender;
        NAME=name;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_provider_details,container,false);
        //initiating ui
        open_hours = v.findViewById(R.id.textView62);
        closed_hours = v.findViewById(R.id.textView65);
        address = v.findViewById(R.id.address);
        about = v.findViewById(R.id.textView70);
        loading_anim = v.findViewById(R.id.progressBar7); loading_anim.playAnimation();
        details_layout = v.findViewById(R.id.details_layout);
        favourites = v.findViewById(R.id.favourites);
        maps = v.findViewById(R.id.open_maps);
        //maps onClick
        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make button enable == false, text = please wait..
                maps.setEnabled(false);
                maps.setText("Please wait..");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("providers/salons/" + PROVIDER_UID + "/coordinates");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String latitude = dataSnapshot.child("latitude").getValue().toString();
                        String longitude = dataSnapshot.child("longitude").getValue().toString();
                        maps.setEnabled(true);
                        maps.setText("Open in maps");
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude));
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        maps.setEnabled(true);
                        maps.setText("Open in maps");
                    }
                });
            }
        });
        //favourites onClick
        favourites.setOnClickListener(new View.OnClickListener() {
            //after clicking button change button text and remove clickable
            //after completion of task, change text on button then make button clickable
            @Override
            public void onClick(View view) {
                if(!isFavourite){
                    favourites.setText("Wait...");
                    favourites.setEnabled(false);
                    final DatabaseReference fav_ref = FirebaseDatabase.getInstance().getReference().child("favourites/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + PROVIDER_UID);
                    fav_ref.setValue(PROVIDER_UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                isFavourite = true;
                                favourites.setText("Remove");
                                favourites.setEnabled(true);
                            }
                            else{
                                isFavourite = false;
                                favourites.setText("Add");
                                favourites.setEnabled(true);
                            }
                        }
                    });
                }
                else{
                    favourites.setText("Wait...");
                    favourites.setEnabled(false);
                    final DatabaseReference fav_ref = FirebaseDatabase.getInstance().getReference().child("favourites/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + PROVIDER_UID);
                    fav_ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                isFavourite = false;
                                favourites.setText("Add");
                                favourites.setEnabled(true);
                            }
                            else{
                                isFavourite = true;
                                favourites.setText("Remove");
                                favourites.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });
        //
        get_n_setDetails();
        //set fav button
        set_fav_button();
        return v;
    }

    private void set_fav_button() {
        favourites.setText("Wait...");
        favourites.setEnabled(false);
        final DatabaseReference fav_ref = FirebaseDatabase.getInstance().getReference().child("favourites/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + PROVIDER_UID);
        fav_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.getValue() == null) {
                        favourites.setText("Add");
                        favourites.setEnabled(true);
                    }
                    else {
                        favourites.setText("Remove");
                        isFavourite = true;
                        favourites.setEnabled(true);
                    }
                }catch (Exception e){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void get_n_setDetails() {
        //while loading data hide consteain layout
        //make progress bar visible
        details_layout.setVisibility(View.GONE);
        loading_anim.setVisibility(View.VISIBLE);
        DatabaseReference details_ref = FirebaseDatabase.getInstance().getReference().child("providers/salons/" + PROVIDER_UID);
        details_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    int open_hours_int = Integer.parseInt(dataSnapshot.child("time/open_hour").getValue().toString());
                    int close_hour_int = Integer.parseInt(dataSnapshot.child("time/close_hour").getValue().toString());
                    int open_min_int = Integer.parseInt(dataSnapshot.child("time/open_min").getValue().toString());
                    int close_min_int = Integer.parseInt(dataSnapshot.child("time/close_min").getValue().toString());

                    //opening time
                    if(open_hours_int < 12){
                        if(open_min_int > 0){
                            open_hours.setText(Integer.toString(open_hours_int) + " : " + Integer.toString(open_min_int) + " AM");
                        }
                        else {
                            open_hours.setText(Integer.toString(open_hours_int) + " AM");
                        }
                    }
                    else{
                        if(open_min_int > 0){
                            open_hours.setText(Integer.toString(open_hours_int - 12) + " : " + Integer.toString(open_min_int) + " PM");
                        }
                        else {
                            open_hours.setText(Integer.toString(open_hours_int - 12) + " PM");
                        }
                    }

                    //close time
                    if(close_hour_int < 12){
                        if(close_min_int > 0){
                            closed_hours.setText(Integer.toString(close_hour_int) + " : " + Integer.toString(close_min_int) + " AM");
                        }
                        else {
                            closed_hours.setText(Integer.toString(close_hour_int) + " AM");
                        }
                    }
                    else{
                        if(close_min_int > 0){
                            closed_hours.setText(Integer.toString(close_hour_int - 12) + " : " + Integer.toString(close_min_int) + " PM");
                        }
                        else {
                            closed_hours.setText(Integer.toString(close_hour_int - 12) + " PM");
                        }
                    }
                    //setting values
                    address.setText(dataSnapshot.child("address").getValue().toString());
                    about.setText(dataSnapshot.child("about").getValue().toString());

                    //change visibilities
                    loading_anim.setVisibility(View.GONE);
                    details_layout.setVisibility(View.VISIBLE);

                }catch (Exception e){
                    //on exception change visibilities
                    loading_anim.setVisibility(View.GONE);
                    details_layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
