package com.pplugin.messo_se.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.pplugin.messo_se.ui.messages.MessagesFragment;
import com.pplugin.messo_se.ui.contacts.ContactsFragment;
import com.pplugin.messo_se.ui.profile.ProfileFragment;
import com.pplugin.messo_se.ui.search.SearchFragment;

public class ViewpagerAdapter extends FragmentStatePagerAdapter {
    public ViewpagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MessagesFragment();
            case 1:
                return new ContactsFragment();
            case 2:
                return new SearchFragment();
            case 3:
                return new ProfileFragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 4;
    }
}
