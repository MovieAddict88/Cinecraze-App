package com.cinecraze.free.stream;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

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
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 3;

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

        // Only fetch data if we don't have it already
        if (allEntries.isEmpty()) {
            fetchPlaylist();
        }
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
            if (playlistCache != null && playlistCache.getCategories() != null) {
                for (Category cat : playlistCache.getCategories()) {
                    if (cat != null && cat.getMainCategory() != null && 
                        cat.getMainCategory().equalsIgnoreCase(category) && 
                        cat.getEntries() != null) {
                        filteredEntries.addAll(cat.getEntries());
                    }
                }
            }
        }
        entryList.clear();
        entryList.addAll(filteredEntries);
        movieAdapter.notifyDataSetChanged();
    }

    private Playlist playlistCache;

    private void fetchPlaylist() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("MainActivity", "Fetching playlist data...");
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Playlist> call = apiService.getPlaylist();
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                Log.d("MainActivity", "Response received: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    playlistCache = response.body();
                    allEntries.clear();
                    
                    if (playlistCache.getCategories() != null) {
                        for (Category category : playlistCache.getCategories()) {
                            if (category != null && category.getEntries() != null) {
                                allEntries.addAll(category.getEntries());
                            }
                        }
                    }
                    
                    if (allEntries.isEmpty()) {
                        Toast.makeText(MainActivity.this, "No data available", Toast.LENGTH_LONG).show();
                        return;
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
                    retryCount = 0; // Reset retry count on success
                    Log.d("MainActivity", "Data loaded successfully with " + allEntries.size() + " items");
                    Toast.makeText(MainActivity.this, "Data loaded successfully (" + allEntries.size() + " items)", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("MainActivity", "Failed to load data: " + response.code());
                    Toast.makeText(MainActivity.this, "Failed to load data: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                Log.e("MainActivity", "Network failure: " + t.getMessage(), t);
                
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    Log.d("MainActivity", "Retrying... Attempt " + retryCount + "/" + MAX_RETRY_COUNT);
                    Toast.makeText(MainActivity.this, "Retrying... Attempt " + retryCount + "/" + MAX_RETRY_COUNT, Toast.LENGTH_SHORT).show();
                    // Retry after a short delay
                    new android.os.Handler().postDelayed(() -> fetchPlaylist(), 2000);
                } else {
                    Log.e("MainActivity", "Failed to load data after " + MAX_RETRY_COUNT + " attempts");
                    Toast.makeText(MainActivity.this, "Failed to load data after " + MAX_RETRY_COUNT + " attempts", Toast.LENGTH_LONG).show();
                    retryCount = 0; // Reset for next manual retry
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if we need to refresh data (e.g., if we came back from background)
        if (allEntries.isEmpty() && isNetworkAvailable()) {
            fetchPlaylist();
        }
    }

    private void setupSearch() {
        AutoCompleteTextView searchBar = findViewById(R.id.search_bar);
        List<String> titles = new ArrayList<>();
        for (Entry entry : allEntries) {
            if (entry != null && entry.getTitle() != null) {
                titles.add(entry.getTitle());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titles);
        searchBar.setAdapter(adapter);

        searchBar.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = (String) parent.getItemAtPosition(position);
            for (Entry entry : allEntries) {
                if (entry != null && entry.getTitle() != null && entry.getTitle().equals(selectedTitle)) {
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra("entry", new Gson().toJson(entry));
                    startActivity(intent);
                    break;
                }
            }
        });
    }
}
