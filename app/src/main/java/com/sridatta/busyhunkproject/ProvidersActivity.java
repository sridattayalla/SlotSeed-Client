package com.sridatta.busyhunkproject;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class ProvidersActivity extends AppCompatActivity implements ProviderBottomSheetDialog.category_interface {
    Toolbar toolbar;
    String SELECTED_CATEGORY,LOCATION,GENDER, REFFERAL_REWARD_CATEGORY;
    HashMap<String, String> selected_categories = new HashMap<>();
    HashMap<String, Provider> providerHashMap = new HashMap<>();
    LottieAnimationView loading_anim;
    RecyclerView recyclerView;
    LayoutInflater layoutInflater;
    ProviderAdapter adapter = new ProviderAdapter();
    TextView gender_in_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_providers);
        //intent
        SELECTED_CATEGORY=getIntent().getStringExtra("category");
        GENDER=getIntent().getStringExtra("gender");
        REFFERAL_REWARD_CATEGORY = getIntent().getStringExtra("referral_reward_category");
        //inflater
        layoutInflater = LayoutInflater.from(getApplicationContext());
        //initiating ui
        gender_in_title = findViewById(R.id.gender_title); gender_in_title.setText(GENDER);
        loading_anim = findViewById(R.id.progressbar); loading_anim.playAnimation();
        recyclerView = findViewById(R.id.container);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        toolbar = findViewById(R.id.toolbar9);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        //
        if(SELECTED_CATEGORY!=null){
            selected_categories.put(SELECTED_CATEGORY, SELECTED_CATEGORY);
        }
        //
        get_location_n_set_provider_details();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.providers_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.options :
            ProviderBottomSheetDialog dialog = new ProviderBottomSheetDialog();
            Bundle args = new Bundle();
            args.putSerializable("categories", selected_categories);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "bottom_dialog");
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void get_location_n_set_provider_details() {
        providerHashMap.clear();
        loading_anim.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        DatabaseReference location_ref= FirebaseDatabase.getInstance().getReference().child("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+"/location");
        location_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    LOCATION=dataSnapshot.getValue().toString();
                    if(LOCATION!=null){
                        get_providers();
                    }
                }
                catch (Exception e){
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void get_providers() {
        DatabaseReference services_ref = FirebaseDatabase.getInstance().getReference().child("services/salons/" + LOCATION);
        services_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    if(has_category(dataSnapshot1.child(GENDER), 0, new ArrayList<>(selected_categories.keySet()))){
                        Log.d("datta_text", "has_category in:" + dataSnapshot1.getKey().toString());
                        Query providers_query = FirebaseDatabase.getInstance().getReference().child("providers/salons/"+dataSnapshot1.getKey().toString()).orderByChild("name");
                        DatabaseReference provider_ref = FirebaseDatabase.getInstance().getReference().child("providers/salons/"+dataSnapshot1.getKey().toString());
                        providers_query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try{
                                    providerHashMap.put(dataSnapshot.getKey().toString() ,
                                                               new Provider(dataSnapshot.getKey().toString(),
                                                                            dataSnapshot.child("name").getValue().toString(),
                                                                            dataSnapshot.child("address").getValue().toString(),
                                                                            dataSnapshot.child("image_urls/url0").getValue().toString()));
                                    //notify
                                    adapter.notifyDataSetChanged();
                                    recyclerView.setVisibility(View.VISIBLE);
                                    loading_anim.setVisibility(View.GONE);
                                }
                                catch (NullPointerException e){

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Something went wrong :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean has_category(DataSnapshot dataSnapshot, int position, ArrayList keys){
        if(dataSnapshot.getValue() == null){return false;}
        if(position >= selected_categories.size()){
            return true;
        }else {
            if(dataSnapshot.hasChild(selected_categories.get(keys.get(position)))) {
                Log.d("datta_text", selected_categories.get(keys.get(position)));
                return has_category(dataSnapshot, position + 1, keys);
            }
            else {
                return false;
            }
        }
    }

    @Override
    public void getSelectedCategories(HashMap hashMap) {
        selected_categories = hashMap;
        get_location_n_set_provider_details();
    }

    class ProviderAdapter extends RecyclerView.Adapter<ProviderViewHolder>{
        View v;
        @Override
        public ProviderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v = layoutInflater.inflate(R.layout.single_provider, parent, false);
            return new ProviderViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ProviderViewHolder holder, int position) {
            ArrayList<String> keys = new ArrayList<>(providerHashMap.keySet());
            holder.setDetails(providerHashMap.get(keys.get(position)));
        }

        @Override
        public int getItemCount() {
            return providerHashMap.size();
        }
    }

    class ProviderViewHolder extends RecyclerView.ViewHolder{
        View v;
        public ProviderViewHolder(View itemView) {
            super(itemView);
            v = itemView;
            final ImageView imageView = v.findViewById(R.id.salon_logo);

            //imageview onClick
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView name_textview=v.findViewById(R.id.name);
                    Log.d("datta_text","uid is "+v.getTag().toString());
                    Intent salon_services_intent=new Intent(ProvidersActivity.this ,ProviderActivity.class);
                    salon_services_intent.putExtra("uid",v.getTag().toString());
                    salon_services_intent.putExtra("name",name_textview.getText().toString());
                    salon_services_intent.putExtra("gender",GENDER);
                    salon_services_intent.putExtra("referral_reward_category", REFFERAL_REWARD_CATEGORY);
                    Pair[] pair1=new Pair[1];
                    pair1[0]=new Pair(imageView,"image");
                    ActivityOptions options1=ActivityOptions.makeSceneTransitionAnimation(ProvidersActivity.this,pair1);
                    startActivity(salon_services_intent,options1.toBundle());
                }
            });

            //details onclick
            Button details = v.findViewById(R.id.details);
            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView name_textview=v.findViewById(R.id.name);
                    Log.d("datta_text","uid is "+v.getTag().toString());
                    Intent salon_services_intent=new Intent(ProvidersActivity.this ,ProviderActivity.class);
                    salon_services_intent.putExtra("uid",v.getTag().toString());
                    salon_services_intent.putExtra("name",name_textview.getText().toString());
                    salon_services_intent.putExtra("gender",GENDER);
                    salon_services_intent.putExtra("referral_reward_category", REFFERAL_REWARD_CATEGORY);
                    Pair[] pair1=new Pair[1];
                    pair1[0]=new Pair(imageView,"image");
                    ActivityOptions options1=ActivityOptions.makeSceneTransitionAnimation(ProvidersActivity.this,pair1);
                    startActivity(salon_services_intent,options1.toBundle());
                }
            });

            //services onclick
            Button services = v.findViewById(R.id.services);
            services.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView name_text=v.findViewById(R.id.name);
                    Intent salon_services_intent=new Intent(ProvidersActivity.this,SalonServicesActivity.class);
                    salon_services_intent.putExtra("uid",v.getTag().toString());
                    salon_services_intent.putExtra("gender",GENDER);
                    salon_services_intent.putExtra("referral_reward_category", REFFERAL_REWARD_CATEGORY);
                    salon_services_intent.putExtra("name",name_text.getText().toString());
                    startActivity(salon_services_intent);
                }
            });
        }

        public void setDetails(final Provider provider) {
            v.setTag(provider.provider_uid);

            TextView name_textview = v.findViewById(R.id.name);
            name_textview.setText(provider.getName());

            TextView address_textview = v.findViewById(R.id.address);
            address_textview.setText(provider.getAddress());

            final ImageView imageView = v.findViewById(R.id.salon_logo);
            final Picasso picasso = Picasso.with(getApplicationContext());
            picasso.setIndicatorsEnabled(false);
            picasso.load(Uri.parse(provider.getImage_url())).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.promo_empty_background).into(imageView, new Callback() {
                @Override
                public void onSuccess() { }
                @Override
                public void onError() {
                    picasso.with(getApplicationContext()).load(Uri.parse(provider.getImage_url())).placeholder(R.drawable.promo_empty_background).into(imageView);
                }
            });

        }
    }

    class Provider{
        String provider_uid, name, address, image_url;

        public Provider(String provider_uid, String name, String address, String image_url) {
            this.provider_uid = provider_uid;
            this.name = name;
            this.address = address;
            this.image_url = image_url;
        }

        public String getProvider_uid() {
            return provider_uid;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getImage_url() {
            return image_url;
        }
    }

}
