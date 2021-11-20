package com.sridatta.busyhunkproject;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class BookingsFilterBottomsheetDialog extends DialogFragment {
    View v;
    Button all, successful, failed, completed, missed, cancelled;
    BookingsFilter bookingsFilter;
    String selected_filter = null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.booking_filter_dialog, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //getting arguments
        selected_filter = getArguments().getString("filter", null);
        Toast.makeText(getActivity(), selected_filter, Toast.LENGTH_SHORT).show();
        //initiating ui
        all = v.findViewById(R.id.button7);
        successful = v.findViewById(R.id.button11);
        failed = v.findViewById(R.id.button10);
        completed = v.findViewById(R.id.button9);
        cancelled = v.findViewById(R.id.button12);
        missed = v.findViewById(R.id.button16);

        //set already selected filter
        setSelected(selected_filter);

        //onclicks
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingsFilter.selectedFilter(null);
                getDialog().dismiss();
            }
        });

        failed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingsFilter.selectedFilter(failed.getText().toString());
                getDialog().dismiss();
            }
        });

        successful.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingsFilter.selectedFilter(successful.getText().toString());
                getDialog().dismiss();
            }
        });

        completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingsFilter.selectedFilter(completed.getText().toString());
                getDialog().dismiss();
            }
        });

        missed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingsFilter.selectedFilter(missed.getText().toString());
                getDialog().dismiss();
            }
        });

        cancelled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingsFilter.selectedFilter(cancelled.getText().toString());
                getDialog().dismiss();
            }
        });
        return v;
    }


    void setSelected(String s){
            removeAll();
            if(s == null){
                Log.d("datta_text", "null_text");
                all.setBackgroundColor(getActivity().getResources().getColor(R.color.colorAccent));
            } else if(failed.getText().toString().equals(s)){
                failed.setBackground(getActivity().getDrawable(R.color.colorAccent));
            } else if(cancelled.getText().toString().equals(s)){
                cancelled.setBackgroundColor(getActivity().getResources().getColor(R.color.colorAccent));
            } else if(completed.getText().toString().equals(s)){
                completed.setBackgroundColor(getActivity().getResources().getColor(R.color.colorAccent));
            } else if(successful.getText().toString().equals(s)){
                successful.setBackgroundColor(getActivity().getResources().getColor(R.color.colorAccent));
            } else if(missed.getText().toString().equals(s)){
                missed.setBackgroundColor(getActivity().getResources().getColor(R.color.colorAccent));
            }
        }

    private void removeAll() {
            all.setBackground(getActivity().getDrawable(R.drawable.white_dialog_curved_corners));
            failed.setBackground(getActivity().getDrawable(R.drawable.login_button_primry_shape));
            completed.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
            missed.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
            successful.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
            cancelled.setBackground(getActivity().getResources().getDrawable(R.drawable.accent_radius_outline_button));
    }


    interface BookingsFilter{
            void selectedFilter(String filter);
        }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bookingsFilter = (BookingsFilter) activity;
    }
}
