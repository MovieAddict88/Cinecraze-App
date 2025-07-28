package com.cinecraze.free.stream;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.cinecraze.free.stream.fragments.HomeFragment;
import com.cinecraze.free.stream.fragments.LiveTvFragment;
import com.cinecraze.free.stream.fragments.MoviesFragment;
import com.cinecraze.free.stream.fragments.SearchFragment;
import com.cinecraze.free.stream.fragments.SeriesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new MoviesFragment();
            case 2:
                return new SeriesFragment();
            case 3:
                return new LiveTvFragment();
            case 4:
                return new SearchFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5; // Home, Movies, Series, LiveTV, Search
    }
}