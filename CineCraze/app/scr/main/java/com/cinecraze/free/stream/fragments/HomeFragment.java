package com.cinecraze.free.stream.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cinecraze.free.stream.CarouselAdapter;
import com.cinecraze.free.stream.MainActivity;
import com.cinecraze.free.stream.MovieAdapter;
import com.cinecraze.free.stream.R;
import com.cinecraze.free.stream.models.Entry;

import java.util.List;

public class HomeFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private RecyclerView carouselRecyclerView;
    private ViewPager2 carousel;
    private MovieAdapter movieAdapter;
    private CarouselAdapter carouselAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initViews(view);
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        carouselRecyclerView = view.findViewById(R.id.carousel_recycler_view);
        carousel = view.findViewById(R.id.carousel);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieAdapter = new MovieAdapter(getContext());
        recyclerView.setAdapter(movieAdapter);
        
        // Setup Carousel
        if (carouselRecyclerView != null) {
            carouselRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            carouselAdapter = new CarouselAdapter(getContext());
            carouselRecyclerView.setAdapter(carouselAdapter);
        }
        
        if (carousel != null) {
            carouselAdapter = new CarouselAdapter(getContext());
            carousel.setAdapter(carouselAdapter);
        }
    }
    
    public void updateMovies(List<Entry> movies) {
        if (movieAdapter != null) {
            movieAdapter.updateMovies(movies);
        }
    }
    
    public void updateCarousel(List<Entry> carouselItems) {
        if (carouselAdapter != null) {
            carouselAdapter.updateMovies(carouselItems);
        }
    }
    
    public MovieAdapter getMovieAdapter() {
        return movieAdapter;
    }
    
    public CarouselAdapter getCarouselAdapter() {
        return carouselAdapter;
    }
}