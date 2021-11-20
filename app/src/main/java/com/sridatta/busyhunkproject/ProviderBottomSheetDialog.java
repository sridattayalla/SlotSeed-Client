package com.sridatta.busyhunkproject;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.HashMap;

public class ProviderBottomSheetDialog extends BottomSheetDialogFragment implements CustomCheckBox.OnCheckedChangeListener {
    HashMap<String, String> selected_categories = new HashMap<>();
    CustomCheckBox hair_checkbox, skin_checkbox, manicure_checkbox, pedicure_checkbox, spa_checkbox;
    View v;
    category_interface mCategory_interface;
    Button done;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.providers_bottomsheet_dialog, container, false);
        //
        selected_categories = (HashMap<String, String>) getArguments().getSerializable("categories");
        //initiating  ui
        hair_checkbox = v.findViewById(R.id.hair_cut_cbox); hair_checkbox.setOnCheckedChangeListener(this);
        skin_checkbox = v.findViewById(R.id.skin_cbox); skin_checkbox.setOnCheckedChangeListener(this);
        manicure_checkbox = v.findViewById(R.id.manicure_cbox); manicure_checkbox.setOnCheckedChangeListener(this);
        pedicure_checkbox = v.findViewById(R.id.pedicure_cbox); pedicure_checkbox.setOnCheckedChangeListener(this);
        spa_checkbox = v.findViewById(R.id.spa_cbox); spa_checkbox.setOnCheckedChangeListener(this);
        done = v.findViewById(R.id.done);
        //initialize checkboxes
        initializeCheckboxes();
        //done
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCategory_interface.getSelectedCategories(selected_categories);
                getDialog().dismiss();
            }
        });

        return v;
    }

    private void initializeCheckboxes() {
        ArrayList<String> keys = new ArrayList<>(selected_categories.keySet());
        for(int i=0; i< keys.size(); i++){
            String category = selected_categories.get(keys.get(i));
            //checking each checkbox and ticking if selected
            if(hair_checkbox.getTag().toString().equals(category)){
                hair_checkbox.toggle();
            }
            if(skin_checkbox.getTag().toString().equals(category)){
                skin_checkbox.toggle();
            }
            if(manicure_checkbox.getTag().toString().equals(category)){
                manicure_checkbox.toggle();
            }
            if(pedicure_checkbox.getTag().toString().equals(category)){
                pedicure_checkbox.toggle();
            }
            if(spa_checkbox.getTag().toString().equals(category)){
                spa_checkbox.toggle();
            }
        }
    }

    @Override
    public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
        if(checkBox.isChecked()){
            selected_categories.put(checkBox.getTag().toString(), checkBox.getTag().toString());
            Log.d("datta_text", checkBox.getTag().toString() + " is added");
        }
        else {
            selected_categories.remove(checkBox.getTag().toString());
            Log.d("datta_text", checkBox.getTag().toString() + " is removed");
        }
    }

    interface category_interface{
        void getSelectedCategories(HashMap hashMap);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCategory_interface=(category_interface)activity;
    }
}
