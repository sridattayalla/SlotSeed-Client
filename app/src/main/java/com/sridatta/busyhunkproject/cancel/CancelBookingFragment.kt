package com.sridatta.busyhunkproject.cancel

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.sridatta.busyhunkproject.R

class CancelBookingFragment: DialogFragment() {
    lateinit var v: View
    lateinit var cancel: Button
    lateinit var back:Button
    lateinit var showloading: showLoading

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.cancel_booking_dialog, container, false)

        //initializing ui
        cancel = v.findViewById(R.id.cancel)
        back = v.findViewById(R.id.back)

        //back onclick
        back.setOnClickListener( View.OnClickListener {
            dialog.dismiss()
        })
        return v;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setting style to remove title
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog);
    }
    
    interface showLoading{
        fun load();
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        showloading = activity as showLoading
    }
}