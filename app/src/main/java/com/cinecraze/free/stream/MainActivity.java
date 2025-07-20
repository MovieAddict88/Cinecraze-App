package com.cinecraze.free.stream;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cinecraze.free.stream.models.Category;
import com.cinecraze.free.stream.models.Entry;
import com.cinecraze.free.stream.models.Playlist;
import com.cinecraze.free.stream.net.ApiService;
import com.cinecraze.free.stream.net.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Entry> entryList = new ArrayList<>();
    public List<Entry> allEntries = new ArrayList<>();
    private ViewPager2 carouselViewPager;
    private CarouselAdapter carouselAdapter;
    private ImageView gridViewIcon;
    private ImageView listViewIcon;
    private BottomNavigationView bottomNavigationView;

    private boolean isGridView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        carouselViewPager = findViewById(R.id.carousel_view_pager);
        gridViewIcon = findViewById(R.id.grid_view_icon);
        listViewIcon = findViewById(R.id.list_view_icon);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupRecyclerView();
        setupCarousel();
        setupBottomNavigation();
        setupViewSwitch();

        fetchPlaylist();
    }

    private void setupRecyclerView() {
        movieAdapter = new MovieAdapter(this, entryList, isGridView);
        if (isGridView) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        recyclerView.setAdapter(movieAdapter);
    }

    private void setupCarousel() {
        carouselAdapter = new CarouselAdapter(this, new ArrayList<>(), allEntries);
        carouselViewPager.setAdapter(carouselAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                filterEntries("");
            } else if (item.getItemId() == R.id.nav_movies) {
                filterEntries("Movies");
            } else if (item.getItemId() == R.id.nav_series) {
                filterEntries("TV Series");
            } else if (item.getItemId() == R.id.nav_live) {
                filterEntries("Live TV");
            }
            return true;
        });
    }

    private void setupViewSwitch() {
        gridViewIcon.setOnClickListener(v -> {
            isGridView = false;
            setupRecyclerView();
            gridViewIcon.setVisibility(View.GONE);
            listViewIcon.setVisibility(View.VISIBLE);
        });

        listViewIcon.setOnClickListener(v -> {
            isGridView = true;
            setupRecyclerView();
            listViewIcon.setVisibility(View.GONE);
            gridViewIcon.setVisibility(View.VISIBLE);
        });
    }

    private void filterEntries(String category) {
        List<Entry> filteredEntries = new ArrayList<>();
        if (category.isEmpty()) {
            filteredEntries.addAll(allEntries);
        } else {
            for (Category cat : playlistCache.getCategories()) {
                if (cat.getMainCategory().equalsIgnoreCase(category)) {
                    filteredEntries.addAll(cat.getEntries());
                }
            }
        }
        entryList.clear();
        entryList.addAll(filteredEntries);
        movieAdapter.notifyDataSetChanged();
    }

    private Playlist playlistCache;

    private void fetchPlaylist() {
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Playlist> call = apiService.getPlaylist();
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    playlistCache = response.body();
                    allEntries.clear();
                    for (Category category : playlistCache.getCategories()) {
                        allEntries.addAll(category.getEntries());
                    }
                    filterEntries(""); // Show all entries initially

                    // For now, just use the first 5 entries for the carousel
                    List<Entry> carouselEntries = new ArrayList<>();
                    for (int i = 0; i < 5 && i < allEntries.size(); i++) {
                        carouselEntries.add(allEntries.get(i));
                    }
                    carouselAdapter.setEntries(carouselEntries);
                    carouselAdapter.notifyDataSetChanged();

                    setupSearch();
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                // Handle failure
            }
        });
    }

    private void setupSearch() {
        AutoCompleteTextView searchBar = findViewById(R.id.search_bar);
        List<String> titles = new ArrayList<>();
        for (Entry entry : allEntries) {
            titles.add(entry.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        searchBar.setAdapter(adapter);

        searchBar.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = (String) parent.getItemAtPosition(position);
            for (Entry entry : allEntries) {
                if (entry.getTitle().equals(selectedTitle)) {
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra("entry", new Gson().toJson(entry));
                    startActivity(intent);
                    break;
                }
            }
        });
    }
}
