package com.sridatta.busyhunkproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class LocationPermssionFragment extends DialogFragment {
    View v;
    Button accept;
    response mresponse;

    static LocationPermssionFragment newInstance() {
        LocationPermssionFragment f = new LocationPermssionFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_location_permission,container,false);
        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        getDialog().getWindow().setLayout(width,height);
        //inititating ui
        accept=v.findViewById(R.id.button4);

        //accept onClick
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mresponse.returnResponce(1);
                getDialog().dismiss();
            }
        });
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fragment style
        int style = DialogFragment.STYLE_NORMAL;
        style = DialogFragment.STYLE_NO_TITLE;
        setStyle(style,android.R.style.Theme_DeviceDefault_Dialog);
    }

    interface response{
        void returnResponce(int num);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mresponse=(response)activity;
    }
}
