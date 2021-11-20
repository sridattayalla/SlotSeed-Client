package com.sridatta.busyhunkproject.cancel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sridatta.busyhunkproject.R;

public class CancelUnableDialog extends DialogFragment {
    View v;
    Button okay;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.unable_to_cancel_dialog, container, false);
        //initiating ui
        okay = v.findViewById(R.id.okay);

        //okay onClick
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setting style to remove title
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE,android.R.style.Theme_DeviceDefault_Dialog);
    }
}
