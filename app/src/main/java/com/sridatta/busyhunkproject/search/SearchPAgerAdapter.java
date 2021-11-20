package com.sridatta.busyhunkproject.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SearchPAgerAdapter extends FragmentPagerAdapter {


    String[] titles={"Services","Providers"};
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    public SearchPAgerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:return new servicesFragment();
            case 1:return new ProvidersFragment();
            default:return new servicesFragment();
        }

    }



    @Override
    public int getCount() {
        return 2;
    }
}
