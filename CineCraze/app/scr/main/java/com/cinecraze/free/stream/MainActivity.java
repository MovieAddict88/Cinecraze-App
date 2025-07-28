package com.cinecraze.free.stream;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.cinecraze.free.stream.fragments.HomeFragment;
import com.cinecraze.free.stream.fragments.LiveTvFragment;
import com.cinecraze.free.stream.fragments.MoviesFragment;
import com.cinecraze.free.stream.fragments.SearchFragment;
import com.cinecraze.free.stream.fragments.SeriesFragment;
import com.cinecraze.free.stream.models.Category;
import com.cinecraze.free.stream.models.Entry;
import com.cinecraze.free.stream.models.Playlist;
import com.cinecraze.free.stream.net.ApiService;
import com.cinecraze.free.stream.net.RetrofitClient;
import com.cinecraze.free.stream.repository.DataRepository;
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.cinecraze.free.stream.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MainActivity with Bottom Navigation and Custom Search Bar
 * Based on MovieAddict88/CineCrazeFetch implementation
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    // UI Components - Bottom Navigation
    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;
    private BubbleNavigationConstraintView bubbleNavigationView;
    
    // Fragments
    private HomeFragment homeFragment;
    private MoviesFragment moviesFragment;
    private SeriesFragment seriesFragment;
    private LiveTvFragment liveTvFragment;
    private SearchFragment searchFragment;
    
    // Search Components
    private RelativeLayout searchSection;
    private EditText searchEditText;
    private ImageView searchButton, closeSearchButton, backButton;
    private Toolbar toolbar;
    
    // Data
    private List<Entry> allMovies = new ArrayList<>();
    private List<Entry> carouselMovies = new ArrayList<>();
    
    // Repository
    private DataRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initRepository();
        initBottomNavigation();
        initSearchFunctionality();
        loadInitialData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("CineCraze");
        }
        
        // ViewPager setup
        viewPager = findViewById(R.id.vp_horizontal_ntb);
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(true);
        
        // Search components
        searchSection = findViewById(R.id.relative_layout_home_activity_search_section);
        searchEditText = findViewById(R.id.edit_text_home_activity_search);
        searchButton = findViewById(R.id.image_view_activity_home_search);
        closeSearchButton = findViewById(R.id.image_view_activity_home_close_search);
        backButton = findViewById(R.id.image_view_activity_actors_back);
        
        // Bottom Navigation
        bubbleNavigationView = findViewById(R.id.top_navigation_constraint);
    }

    private void initRepository() {
        repository = new DataRepository(this);
    }

    private void initBottomNavigation() {
        // Set initial position
        viewPager.setCurrentItem(0);
        bubbleNavigationView.setCurrentActiveItem(0);
        
        // ViewPager change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bubbleNavigationView.setCurrentActiveItem(position);
            }
        });
        
        // Bottom navigation change listener
        bubbleNavigationView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                viewPager.setCurrentItem(position, true);
                
                // Update fragment data based on selection
                updateFragmentData(position);
            }
        });
    }

    private void initSearchFunctionality() {
        // Back button to close search
        backButton.setOnClickListener(v -> {
            searchSection.setVisibility(View.GONE);
            searchEditText.setText("");
        });
        
        // Search input listener
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                performSearch();
                return true;
            }
            return false;
        });
        
        // Close search button
        closeSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
        });
        
        // Search button
        searchButton.setOnClickListener(v -> {
            performSearch();
        });
        
        // Show search when Search tab is selected (position 4)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 4) { // Search tab
                    showSearchSection();
                } else {
                    hideSearchSection();
                }
            }
        });
    }

    private void showSearchSection() {
        searchSection.setVisibility(View.VISIBLE);
        searchEditText.requestFocus();
    }

    private void hideSearchSection() {
        searchSection.setVisibility(View.GONE);
        searchEditText.setText("");
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            // Filter movies based on search query
            List<Entry> searchResults = new ArrayList<>();
            for (Entry movie : allMovies) {
                if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    searchResults.add(movie);
                }
            }
            
            // Update SearchFragment with results
            SearchFragment currentSearchFragment = getCurrentSearchFragment();
            if (currentSearchFragment != null) {
                currentSearchFragment.updateMovies(searchResults);
            }
            
            Toast.makeText(this, "Found " + searchResults.size() + " results", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFragmentData(int position) {
        List<Entry> filteredData = new ArrayList<>();
        
        switch (position) {
            case 0: // Home - Show all
                filteredData = allMovies;
                HomeFragment homeFragment = getCurrentHomeFragment();
                if (homeFragment != null) {
                    homeFragment.updateMovies(filteredData);
                    homeFragment.updateCarousel(carouselMovies);
                }
                break;
            case 1: // Movies - Filter movies only
                for (Entry entry : allMovies) {
                    if ("movie".equalsIgnoreCase(entry.getType())) {
                        filteredData.add(entry);
                    }
                }
                MoviesFragment moviesFragment = getCurrentMoviesFragment();
                if (moviesFragment != null) {
                    moviesFragment.updateMovies(filteredData);
                }
                break;
            case 2: // Series - Filter series only
                for (Entry entry : allMovies) {
                    if ("series".equalsIgnoreCase(entry.getType())) {
                        filteredData.add(entry);
                    }
                }
                SeriesFragment seriesFragment = getCurrentSeriesFragment();
                if (seriesFragment != null) {
                    seriesFragment.updateMovies(filteredData);
                }
                break;
            case 3: // Live TV - Filter live streams
                for (Entry entry : allMovies) {
                    if ("live".equalsIgnoreCase(entry.getType())) {
                        filteredData.add(entry);
                    }
                }
                LiveTvFragment liveTvFragment = getCurrentLiveTvFragment();
                if (liveTvFragment != null) {
                    liveTvFragment.updateMovies(filteredData);
                }
                break;
            case 4: // Search - Show all but let search fragment handle filtering
                SearchFragment searchFragment = getCurrentSearchFragment();
                if (searchFragment != null) {
                    searchFragment.updateMovies(allMovies);
                }
                break;
        }
    }

    private void loadInitialData() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load playlist data
        loadPlaylistData();
    }

    private void loadPlaylistData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Playlist> call = apiService.getPlaylist();
        
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(@NonNull Call<Playlist> call, @NonNull Response<Playlist> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Playlist playlist = response.body();
                    processPlaylistData(playlist);
                } else {
                    Log.e(TAG, "Failed to load playlist: " + response.code());
                    Toast.makeText(MainActivity.this, "Failed to load content", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Playlist> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processPlaylistData(Playlist playlist) {
        allMovies.clear();
        carouselMovies.clear();
        
        if (playlist.getCategories() != null) {
            for (Category category : playlist.getCategories()) {
                if (category.getStreams() != null) {
                    allMovies.addAll(category.getStreams());
                }
            }
        }
        
        // Get first 5 items for carousel
        int carouselSize = Math.min(5, allMovies.size());
        carouselMovies.addAll(allMovies.subList(0, carouselSize));
        
        // Update current fragment
        updateFragmentData(viewPager.getCurrentItem());
        
        Log.d(TAG, "Loaded " + allMovies.size() + " items");
    }

    // Helper methods to get current fragments
    private HomeFragment getCurrentHomeFragment() {
        try {
            return (HomeFragment) getSupportFragmentManager().findFragmentByTag("f0");
        } catch (Exception e) {
            return null;
        }
    }

    private MoviesFragment getCurrentMoviesFragment() {
        try {
            return (MoviesFragment) getSupportFragmentManager().findFragmentByTag("f1");
        } catch (Exception e) {
            return null;
        }
    }

    private SeriesFragment getCurrentSeriesFragment() {
        try {
            return (SeriesFragment) getSupportFragmentManager().findFragmentByTag("f2");
        } catch (Exception e) {
            return null;
        }
    }

    private LiveTvFragment getCurrentLiveTvFragment() {
        try {
            return (LiveTvFragment) getSupportFragmentManager().findFragmentByTag("f3");
        } catch (Exception e) {
            return null;
        }
    }

    private SearchFragment getCurrentSearchFragment() {
        try {
            return (SearchFragment) getSupportFragmentManager().findFragmentByTag("f4");
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        if (searchSection.getVisibility() == View.VISIBLE) {
            hideSearchSection();
        } else if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }
}