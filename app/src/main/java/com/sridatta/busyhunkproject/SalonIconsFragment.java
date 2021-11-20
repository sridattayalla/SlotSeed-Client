package com.sridatta.busyhunkproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.sridatta.busyhunkproject.R;

import org.jetbrains.annotations.NotNull;
import io.ghyeok.stickyswitch.widget.StickySwitch;

public class SalonIconsFragment extends Fragment implements View.OnClickListener {
    View v;
//    Button icon_switch_cover;
//    IconSwitch iconSwitch;
    CardView male_iconsCard, female_iconsCard;
    ConstraintLayout he_layout, she_layout;
    ImageView male_hair, male_skin, male_spa, male_All, female_hair, female_skin, female_Spa, female_all;
    TextView m_hair, m_skin, m_spa, m_all, f_hair, f_skin, f_spa, f_All;
    StickySwitch stickySwitch;
    TextView indicator_test;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.salon_icons_layout, container, false);
        //initiating ui
//        icon_switch_cover = v.findViewById(R.id.switch_cover);
//        iconSwitch = v.findViewById(R.id.icon_switch);
//        male_iconsCard = v.findViewById(R.id.men_card);
//        female_iconsCard = v.findViewById(R.id.women_card);
        indicator_test = v.findViewById(R.id.textView20);
        stickySwitch = v.findViewById(R.id.toggleSwitch);
        he_layout = v.findViewById(R.id.he_layout);
        she_layout = v.findViewById(R.id.she_layout);
        male_hair = v.findViewById(R.id.mi_hair);male_hair.setOnClickListener(this);
        male_skin = v.findViewById(R.id.mi_skin);male_skin.setOnClickListener(this);
        male_spa = v.findViewById(R.id.mi_spa);male_spa.setOnClickListener(this);
        male_All = v.findViewById(R.id.mi_all);male_All.setOnClickListener(this);
        m_hair = v.findViewById(R.id.m_hair);m_hair.setOnClickListener(this);
        m_skin = v.findViewById(R.id.m_skin);m_skin.setOnClickListener(this);
        m_spa = v.findViewById(R.id.m_spa);m_spa.setOnClickListener(this);
        m_all = v.findViewById(R.id.m_all);m_all.setOnClickListener(this);

        female_hair = v.findViewById(R.id.fi_hair);female_hair.setOnClickListener(this);
        female_skin = v.findViewById(R.id.fi_skin);female_skin.setOnClickListener(this);
        female_Spa = v.findViewById(R.id.fi_spa);female_Spa.setOnClickListener(this);
        female_all = v.findViewById(R.id.fi_all);female_all.setOnClickListener(this);
        f_hair = v.findViewById(R.id.f_hair);m_hair.setOnClickListener(this);
        f_skin = v.findViewById(R.id.f_skin);f_skin.setOnClickListener(this);
        f_spa = v.findViewById(R.id.f_spa);f_spa.setOnClickListener(this);
        f_All = v.findViewById(R.id.f_all);f_All.setOnClickListener(this);



        //switch cover onclick
        /*icon switch is not working properly in sliding so in order to disable direct use of it cover it with transperent button*/
//        icon_switch_cover.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                iconSwitch.toggle();
//            }
//        });
//
//        //icon switch changed
//        iconSwitch.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
//            @Override
//            public void onCheckChanged(IconSwitch.Checked current) {
//                Log.d("iconSwitch",current.toString());
//                if(current.equals(IconSwitch.Checked.LEFT)){
//                    female_iconsCard.setVisibility(View.GONE);
//                   male_iconsCard.setVisibility(View.VISIBLE);
//                }
//                else{
//                    male_iconsCard.setVisibility(View.GONE);
//                    female_iconsCard.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//        toggleSwitch.setOnToggleSwitchChangeListener(new ToggleSwitch.OnToggleSwitchChangeListener(){
//
//            @Override
//            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
//                //Toast.makeText(getActivity(), Integer.toString(position), Toast.LENGTH_SHORT).show();
//                if(position == 0){
//                    indicator_test.setText("Men grooming");
//                    she_layout.setVisibility(View.GONE);
//                    he_layout.setVisibility(View.VISIBLE);
//                }else{
//                    indicator_test.setText("Women grooming");
//                    he_layout.setVisibility(View.GONE);
//                    she_layout.setVisibility(View.VISIBLE);
//                }
//            }
//        });
        stickySwitch.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NotNull StickySwitch.Direction direction, @NotNull String text) {
                if(direction.name().equals(StickySwitch.Direction.LEFT.name())){
                    indicator_test.setText("Men grooming");
                    she_layout.setVisibility(View.GONE);
                    he_layout.setVisibility(View.VISIBLE);
                }else{
                    indicator_test.setText("Women grooming");
                    he_layout.setVisibility(View.GONE);
                    she_layout.setVisibility(View.VISIBLE);
                }
            }
        });

        return v;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.f_hair || view.getId() == R.id.fi_hair){
            Intent hair_intent=new Intent(getActivity(),ProvidersActivity.class);
            hair_intent.putExtra("category",getResources().getString(R.string.hair));
            hair_intent.putExtra("gender",getResources().getString(R.string.female));
            startActivity(hair_intent);
        }
        else if(view.getId() == R.id.m_hair || view.getId() == R.id.mi_hair){
            Intent hair_intent=new Intent(getActivity(),ProvidersActivity.class);
            hair_intent.putExtra("category",getResources().getString(R.string.hair));
            hair_intent.putExtra("gender",getResources().getString(R.string.male));
            startActivity(hair_intent);
        }
        else if(view.getId() == R.id.f_skin || view.getId() == R.id.fi_skin){
            Intent skin_intent=new Intent(getActivity(),ProvidersActivity.class);
            skin_intent.putExtra("category",getResources().getString(R.string.skin_care));
            skin_intent.putExtra("gender",getResources().getString(R.string.female));
            startActivity(skin_intent);
        }
        else if(view.getId() == R.id.m_skin || view.getId() == R.id.mi_skin){
            Intent skin_intent=new Intent(getActivity(),ProvidersActivity.class);
            skin_intent.putExtra("category",getResources().getString(R.string.skin_care));
            skin_intent.putExtra("gender",getResources().getString(R.string.male));
            startActivity(skin_intent);
        }
        else if(view.getId() == R.id.f_spa || view.getId() == R.id.fi_spa){
            Intent spa_intent=new Intent(getActivity(),ProvidersActivity.class);
            spa_intent.putExtra("category",getResources().getString(R.string.spa));
            spa_intent.putExtra("gender",getResources().getString(R.string.female));
            startActivity(spa_intent);
        }
        else if(view.getId() == R.id.m_spa || view.getId() == R.id.mi_spa){
            Intent spa_intent=new Intent(getActivity(),ProvidersActivity.class);
            spa_intent.putExtra("category",getResources().getString(R.string.spa));
            spa_intent.putExtra("gender",getResources().getString(R.string.male));
            startActivity(spa_intent);
        }
        else if(view.getId() == R.id.m_all || view.getId()==R.id.mi_all){
            Intent all_intent=new Intent(getActivity(),ProvidersActivity.class);
            all_intent.putExtra("gender",getResources().getString(R.string.male));
            startActivity(all_intent);
        }
        else if(view.getId() == R.id.f_all || view.getId()==R.id.fi_all){
            Intent all_intent=new Intent(getActivity(),ProvidersActivity.class);
            all_intent.putExtra("gender",getResources().getString(R.string.female));
            startActivity(all_intent);
        }
    }
}
