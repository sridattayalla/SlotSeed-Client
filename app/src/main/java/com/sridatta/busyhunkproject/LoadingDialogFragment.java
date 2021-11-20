package com.sridatta.busyhunkproject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;

public class LoadingDialogFragment extends DialogFragment {
    LottieAnimationView animationView;

    static LoadingDialogFragment newInstance() {
        LoadingDialogFragment f = new LoadingDialogFragment();
        return f;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.loading_dialog_fragment, container, false);
        //initiating ui
        animationView = v.findViewById(R.id.animation_viewfrog);
        //implicitly declaring layout size
//        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
//        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
//        getDialog().getWindow().setLayout(width,height);
        //start animation
        //animationView.playAnimation();
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fragment style
        //this will remove top space of dialog that usually there for title
        int style = android.support.v4.app.DialogFragment.STYLE_NORMAL;
        style = android.support.v4.app.DialogFragment.STYLE_NO_TITLE;
        setStyle(style,android.R.style.Theme_DeviceDefault_Dialog);
    }

    public void dismissFragment(){
        try {
            getDialog().dismiss();
        }catch (Exception e){}

    }
}
