package com.sridatta.busyhunkproject;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sridatta.busyhunkproject.models.location;

import java.util.ArrayList;
import java.util.List;

public class locationSearchDialog extends DialogFragment {
    RecyclerView recyclerView;
    EditText searchLocation;
    LayoutInflater layoutInflater;
    List<Object> locations_list=new ArrayList<>();
    location_recyclerview_adapter locationRecyclerviewAdapter;
    location_interface locationInterface;

    static locationSearchDialog newInstance() {
        locationSearchDialog f = new locationSearchDialog();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_location_search, container, false);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        layoutInflater=LayoutInflater.from(getActivity());
        //initiating ui
        searchLocation=v.findViewById(R.id.searchLocation);
        recyclerView=v.findViewById(R.id.locations_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //okay Onclick
//        okay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                locationInterface.getChoosedLocation("kakinada");
//                getDialog().dismiss();
//            }
//        });

        //text changed search
        searchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Toast.makeText(getActivity(),charSequence.toString(),Toast.LENGTH_SHORT).show();
                //getting data
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("locations");
                Query query=reference.orderByKey().startAt(charSequence.toString()).endAt(charSequence.toString()+"\uf8ff");
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //emptying list
                        locations_list.clear();
                        for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            String location_name=dataSnapshot1.getKey().toString();
                            String disp_location_name=dataSnapshot1.child("disp_location_name").getValue() .toString();
                            Log.d("location_name",location_name);
                            String details=dataSnapshot1.child("details").getValue().toString();
                            locations_list.add(new location(location_name,details,disp_location_name));
                        }

                        locationRecyclerviewAdapter=new location_recyclerview_adapter();
                        recyclerView.setAdapter(locationRecyclerviewAdapter);
                    }



                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                       // Toast.makeText(getActivity(),databaseError.toString().toString(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //populate recycler view
        get_n_set_locations();
        return v;
    }

    private void get_n_set_locations() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("locations");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //emptying list
                locations_list.clear();
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String location_name=dataSnapshot1.getKey().toString();
                    String disp_location_name=dataSnapshot1.child("disp_location_name").getValue() .toString();
                    Log.d("location_name",location_name);
                    String details=dataSnapshot1.child("details").getValue().toString();
                    locations_list.add(new location(location_name,details,disp_location_name));
                }

                locationRecyclerviewAdapter=new location_recyclerview_adapter();
                recyclerView.setAdapter(locationRecyclerviewAdapter);
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(getActivity(),databaseError.toString().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class location_recyclerview_adapter extends RecyclerView.Adapter<locationViewHolder>{
        @Override
        public locationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View v=layoutInflater.inflate(R.layout.location_single,parent,false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locationInterface.getChoosedLocation(v.getTag().toString());
                    getDialog().dismiss();
                }
            });
            locationViewHolder locationViewHolder=new locationViewHolder(v);
            return locationViewHolder;
        }

        @Override
        public void onBindViewHolder(locationViewHolder holder, int position) {
            final location location = (location) locations_list.get(position);
            holder.setLocationName(location.getDisp_location_name());
            holder.setDetails(location.getLocation_details());
            holder.setViewTag(location.getLocation_name());
        }

        @Override
        public int getItemCount() {
            return locations_list.size();
        }
    }

    public class locationViewHolder extends RecyclerView.ViewHolder {
        View v;

        public locationViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setViewTag(String location_tag){
            v.setTag(location_tag);
        }

        public void setLocationName(String name) {
            TextView textView = v.findViewById(R.id.location_name);
            textView.setText(name);
        }

        public void setDetails(String details) {
            TextView textView = v.findViewById(R.id.location_details);
            textView.setText(details);
        }
    }

    public interface location_interface{
        void getChoosedLocation(String choosed_location_name);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        locationInterface=(location_interface)activity;
    }
}
