package com.sridatta.busyhunkproject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RefferalRedeemServiceDialogFragment extends DialogFragment {
    View v;
    ProgressBar horizontal_loading;
    RadioButton hair_care, skin_care, male, female;
    Button okay_button;
    String SELECTED_CATEGORY, SELECTED_GENDER;

    private OnFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_refferal_redeem_service_dialog, container, false);
        //initiating ui
        male = v.findViewById(R.id.male);
        female = v.findViewById(R.id.female);
        horizontal_loading = v.findViewById(R.id.horizontal_loading);
        hair_care = v.findViewById(R.id.button16);
        skin_care = v.findViewById(R.id.button11);
        okay_button = v.findViewById(R.id.button17);
        try {
            //hair care onlick
            hair_care.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SELECTED_CATEGORY = getActivity().getResources().getString(R.string.hair);
                    setSELECTED_CATEGORY(1);
                }
            });
            //skin care onclikc
            skin_care.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SELECTED_CATEGORY = getActivity().getResources().getString(R.string.skin_care);
                    setSELECTED_CATEGORY(2);
                }
            });
            //male onClick
            male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SELECTED_GENDER = getActivity().getResources().getString(R.string.male);
                }
            });
            //female onClick
            female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SELECTED_GENDER = getActivity().getResources().getString(R.string.female);
                }
            });
        }catch (Exception e){}

        //okay onclick
        okay_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SELECTED_CATEGORY != null && SELECTED_CATEGORY.length() > 0){
                    if(SELECTED_GENDER != null && SELECTED_GENDER.length() >0){
                        mListener.onFragmentInteraction(SELECTED_CATEGORY, SELECTED_GENDER);
                        getDialog().dismiss();
                    }else {
                        Toast.makeText(getActivity(), "Please select gender", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Please select category", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return v;
    }


    //method to handle selected category
    void setSELECTED_CATEGORY(int position){
        try{
        switch (position){
            case 1: hair_care.setChecked(true);
                skin_care.setChecked(false);
                break;
            case 2: skin_care.setChecked(true);
                hair_care.setChecked(false);
                break;
        }
        }catch (Exception e){}
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setting style to remove title
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE,android.R.style.Theme_DeviceDefault_Dialog);
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
        void onFragmentInteraction(String category, String gender);
    }
}
