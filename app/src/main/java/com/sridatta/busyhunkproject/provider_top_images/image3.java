package com.sridatta.busyhunkproject.provider_top_images;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.sridatta.busyhunkproject.R;

public class image3 extends Fragment {
    View v;
    ImageView imageView;
    int POSITION=0;
    String PROVIDER_UID;

    public image3 getInstance(String provider_uid){
        PROVIDER_UID=provider_uid;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("providers/salons/"+PROVIDER_UID+"/image_urls/url2");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    final String image_url = dataSnapshot.getValue().toString();
                    Log.d("datta_text", "image url: " + image_url);

                    final Picasso picasso = Picasso.with(getActivity());
                    picasso.setIndicatorsEnabled(false);
                    picasso.load(Uri.parse(image_url)).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.promo_empty_background).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            picasso.with(getActivity()).load(Uri.parse(image_url)).placeholder(R.drawable.promo_empty_background).into(imageView);
                        }
                    });
                }catch (Exception e){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text","error in getting url0: "+databaseError.toString());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.provider_top_image_layout,container,false);
        //initiating ui
        imageView=v.findViewById(R.id.image_top);
        //

        return v;
    }
}
