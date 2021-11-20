package com.sridatta.busyhunkproject;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sridatta.busyhunkproject.models.PriceCutter;

import java.util.ArrayList;
import java.util.HashMap;

public class PromocodesFragment extends android.app.DialogFragment {
    View v;
    RecyclerView recyclerView;
    String GENDER;
    HashMap<String,SalonServicesActivity.service> services_hashmap = new HashMap<>();
    int total_charge;
    LayoutInflater layoutInflater;
    HashMap<String, PriceCutter> promoCodeHashMap = new HashMap<>();
    promocodeInterface mpromocodeInterface;
    Promocode_adapter adapter = new Promocode_adapter();
    String selected_promocode = null;
    LottieAnimationView loading;

    static PromocodesFragment newInstance(){
        return new PromocodesFragment();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_promocodes,container,false);
        //layout inflater
        layoutInflater=inflater;
        //
        GENDER = getArguments().getString("gender");
        services_hashmap = (HashMap<String, SalonServicesActivity.service>) getArguments().getSerializable("services");
        total_charge = getArguments().getInt("t_charge");
        //initiating ui
        loading = v.findViewById(R.id.promo_progress);loading.playAnimation();
        recyclerView=v.findViewById(R.id.promocodes_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        //
        get_n_set_promocodes();
        return v;
    }

    private void get_n_set_promocodes() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("price_cutters/salon_spa");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    final String name=dataSnapshot1.getKey().toString();
                    final String desc=dataSnapshot1.child("description").getValue().toString();
                    final int cutt = Integer.parseInt(dataSnapshot1.child("value").getValue().toString());
                    final String sub_category = dataSnapshot1.child("sub_category").getValue().toString();
                    try{
                        DatabaseReference usage_ref = FirebaseDatabase.getInstance().getReference().child("offer_usage/promo_codes/"+name);
                        usage_ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                loading.setVisibility(View.GONE);
                                if(!dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    if(hasSubCategory(sub_category)){promoCodeHashMap.put(name,new PriceCutter(name, desc, cutt, sub_category, false));
                                    Log.d("datta_text",name);
                                    adapter.notifyDataSetChanged();}
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d("datta_text","error in getting offer_usage : "+databaseError.toString());
                            }
                        });
                    }catch (Exception e){}
                    //check category
                    /*final String category=dataSnapshot1.child("category").getValue().toString();
                    final String name=dataSnapshot1.child("code").getValue().toString();
                    final String desc=dataSnapshot1.child("description").getValue().toString();
                    String gender=dataSnapshot1.child("gender").getValue().toString();
                    int min_charge=Integer.parseInt(dataSnapshot1.child("min_charge").getValue().toString());
                    int no_of_times=-1;
                    String temp_no_of_times=dataSnapshot1.child("no_of_times").getValue().toString();
                    if(!temp_no_of_times.equals("not_applicable")){no_of_times=Integer.parseInt(temp_no_of_times);}
                    String terms=dataSnapshot1.child("terms_conditions").getValue().toString();

                    try{
                        if(no_of_times!=-1){
                            Log.d("datta_text",name);
                            DatabaseReference usage_ref = FirebaseDatabase.getInstance().getReference().child("offer_usage/promo_codes/"+name);
                            usage_ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        boolean has_category = hasCategory(category);
                                        promoCodeHashMap.put(name,new PriceCutter(name, desc, false));
                                        Log.d("datta_text",name);
                                        adapter.notifyDataSetChanged();
                                        loading.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d("datta_text","error in getting offer_usage : "+databaseError.toString());
                                }
                            });
                        }
                        else {
                            boolean has_category = hasCategory(category);
                            promoCodeHashMap.put(name,new PriceCutter(name, desc, false));
                            Log.d("datta_text",name);
                            adapter.notifyDataSetChanged();
                        }
                    }catch(Exception e){}*/
                }


//                //if no promocode is available then notify
//                if(promoCodeHashMap.size() <= 0){
//                    Toast.makeText(getActivity(), "No available promocodes", Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text","error in getting promocodes : "+databaseError.toString());
            }
        });
    }

    boolean hasSubCategory(String sub_category){
        try {
            ArrayList<String> keys = new ArrayList<>(services_hashmap.keySet());
            for (int i = 0; i < services_hashmap.size(); i++) {
                if (services_hashmap.get(keys.get(i)).getSubCategory().equals(sub_category)) {
                    return true;
                }
            }
        }catch (Exception e){getDialog().dismiss();}
        return false;
    }

    class Promocode_adapter extends RecyclerView.Adapter<Promocode_ViewHoder>{
        View v;
        @Override
        public Promocode_ViewHoder onCreateViewHolder(ViewGroup parent, int viewType) {
            v=layoutInflater.inflate(R.layout.single_promocode_layout,parent,false);
            return new Promocode_ViewHoder(v);
        }

        @Override
        public void onBindViewHolder(Promocode_ViewHoder holder, int position) {
            ArrayList<String> keys=new ArrayList<>(promoCodeHashMap.keySet());
            Log.d("datta_text", "in onBindViewHolder");
            holder.setDetails(promoCodeHashMap.get(keys.get(position)));
        }

        @Override
        public int getItemCount() {
            return promoCodeHashMap.size();
        }
    }

    class Promocode_ViewHoder extends RecyclerView.ViewHolder{
        View v;
        public Promocode_ViewHoder(View itemView) {
            super(itemView);
            v=itemView;
            final TextView title=v.findViewById(R.id.name);
            Button apply=v.findViewById(R.id.apply);

            apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = title.getText().toString();
                    mpromocodeInterface.onPromocodeSelect(name, promoCodeHashMap.get(name).getSub_category(), promoCodeHashMap.get(name).getCutt());
                    selected_promocode = title.getText().toString();
                    getDialog().dismiss();
                }
            });

        }

        public void setDetails(PriceCutter priceCutter) {
            final TextView title=v.findViewById(R.id.name);
            TextView desc=v.findViewById(R.id.description);
            Button apply=v.findViewById(R.id.apply);
            //setting details
            v.setTag(priceCutter.getName());
            title.setText(priceCutter.getName());
            desc.setText(priceCutter.getDesc());
            Log.d("datta_text", "in set details");
        }
    }

    void refresh_recyclerView(){
        adapter.notifyDataSetChanged();
    }


    interface promocodeInterface{
        void onPromocodeSelect(String promocodeName, String sub_category, int cutt);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{mpromocodeInterface=(promocodeInterface)activity;}
        catch (Exception e){}
    }
}
