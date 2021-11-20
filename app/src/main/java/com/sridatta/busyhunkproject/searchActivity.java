package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sridatta.busyhunkproject.search.SearchPAgerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class searchActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText search_text;
    ImageView clear_text;
    CardView search_card;
    RecyclerView recyclerView;
    seachResultAdapter adapter = new seachResultAdapter();
    HashMap<String, searchResult> searchResultHashmap = new HashMap<>();
    LayoutInflater layoutInflater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //inflater
        layoutInflater = LayoutInflater.from(getApplicationContext());
        //initiating ui
        search_text=findViewById(R.id.search);
        clear_text=findViewById(R.id.clear);
        search_card=findViewById(R.id.search_card);
        toolbar=findViewById(R.id.toolbar3); setSupportActionBar(toolbar); getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.search_result_container);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);

        //search listener
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()>2){
                    if(search_card.getCardElevation()==5){
                        search_card.setCardElevation(10);
                        get_n_setResults(charSequence.toString().toLowerCase());
                    }
                    clear_text.setVisibility(View.VISIBLE); clear_text.setEnabled(true);}
                else{
                    searchResultHashmap.clear();
                    adapter.notifyDataSetChanged();
                    search_card.setCardElevation(5);
                    clear_text.setVisibility(View.INVISIBLE); clear_text.setEnabled(false);}
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //clear
        clear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_text.setText(null);
            }
        });

    }

    void get_n_setResults(final String search_query){
        searchResultHashmap.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("providers/salons");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(final DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    try{
                        if(dataSnapshot1.child("lower_case_name").getValue().toString().contains(search_query)){
                            DatabaseReference gender_ref = FirebaseDatabase.getInstance().getReference().child("services/salons/"+dataSnapshot1.child("location").getValue().toString()+"/"+dataSnapshot1.getKey());
                            gender_ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d("datta_text", dataSnapshot.toString());
                                    for(DataSnapshot gender_ds:dataSnapshot.getChildren()){
                                        searchResultHashmap.put(dataSnapshot1.getKey()+gender_ds.getKey(), new searchResult(dataSnapshot1.child("name").getValue().toString(), gender_ds.getKey(),
                                                dataSnapshot1.getKey(), dataSnapshot1.child("location").getValue().toString()));
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                    catch (Exception e){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Some problem occured :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    private void LogOut(){
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("IS_REGISTERED",0);
        editor.commit();
        startActivity(new Intent(searchActivity.this, Login_Register.class));
        finish();
    }

    private void getSearchResults(){

    }

    class seachResultAdapter extends RecyclerView.Adapter<searchResultViewHolder>{
        View v;
        @Override
        public searchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v = layoutInflater.inflate(R.layout.search_result_single, parent, false);
            return new searchResultViewHolder(v);
        }

        @Override
        public void onBindViewHolder(searchResultViewHolder holder, int position) {
            ArrayList<String> keys = new ArrayList<>(searchResultHashmap.keySet());
            holder.setDetails(searchResultHashmap.get(keys.get(position)));
        }

        @Override
        public int getItemCount() {
            return searchResultHashmap.size();
        }
    }

    class searchResultViewHolder extends RecyclerView.ViewHolder{
        View v;
        public searchResultViewHolder(View itemView) {
            super(itemView);
            v = itemView;
            final TextView salon_name_textview = v.findViewById(R.id.salon_name);
            final TextView salon_gender = v.findViewById(R.id.gender);
            //onClicl
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent salon_services_intent=new Intent(searchActivity.this ,ProviderActivity.class);
                    salon_services_intent.putExtra("uid",v.getTag().toString());
                    salon_services_intent.putExtra("name",salon_name_textview.getText().toString());
                    salon_services_intent.putExtra("gender", salon_gender.getText().toString());
                    startActivity(salon_services_intent);
                }
            });
        }

        public void setDetails(searchResult searchResult) {
            TextView salon_name_textview = v.findViewById(R.id.salon_name);
            TextView gender_textView = v.findViewById(R.id.gender);
            TextView location_textview = v.findViewById(R.id.provider_location);
            //
            v.setTag(searchResult.getSalon_uid());
            salon_name_textview.setText(searchResult.getSalon_name());
            gender_textView.setText(searchResult.getGender());
            location_textview.setText(searchResult.getSalon_location());
        }
    }

    class searchResult{
        private String salon_name, gender,  salon_uid, salon_location;

        public searchResult(String salon_name, String gender, String salon_uid, String salon_location) {
            this.salon_name = salon_name;
            this.gender = gender;
            this.salon_uid = salon_uid;
            this.salon_location = salon_location;
        }

        public String getSalon_name() {
            return salon_name;
        }

        public String getGender() {
            return gender;
        }

        public String getSalon_uid() {
            return salon_uid;
        }

        public String getSalon_location() {
            return salon_location;
        }
    }
}
