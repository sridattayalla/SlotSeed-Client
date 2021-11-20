package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sridatta.busyhunkproject.models.Offer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class maleSalonItemsFragment extends Fragment implements View.OnClickListener {
    View v;
    String LOCATION,GENDER,PROVIDER_UID;
    LayoutInflater layoutInflater;
    RecyclerView recyclerView;
    OffersAdapter adapter;
    LinkedHashMap<String,Offer> offer_hashmap=new LinkedHashMap<>();
    ConstraintLayout loading_layout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.offers_for_any_fragment,container,false);
        //layout inflater
        layoutInflater=LayoutInflater.from(getActivity());
        //getting gender
        GENDER=getResources().getString(R.string.male);
        //initiating ui
        loading_layout = v.findViewById(R.id.placeholder_layout);
        recyclerView=v.findViewById(R.id.container);recyclerView.setNestedScrollingEnabled(false);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(adapter);
        //setting adapter
        adapter=new OffersAdapter();
        recyclerView.setAdapter(adapter);

        //setting offers
        get_n_set_details();
        return v;
    }

    void get_n_set_details(){
        //getting location
        if(FirebaseAuth.getInstance().getCurrentUser()==null){getActivity().finish(); }
        DatabaseReference location_ref= FirebaseDatabase.getInstance().getReference().child("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+"/location");
        location_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.d("datta_text", "location is " + dataSnapshot.getValue().toString());
                    LOCATION = dataSnapshot.getValue().toString();
                }
                catch (Exception e){
                    Log.d("datta_text","error while getting location in male fragment "+e.toString());
                }
                //getting offers
                final DatabaseReference offers_ref=FirebaseDatabase.getInstance().getReference().child("add_images/salons/" + LOCATION);
                offers_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //clearing hashmap
                        offer_hashmap.clear();
                        for( final DataSnapshot provider_datasnapShot:dataSnapshot.getChildren()){
                            PROVIDER_UID=provider_datasnapShot.getKey().toString();
                            Log.d("datta_text", "uid is " + PROVIDER_UID);
                            try {
                                //getting name
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("providers/salons/" + provider_datasnapShot.getKey().toString() + "/name");
                                /*creating unnecessary copy to avoid making it provider data snapshot final*/
                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String name = dataSnapshot.getValue().toString();
                                        //inserting to hashmap
                                        if (provider_datasnapShot.hasChild(GENDER )) {
                                            for (DataSnapshot offer_dataSnapShot : provider_datasnapShot.child(GENDER).getChildren()) {

                                                offer_hashmap.put(offer_dataSnapShot.getKey().toString(), new Offer(provider_datasnapShot.getKey().toString(), name, offer_dataSnapShot.getValue().toString()));
                                                //notify addapter
                                                adapter.notifyDataSetChanged();
                                                //visibility setting
                                                loading_layout.setVisibility(View.GONE);
                                               /* if(offer_dataSnapShot.child("offer_type").getValue().toString().equals("FTO")){
                                                    if(!offer_dataSnapShot.hasChild("used_by/"+FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                        try {
                                                            offer_hashmap.put(offer_dataSnapShot.getKey().toString(), new Offer(provider_datasnapShot.getKey().toString()
                                                                    , name
                                                                    , offer_dataSnapShot.child("image_url").getValue().toString()));
                                                            Log.d("datta_text", "uid is " + provider_datasnapShot.getKey().toString());
                                                            Log.d("datta_text", "name is " + name);
                                                            Log.d("datta_text", "code is " + offer_dataSnapShot.getKey().toString());
                                                            Log.d("datta_text", "url is " + offer_dataSnapShot.child("image_url").getValue().toString());
                                                            //notify addapter
                                                            adapter.notifyDataSetChanged();
                                                            //visibility setting
                                                            loading_layout.setVisibility(View.INVISIBLE);
                                                            internet_interuption_layout.setVisibility(View.GONE);
                                                        } catch (NullPointerException e) {
                                                            Log.d("datta_text", "exception at inserting offer " + e.toString());
                                                        }
                                                    }
                                                }
                                                else {
                                                    try {
                                                        offer_hashmap.put(offer_dataSnapShot.getKey().toString(), new Offer(provider_datasnapShot.getKey().toString()
                                                                , name
                                                                , offer_dataSnapShot.child("image_url").getValue().toString()));
                                                        Log.d("datta_text", "uid is " + provider_datasnapShot.getKey().toString());
                                                        Log.d("datta_text", "name is " + name);
                                                        Log.d("datta_text", "code is " + offer_dataSnapShot.getKey().toString());
                                                        Log.d("datta_text", "url is " + offer_dataSnapShot.child("image_url").getValue().toString());
                                                        //notify addapter
                                                        adapter.notifyDataSetChanged();
                                                        //visibility setting
                                                        loading_layout.setVisibility(View.INVISIBLE);
                                                        internet_interuption_layout.setVisibility(View.GONE);
                                                    } catch (NullPointerException e) {
                                                        Log.d("datta_text", "exception at inserting offer " + e.toString());
                                                    }
                                                }*/
//                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            catch (Exception e){
                                Log.d("datta_text","exception occured at getting male offer details "+e.toString());
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.hair:Intent hair_intent=new Intent(getActivity(),ProvidersActivity.class);
                hair_intent.putExtra("category",getResources().getString(R.string.hair));
                hair_intent.putExtra("gender",getResources().getString(R.string.male));
                startActivity(hair_intent);break;
            case R.id.skin:Intent skin_intent=new Intent(getActivity(),ProvidersActivity.class);
                skin_intent.putExtra("category",getResources().getString(R.string.skin_care));
                skin_intent.putExtra("gender",getResources().getString(R.string.male));
                startActivity(skin_intent);break;
            case R.id.spa:Intent spa_intent=new Intent(getActivity(),ProvidersActivity.class);
                spa_intent.putExtra("category",getResources().getString(R.string.spa));
                spa_intent.putExtra("gender",getResources().getString(R.string.male));
                startActivity(spa_intent);break;
            case R.id.all:Intent all_intent=new Intent(getActivity(),ProvidersActivity.class);
                all_intent.putExtra("gender",getResources().getString(R.string.male));
                startActivity(all_intent);break;
        }
    }

    class OffersAdapter extends RecyclerView.Adapter<OfferHolder>{
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
            final TextView name_textview=v.findViewById(R.id.name);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent salon_services_intent=new Intent(getActivity(),ProviderActivity.class);
                    salon_services_intent.putExtra("uid",v.getTag().toString());
                    salon_services_intent.putExtra("name",name_textview.getText().toString());
                    salon_services_intent.putExtra("gender",GENDER);
                    startActivity(salon_services_intent);
                }
            });
        }


        void setDetails(final Offer offer){
           final ImageView imageView=v.findViewById(R.id.offer_image);
           TextView name_textview=v.findViewById(R.id.name);

            //setting image
            try {
                final Picasso picasso = Picasso.with(getActivity());
                picasso.setIndicatorsEnabled(false);
                picasso.with(getActivity()).load(Uri.parse(offer.getUrl()))
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.promo_empty_background).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        picasso.with(getActivity()).load(Uri.parse(offer.getUrl())).placeholder(R.drawable.promo_empty_background).into(imageView);
                    }
                });
            }
            catch (Exception e){
                Log.d("datta_text","exception in male salon fragment while getting image "+e.toString());
            }

            //setting details

            Log.d("datta_text",offer.getProvider_uid());
            name_textview.setText(offer.getProvider_name());
            v.setTag(offer.getProvider_uid());
        }
    }



    class SpacesItemDecorator extends RecyclerView.ItemDecoration{
        private int space;

        public SpacesItemDecorator(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left=space;
            outRect.right=space;
            outRect.bottom=4;
            outRect.top=4;

        }
    }

}
