package com.sridatta.busyhunkproject;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ChoosePhotoInputFragment extends DialogFragment {
    ImageView camera, gallery;
    View v;
    photoOptions mphotoOptions;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.choose_photo_input,container,false);
        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        getDialog().getWindow().setLayout(width,height);
        //inititating ui
        camera = v.findViewById(R.id.camera);
        gallery = v.findViewById(R.id.gallery);

        //camera onClick
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mphotoOptions.choosedOption(0);
                getDialog().dismiss();
            }
        });
        //gallery onclick
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mphotoOptions.choosedOption(1);
                getDialog().dismiss();
            }
        });
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fragment style
        int style = android.support.v4.app.DialogFragment.STYLE_NORMAL;
        style = android.support.v4.app.DialogFragment.STYLE_NO_TITLE;
        setStyle(style,android.R.style.Theme_DeviceDefault_Dialog);
    }

    interface photoOptions{
        void choosedOption(int num);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mphotoOptions=(photoOptions)activity;
    }

}
