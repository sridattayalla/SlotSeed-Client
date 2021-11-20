package com.sridatta.busyhunkproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sridatta.busyhunkproject.models.Promocode;

import java.util.ArrayList;
import java.util.HashMap;


public class PromocodeAdFragment extends Fragment {
    View v;
    String GENDER;
    LayoutInflater layoutInflater;
    HashMap<String,Promocode> promocode_hashmap=new HashMap<>();
    PromocodeAdapter adapter=new PromocodeAdapter();
    RecyclerView recyclerView;
    ConstraintLayout placeholder_layout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_promocode_ad, container, false);
        // inflater
        layoutInflater=LayoutInflater.from(getActivity());
        GENDER=getArguments().getString("gender");
        if(GENDER!=null){
            get_n_set_details();
        }

        //initiating ui
        placeholder_layout = v.findViewById(R.id.placeholder_layout);
        recyclerView=v.findViewById(R.id.promocode_recyclerview);recyclerView.setNestedScrollingEnabled(false);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(adapter);

        return v;
    }

    private void get_n_set_details() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("add_images/promo_codes");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    final DatabaseReference promocode_ref=FirebaseDatabase.getInstance().getReference().child("price_cutters/salon_spa/"+dataSnapshot1.getKey().toString());
                    promocode_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                placeholder_layout.setVisibility(View.GONE);
                                String Caterogy = dataSnapshot.child("category").getValue().toString();
                                promocode_hashmap.put(dataSnapshot1.getKey().toString(), new Promocode(dataSnapshot1.getKey().toString(), dataSnapshot1.getValue().toString(), Caterogy));
                                adapter.notifyDataSetChanged();
                            }catch (Exception e){}
                            /*try {
                                String oppositeGender = getResources().getString(R.string.male);
                                if (GENDER.equals(getResources().getString(R.string.male))) {
                                    oppositeGender = getResources().getString(R.string.female);
                                }
                                if (!dataSnapshot.child("gender").getValue().toString().equals(oppositeGender)) {
                                    String Caterogy = dataSnapshot.child("category").getValue().toString();
                                    if(Caterogy.equals(getActivity().getResources().getString(R.string.no))){
                                        Caterogy = null;
                                    }
                                    promocode_hashmap.put(dataSnapshot1.getKey().toString(), new Promocode(dataSnapshot1.getKey().toString(), dataSnapshot1.getValue().toString(), Caterogy));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            catch (Exception e){
                                Log.d("datta_text","error at adding promocode to hashmap: "+e.toString());
                            }*/
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("datta_text","unable to check promocode images: "+databaseError.toString());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text","unable to get promocode images: "+databaseError.toString());
            }
        });
    }

    class PromocodeAdapter extends RecyclerView.Adapter<PromocodeViewHolder>{
        View v;
        @Override
        public PromocodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v=layoutInflater.inflate(R.layout.single_promocode_ad,parent,false);
            return new PromocodeViewHolder(v);
        }

        @Override
        public void onBindViewHolder(PromocodeViewHolder holder, int position) {
            ArrayList<String> keys=new ArrayList<>(promocode_hashmap.keySet());
            holder.setPromocode(promocode_hashmap.get(keys.get(position)));
        }

        @Override
        public int getItemCount() {
            return promocode_hashmap.size();
        }
    }

    class PromocodeViewHolder extends RecyclerView.ViewHolder{
        View v;
        public PromocodeViewHolder(View itemView) {
            super(itemView);
            v=itemView;

            //onClick
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String category = promocode_hashmap.get(v.getTag().toString()).getCategory();
                    if(category!=null && category.length() >0){
                        Intent intent=new Intent(getActivity(),ProvidersActivity.class);
                        intent.putExtra("category",category);
                        intent.putExtra("gender",GENDER);
                        startActivity(intent);
                    }
                    else{
                        Intent intent=new Intent(getActivity(),ProvidersActivity.class);
                        intent.putExtra("gender",GENDER);
                        startActivity(intent);
                    }
                }
            });
        }

        public void setPromocode(final Promocode promocode) {
            final ImageView imageView=v.findViewById(R.id.promo_image);

            //setting tag
            v.setTag(promocode.getPromocode_name());

            //setting image
            try {
                final Picasso picasso = Picasso.with(getActivity());
                picasso.setIndicatorsEnabled(false);
                picasso.load(Uri.parse(promocode.getUrl()))
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.promo_empty_background).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        picasso.with(getActivity()).load(Uri.parse(promocode.getUrl())).placeholder(R.drawable.promo_empty_background).into(imageView);
                    }
                });
            }
            catch (Exception e){
                Log.d("datta_text","exception in male salon fragment while getting image "+e.toString());
            }
        }
    }


}
