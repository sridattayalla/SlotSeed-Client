package com.sridatta.busyhunkproject.provider_details;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sridatta.busyhunkproject.R;
import com.sridatta.busyhunkproject.SalonServicesActivity;

import net.igenius.customcheckbox.CustomCheckBox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Services extends Fragment {
    View v;
    LayoutInflater layoutInflater;
    String PROVIDER_UID,GENDER,NAME, REFFERAL_REWARD_CATEGORY;
    Button proceed;
    LinkedHashMap<String,Object> serviceList=new LinkedHashMap<>();
    parentAdapter parent_adapter;
    // offerAdapter offer_adapter;
    RecyclerView recyclerView;
    LottieAnimationView loading_anim;

    public Services getInstance(String provider_uid,String gender,String name, String refferal_reward_category){
        PROVIDER_UID=provider_uid;
        GENDER=gender;
        NAME=name;
        REFFERAL_REWARD_CATEGORY = refferal_reward_category;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_services,container,false);
        //inflater
        layoutInflater = LayoutInflater.from(getActivity());
        //initiating ui
        loading_anim = v.findViewById(R.id.progressBar5); loading_anim.playAnimation();
        proceed=v.findViewById(R.id.button5);
        recyclerView = v.findViewById(R.id.service_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //setting adapter
        parent_adapter=new parentAdapter();
        recyclerView.setAdapter(parent_adapter);


        //proceed onclick
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent salon_services_intent=new Intent(getActivity(),SalonServicesActivity.class);
                salon_services_intent.putExtra("uid",PROVIDER_UID);
                salon_services_intent.putExtra("gender",GENDER);
                salon_services_intent.putExtra("name",NAME);
                salon_services_intent.putExtra("referral_reward_category", REFFERAL_REWARD_CATEGORY);
                startActivity(salon_services_intent);
                getActivity().finish();
            }
        });
        //
        get_n_set_details();
        return v;
    }

    void get_n_set_details(){
        DatabaseReference locationRef= FirebaseDatabase.getInstance().getReference().child("providers/salons/"+PROVIDER_UID+"/location");
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String location=dataSnapshot.getValue().toString();
                /*getting all services along with offers*/
                final DatabaseReference services_n_offers_ref=FirebaseDatabase.getInstance().getReference().child("services/salons/"+location+"/"+PROVIDER_UID+"/"+GENDER);
                services_n_offers_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot categorySnapShot:dataSnapshot.getChildren()){
                            String curr_category=categorySnapShot.getKey().toString();
                            int count = 0;
                            for(DataSnapshot subcatDatasnapshot:categorySnapShot.getChildren()){
                                count = count + (int)subcatDatasnapshot.getChildrenCount();
                            }
                            Log.d("datta_text","category is "+curr_category);
                            //category list
                            serviceList.put(curr_category, new category(curr_category, Integer.toString(count)));
                            parent_adapter.notifyDataSetChanged();
                            loading_anim.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText( getActivity(),"Problem occured :(", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText( getActivity(),"Problem occured :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class parentAdapter extends RecyclerView.Adapter<parentViewHolder> implements Serializable {
        View v;
        @Override
        public parentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v=layoutInflater.inflate(R.layout.service_parent_for_fragment,parent,false);
            return new parentViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final parentViewHolder holder, final int position) {
            ArrayList<String> keys = new ArrayList<>(serviceList.keySet());
            holder.setDetails((category) serviceList.get(keys.get(position)));
        }

        @Override
        public int getItemCount() {
            return serviceList.size();
        }
    }

    class parentViewHolder extends RecyclerView.ViewHolder implements Serializable{
        View v;
        public parentViewHolder(final View itemView) {
            super(itemView);
            v=itemView;

            //onClick
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent salon_services_intent=new Intent(getActivity(),SalonServicesActivity.class);
                    salon_services_intent.putExtra("uid",PROVIDER_UID);
                    salon_services_intent.putExtra("gender",GENDER);
                    salon_services_intent.putExtra("name",NAME);
                    salon_services_intent.putExtra("referral_reward_category", REFFERAL_REWARD_CATEGORY);
                    startActivity(salon_services_intent);
                    getActivity().finish();
                }
            });
        }

        void setDetails(category c){
            TextView title_textView=v.findViewById(R.id.title_text);
            title_textView.setText(c.getCategory_name());
            //count
            TextView serviceCount = v.findViewById(R.id.count);
            serviceCount.setText(c.getCount());
            //service in
            TextView service_in = v.findViewById(R.id.textView61);
            if(Integer.parseInt(c.getCount()) == 1){
                service_in.setText("service in");
            }

        }
    }

    static class category {
        private String category_name, count;

        public category(String category_name, String count) {
            this.category_name = category_name;
            this.count = count;
        }

        public String getCategory_name() {
            return category_name;
        }

        public String getCount() {
            return count;
        }
    }

}
