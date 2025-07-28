package com.cinecraze.free.stream.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinecraze.free.stream.MovieAdapter;
import com.cinecraze.free.stream.R;
import com.cinecraze.free.stream.models.Entry;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private MovieAdapter movieAdapter;
    private List<Entry> allMovies = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        initViews(view);
        setupSearch();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieAdapter = new MovieAdapter(getContext());
        recyclerView.setAdapter(movieAdapter);
    }
    
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterMovies(s.toString());
            }
        });
    }
    
    private void filterMovies(String query) {
        if (query.isEmpty()) {
            movieAdapter.updateMovies(allMovies);
            return;
        }
        
        List<Entry> filteredMovies = new ArrayList<>();
        for (Entry movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredMovies.add(movie);
            }
        }
        movieAdapter.updateMovies(filteredMovies);
    }
    
    public void updateMovies(List<Entry> movies) {
        this.allMovies = new ArrayList<>(movies);
        if (movieAdapter != null) {
            movieAdapter.updateMovies(movies);
        }
    }
    
    public MovieAdapter getMovieAdapter() {
        return movieAdapter;
    }
    
    public EditText getSearchEditText() {
        return searchEditText;
    }
}