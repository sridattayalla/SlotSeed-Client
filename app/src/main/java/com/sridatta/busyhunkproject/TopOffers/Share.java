package com.sridatta.busyhunkproject.TopOffers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sridatta.busyhunkproject.R;
import com.sridatta.busyhunkproject.ShareActivity;

public class Share extends Fragment {
    View v;
    ImageView layout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.top_offers_single_layout,container,false);
        //initiating ui
        layout = v.findViewById(R.id.image_holder);
        layout.setImageResource(R.drawable.share_poster);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        final float dpWidth  = outMetrics.widthPixels;
        final float modified_height = dpWidth/16*9; //16 : 9 formfactor
        layout.getLayoutParams().height = (int)modified_height;
        layout.requestLayout();
        layout.setScaleType(ImageView.ScaleType.FIT_XY);
        //onClick
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ShareActivity.class));
            }
        });
        return v;
    }
}
