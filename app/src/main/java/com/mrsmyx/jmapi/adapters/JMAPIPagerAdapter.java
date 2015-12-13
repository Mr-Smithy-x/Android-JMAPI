package com.mrsmyx.jmapi.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mrsmyx.JMAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cj on 10/10/15.
 */
public class JMAPIPagerAdapter extends FragmentStatePagerAdapter {

    private List<JPAGE> fragmentList = new ArrayList<JPAGE>();

    public JMAPIPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void append(JPAGE jPage){
        fragmentList.add(jPage);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position).getFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentList.get(position).getTitle();
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public static class JPAGE {
        String title;
        Fragment fragment;

        public static JPAGE Builder(){
            return new JPAGE();
        }

        public String getTitle() {
            return title;
        }

        public JPAGE setTitle(String title) {
            this.title = title;
            return this;
        }

        public Fragment getFragment() {
            return fragment;
        }

        public JPAGE setFragment(Fragment fragment) {
            this.fragment = fragment;
            return this;
        }
    }
}
