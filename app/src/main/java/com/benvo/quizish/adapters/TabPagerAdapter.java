package com.benvo.quizish.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.benvo.quizish.fragments.DeckRecyclerFragment;

import java.util.ArrayList;

/**
 * Created by benvo on 4/22/2017.
 */

public class TabPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<DeckRecyclerFragment> fragmentList = new ArrayList<>();
    private final ArrayList<String> fragmentTitleList = new ArrayList<>();

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(DeckRecyclerFragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
    }

    public void setFragment(int position, DeckRecyclerFragment fragment, String title) {
        fragmentList.set(position, fragment);
        fragmentTitleList.set(position, title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
