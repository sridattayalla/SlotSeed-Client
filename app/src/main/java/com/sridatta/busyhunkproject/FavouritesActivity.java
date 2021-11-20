package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.sridatta.busyhunkproject.models.favourite;

import java.util.ArrayList;
import java.util.HashMap;

public class FavouritesActivity extends AppCompatActivity {
    Toolbar toolbar;
    String RECYCLERVIEW = "recyclerview", EMPTY = "empty", GENDER;
    ConstraintLayout empty_layout, fav_layout;
    LottieAnimationView lottieAnimationView;
    RecyclerView recyclerView;
    HashMap<String, favourite> favouriteHashmap = new HashMap<>();
    LayoutInflater layoutInflater;
    FavAdapter adapter = new FavAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        //layoutInflater
        layoutInflater = LayoutInflater.from(this);
        //initiating ui
        empty_layout = findViewById(R.id.empty);
        fav_layout = findViewById(R.id.constraintLayout2);
        lottieAnimationView = findViewById(R.id.progressBar8); lottieAnimationView.playAnimation();
        toolbar=findViewById(R.id.toolbar11);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        //
        getGender();
        //
        get_n_setDetails();
    }

    private void getGender() {
        DatabaseReference genderRef = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        genderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    GENDER = dataSnapshot.child("gender").getValue().toString();
                }catch (Exception e){
                    Log.d("datta_text", "Something went wrong in getting gender: " + e.toString());
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                finish();
                Toast.makeText(getApplicationContext(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void get_n_setDetails() {
        DatabaseReference fav_ref = FirebaseDatabase.getInstance().getReference().child("favourites/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        fav_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.toString() == null){

                }
                //clear hashmap before entering favourite salons
                else{
                    favouriteHashmap.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        try{
                            final String provider_uid = dataSnapshot1.getKey().toString();
                            DatabaseReference provider_ref = FirebaseDatabase.getInstance().getReference().child("providers/salons/" + provider_uid);
                            provider_ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    try {
                                        String address = dataSnapshot.child("address").getValue().toString();
                                        String name = dataSnapshot.child("name").getValue().toString();
                                        favouriteHashmap.put(provider_uid, new favourite(name, address, provider_uid));
                                        adapter.notifyDataSetChanged();
                                        lottieAnimationView.setVisibility(View.GONE);
                                        setLayoutVisibility(RECYCLERVIEW);
                                    }catch (Exception e){}
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("datta_text", "error in getting provider details: " +databaseError.toString());
                                    lottieAnimationView.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }catch (Exception e){
                            //treating that favs are 0 and making visibility gone to recycler view
                            setLayoutVisibility(EMPTY);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("datta_text", "error in getting favourites: " +databaseError.toString());
            }
        });
    }

    class FavAdapter extends RecyclerView.Adapter<FavViewHolder>{
        View v;
        @Override
        public FavViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v = layoutInflater.inflate(R.layout.favourite_salon_single, parent, false);
            return new FavViewHolder(v);
        }

        @Override
        public void onBindViewHolder(FavViewHolder holder, int position) {
            ArrayList<String> keys = new ArrayList<>(favouriteHashmap.keySet());
            holder.setDetails(favouriteHashmap.get(keys.get(position)));
        }

        @Override
        public int getItemCount() {
            return favouriteHashmap.size();
        }
    }

    class FavViewHolder extends RecyclerView.ViewHolder{
        View v;
        public FavViewHolder(View itemView) {
            super(itemView);
            v = itemView;
            final Button remove = v.findViewById(R.id.remove);
            Button details = v.findViewById(R.id.details);
            Button services = v.findViewById(R.id.service);

            //remove onClick
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    loadingDialogFragment.show(ft, "dialog");

                    remove.setEnabled(false);
                    final DatabaseReference fav_ref = FirebaseDatabase.getInstance().getReference().child("favourites/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + v.getTag().toString());
                    fav_ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                remove.setText("remove");
                                Toast.makeText(getApplicationContext(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                loadingDialogFragment.dismissFragment();
                                get_n_setDetails();
                            }
                        }
                    });
                }
            });

            //details onClick
            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //if gender is null then set defalult as male
                    if(GENDER != null && GENDER.length() > 0) {
                        TextView name_textview = v.findViewById(R.id.name);
                        Log.d("datta_text", "uid is " + v.getTag().toString());
                        Intent salon_services_intent = new Intent(FavouritesActivity.this, ProviderActivity.class);
                        salon_services_intent.putExtra("uid", v.getTag().toString());
                        salon_services_intent.putExtra("name", name_textview.getText().toString());
                        salon_services_intent.putExtra("gender", GENDER);
                        startActivity(salon_services_intent);
                    }
                    else{
                        TextView name_textview = v.findViewById(R.id.name);
                        Log.d("datta_text", "uid is " + v.getTag().toString());
                        Intent salon_services_intent = new Intent(FavouritesActivity.this, ProviderActivity.class);
                        salon_services_intent.putExtra("uid", v.getTag().toString());
                        salon_services_intent.putExtra("name", name_textview.getText().toString());
                        salon_services_intent.putExtra("gender", getResources().getString(R.string.male));
                        startActivity(salon_services_intent);
                    }
                }
            });

            //services onClick
            services.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(GENDER != null && GENDER.length() > 0) {
                        TextView name_text = v.findViewById(R.id.name);
                        Intent salon_services_intent = new Intent(FavouritesActivity.this, SalonServicesActivity.class);
                        salon_services_intent.putExtra("uid", v.getTag().toString());
                        salon_services_intent.putExtra("gender", GENDER);
                        salon_services_intent.putExtra("name", name_text.getText().toString());
                        startActivity(salon_services_intent);
                    }
                    else {
                        TextView name_text = v.findViewById(R.id.name);
                        Intent salon_services_intent = new Intent(FavouritesActivity.this, SalonServicesActivity.class);
                        salon_services_intent.putExtra("uid", v.getTag().toString());
                        salon_services_intent.putExtra("gender", getResources().getString(R.string.male));
                        salon_services_intent.putExtra("name", name_text.getText().toString());
                        startActivity(salon_services_intent);
                    }
                }
            });
        }

        void setDetails(favourite favourite){
            TextView name = v.findViewById(R.id.name);
            TextView address = v.findViewById(R.id.address);

            address.setText(favourite.getAddress());
            name.setText(favourite.getName());
            v.setTag(favourite.getUid());
        }
    }

    void setLayoutVisibility(String layout){
        if(layout.equals(EMPTY)){
            empty_layout.setVisibility(View.VISIBLE);
            fav_layout.setVisibility(View.GONE);
        }
        else{
            empty_layout.setVisibility(View.GONE);
            fav_layout.setVisibility(View.VISIBLE);
        }
    }
}
