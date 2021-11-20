package com.sridatta.busyhunkproject.AppRecyclerViews;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sridatta.busyhunkproject.ProviderActivity;
import com.sridatta.busyhunkproject.R;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class OffersForHer {
    Activity activity;
    View v;
    String LOCATION,GENDER;
    LayoutInflater layoutInflater;
    RecyclerView recyclerView;
    ConstraintLayout internet_interuption_layout,loading_layout;
    private Button retry;
    LottieAnimationView animationView;
    OffersAdapter adapter;
    LinkedHashMap<String,Offer> offer_hashmap=new LinkedHashMap<>();
    ProgressDialog pd;
    boolean IS_FIRST_CHECK_OVER;
    int retry_count;
    private ImageView hair_care,skin_care,spa,all;

    public OffersForHer(Activity activity){
        this.activity = activity;
    }



    public class OffersAdapter extends RecyclerView.Adapter<OfferHolder>{
        View v;
        @Override
        public OfferHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v=layoutInflater.inflate(R.layout.offer_single_layout,parent,false);
            OfferHolder holder=new OfferHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(OfferHolder holder, int position) {
            List<String> keyList=new ArrayList<>(offer_hashmap.keySet());
            holder.setDetails(offer_hashmap.get(keyList.get(position)));
        }

        @Override
        public int getItemCount() {
            return offer_hashmap.size();
        }
    }

    class OfferHolder extends RecyclerView.ViewHolder{
        View v;
        public OfferHolder(View itemView) {
            super(itemView);
            v=itemView;
            //onClick
            final TextView offer_name_textView=v.findViewById(R.id.name);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView uid_test=v.findViewById(R.id.name);
                    Log.d("datta_text","uid is "+uid_test.getText().toString());
                    Intent salon_services_intent=new Intent(activity, ProviderActivity.class);
                    salon_services_intent.putExtra("uid",v.getTag().toString());
                    salon_services_intent.putExtra("name",offer_name_textView.getText().toString());
                    salon_services_intent.putExtra("gender",GENDER);
                    activity.startActivity(salon_services_intent);
                }
            });
        }

        void setDetails(final Offer offer){
            final ImageView imageView=v.findViewById(R.id.offer_image);
            TextView offer_name_textView=v.findViewById(R.id.name);

            //setting image
            try {
                final Picasso picasso = Picasso.with(activity);
                picasso.setIndicatorsEnabled(false);
                picasso.load(Uri.parse(offer.getUrl()))
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.empty_background).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        picasso.with(activity).load(Uri.parse(offer.getUrl())).fit().placeholder(R.drawable.empty_background).into(imageView);
                    }
                });
            }
            catch (Exception e){
                Log.d("datta_text","exception in female salon fragment while getting image "+e.toString());
            }

            //setting details
            offer_name_textView.setText(offer.getProvider_name());
            v.setTag(offer.getProvider_uid());
        }
    }

    public class Offer{
        String provider_uid,provider_name,url;


        public Offer(String provider_uid, String provider_name, String url) {
            this.provider_uid = provider_uid;
            this.provider_name = provider_name;
            this.url = url;
        }

        public String getProvider_uid() {
            return provider_uid;
        }

        public void setProvider_uid(String offer_code) {
            this.provider_uid = provider_uid;
        }

        public String getProvider_name() {
            return provider_name;
        }

        public void setProvider_name(String provider_name) {
            this.provider_name = provider_name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
