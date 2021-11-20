package com.sridatta.busyhunkproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.igenius.customcheckbox.CustomCheckBox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;


@SuppressLint("ValidFragment")
public class SubcategoryFragment extends Fragment {
    String CATEGORY, PROVIDER_UID, GENDER;
    TreeMap<String, SalonServicesActivity.SubCategory> sub_categories_list = new TreeMap<>();
    private OnFragmentInteractionListener mListener;
    LottieAnimationView loading_anim;
    RecyclerView recyclerView;
    View v;
    LayoutInflater layoutInflater;
    LinkedHashMap<String, SalonServicesActivity.service> serviceList=new LinkedHashMap<>();
    serviceAdapter service_adapter;
    parentAdapter parent_adapter;
    HashMap<String, SalonServicesActivity.service> SelectedServiceList = new HashMap<>();

    @SuppressLint("ValidFragment")
    public SubcategoryFragment(String category, String provider_uid, String gender) {
        Log.d("datta_text", "in subCategory "+category);
        this.CATEGORY = category;
        this.PROVIDER_UID = provider_uid;
        this.GENDER = gender;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //layout inflater
        layoutInflater = LayoutInflater.from(getActivity());
        v = inflater.inflate(R.layout.sub_category_fragment, container, false);
        //initiating ui
        parent_adapter = new parentAdapter();
        loading_anim = v.findViewById(R.id.sub_cat_progressbar); loading_anim.playAnimation();
        recyclerView = v.findViewById(R.id.container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(parent_adapter);
        get_n_set_details();
        return v;
    }

    void get_n_set_details() {
        loading_anim.setVisibility(View.VISIBLE);
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("providers/salons/" + PROVIDER_UID + "/location");
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String location = dataSnapshot.getValue().toString();
                final DatabaseReference sub_cat_ref = FirebaseDatabase.getInstance().getReference().child("services/salons/" + location + "/" + PROVIDER_UID + "/" + GENDER + "/" + CATEGORY);
                sub_cat_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot sub_category_snashot : dataSnapshot.getChildren()) {
                            TreeMap<String, SalonServicesActivity.service> local_subcat_list = new TreeMap<>();
                            String curr_subcategory = sub_category_snashot.getKey().toString();
                            TreeMap<String, SalonServicesActivity.service> local_serviceTreemap = new TreeMap<>();
                            for (DataSnapshot servicesSnapShot : sub_category_snashot.getChildren()) {
                                String service_name = servicesSnapShot.getKey().toString();
                                String act_charge = servicesSnapShot.child("charge").getValue().toString();
                                String curr_charge = servicesSnapShot.child("curr_charge").getValue().toString();
                                String span = servicesSnapShot.child("span").getValue().toString();
                                int time_hours = Integer.parseInt(servicesSnapShot.child("time_hours").getValue().toString());
                                double time_mins = Double.parseDouble(servicesSnapShot.child("time_mins").getValue().toString());
                                //getting charge and span in numbers
                                int charge_in_num = Integer.parseInt(curr_charge);
                                double span_in_num = time_hours + time_mins / 60;

                                Log.d("datta_text", "span is " + Double.toString(span_in_num));
                                Log.d("datta_text", "charge is " + Integer.toString(charge_in_num));

                                SalonServicesActivity.service localService = new SalonServicesActivity.service(CATEGORY, curr_subcategory, service_name, act_charge, curr_charge, span, time_hours, charge_in_num, time_mins, span_in_num, false);
                                local_subcat_list.put(service_name, localService);
                                //addin service to service list
                                serviceList.put(service_name, localService);


                            }
                            //local sub category treemap
                            SalonServicesActivity sa = new SalonServicesActivity();
                            sub_categories_list.put(curr_subcategory, sa.new SubCategory(curr_subcategory, local_subcat_list));
                            parent_adapter.notifyDataSetChanged();
                            //progress bar visibility
                            loading_anim.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class parentAdapter extends RecyclerView.Adapter<parentViewHolder> implements Serializable{
        View v;
        @Override
        public parentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v=layoutInflater.inflate(R.layout.service_parent,parent,false);
            return new parentViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final parentViewHolder holder, final int position) {
            ArrayList<String> keys = new ArrayList<>(sub_categories_list.keySet());
            holder.setDetails(sub_categories_list.get(keys.get(position)));
            //onClick
            final RecyclerView childRecyclerView=v.findViewById(R.id.child);
            final ImageView up_down_image=v.findViewById(R.id.up_down_image);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(childRecyclerView.getVisibility()==View.GONE){
                        childRecyclerView.setVisibility(View.VISIBLE);
                        up_down_image.setImageDrawable(getResources().getDrawable(R.drawable.minus));
                        recyclerView.scrollToPosition(position);
                    }
                    else{
                        childRecyclerView.setVisibility(View.GONE);
                        up_down_image.setImageDrawable(getResources().getDrawable(R.drawable.plus));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return sub_categories_list.size();
        }
    }

    public class parentViewHolder extends RecyclerView.ViewHolder implements Serializable{
        View v;
        public parentViewHolder(final View itemView) {
            super(itemView);
            v=itemView;
        }

        void setDetails(SalonServicesActivity.SubCategory c){
            TextView title_textView=v.findViewById(R.id.title_text);
            title_textView.setText(c.getTitle());

            //nested reccycler view
            RecyclerView childRecyclerView=v.findViewById(R.id.child);
            childRecyclerView.setHasFixedSize(true);
            childRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            //according to whick kind of linke hashmap adapter will be set
            List<String> keys=new ArrayList<>(c.getServicesHashmap().keySet());
            service_adapter=new serviceAdapter(c.getServicesHashmap());
            childRecyclerView.setAdapter(service_adapter);
            /*if(c.objectLinkedHashMap.get(keys.get(0)) instanceof Offer){
                offer_adapter=new offerAdapter(c.objectLinkedHashMap);
                childRecyclerView.setAdapter(offer_adapter);
            }
            else{
                service_adapter=new serviceAdapter(c.objectLinkedHashMap);
                childRecyclerView.setAdapter(service_adapter);
            }*/
        }
    }

    class serviceAdapter extends RecyclerView.Adapter<serviceViewHolder> implements Serializable{
        TreeMap<String,SalonServicesActivity.service> serviceTreemap;
        View v;

        public serviceAdapter(TreeMap<String, SalonServicesActivity.service> serviceTreeHashmap) {
            this.serviceTreemap = serviceTreeHashmap;
        }

        @Override
        public serviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v=layoutInflater.inflate(R.layout.service_to_select,parent,false);
            return new serviceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(serviceViewHolder holder, int position) {
            List<String> keys=new ArrayList<>(serviceTreemap.keySet());
            holder.setDetails(serviceTreemap.get(keys.get(position)));
        }

        @Override
        public int getItemCount() {
            return serviceTreemap.size();
        }
    }

    public class serviceViewHolder extends RecyclerView.ViewHolder implements Serializable{
        View v;
        public serviceViewHolder(View itemView) {
            super(itemView);
            v=itemView;
            final TextView name_textview=v.findViewById(R.id.service_name);
            final CustomCheckBox checkBox=v.findViewById(R.id.offer_checkbox);
            final Button cover_button=v.findViewById(R.id.cover_button);
            //onClick
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verifyOnClick();
                }
            });

            //checkbox clicks
            cover_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verifyOnClick();
                }
            });



        }

        void verifyOnClick(){
            /*checking previously enabled or not*/
            final TextView name_textview=v.findViewById(R.id.service_name);
            final CustomCheckBox checkBox=v.findViewById(R.id.offer_checkbox);
            final Button cover_button=v.findViewById(R.id.cover_button);
            if(SelectedServiceList.containsKey(name_textview.getText().toString())){
                //remove from selection
                SelectedServiceList.remove(name_textview.getText().toString());
                //call interface method
                mListener.removeService(serviceList.get(name_textview.getText().toString()));
                //ticking checkbox
                checkBox.toggle();
            }
            else{
                //add to selected list
                SelectedServiceList.put(name_textview.getText().toString(), serviceList.get(name_textview.getText().toString()));
                // call intrface method
                mListener.addService(serviceList.get(name_textview.getText().toString()));
                //ticking checkbox
                checkBox.toggle();
            }
        }



        void setDetails(SalonServicesActivity.service s){
            TextView name_TextView=v.findViewById(R.id.service_name);
            TextView span_textView=v.findViewById(R.id.service_span);
            TextView curr_charge=v.findViewById(R.id.curr_charge);
            TextView act_charge=v.findViewById(R.id.act_charge);
            View strike_view = v.findViewById(R.id.view16);

            //setting details
            name_TextView.setText(s.getService_name());
            span_textView.setText(s.getSpan());
            curr_charge.setText("₹"+s.getCurr_charge());
            act_charge.setText("₹"+s.getAct_charge());
            if(s.getCurr_charge().equals(s.getAct_charge())){
                act_charge.setVisibility(View.GONE);
                strike_view.setVisibility(View.GONE);
            }
            //checkbox ui updating
            final CustomCheckBox checkBox=v.findViewById(R.id.offer_checkbox);
            if(SelectedServiceList.containsKey(s.getService_name())){
                checkBox.setChecked(true);
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void addService(SalonServicesActivity.service s);
        // method to remove service
        void removeService(SalonServicesActivity.service s);
    }


}

