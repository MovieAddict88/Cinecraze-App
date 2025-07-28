package com.cinecraze.free.stream.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinecraze.free.stream.MovieAdapter;
import com.cinecraze.free.stream.R;
import com.cinecraze.free.stream.models.Entry;

import java.util.List;

public class SeriesFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_series, container, false);
        
        initViews(view);
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieAdapter = new MovieAdapter(getContext());
        recyclerView.setAdapter(movieAdapter);
    }
    
    public void updateMovies(List<Entry> movies) {
        if (movieAdapter != null) {
            // Filter only series
            movieAdapter.updateMovies(movies);
        }
    }
    
    public MovieAdapter getMovieAdapter() {
        return movieAdapter;
    }
}