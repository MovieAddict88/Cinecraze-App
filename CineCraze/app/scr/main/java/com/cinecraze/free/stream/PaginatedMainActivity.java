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
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cinecraze.free.stream.models.Category;
import com.cinecraze.free.stream.models.Entry;
import com.cinecraze.free.stream.models.Playlist;
import com.cinecraze.free.stream.net.ApiService;
import com.cinecraze.free.stream.net.RetrofitClient;
import com.cinecraze.free.stream.repository.DataRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaginatedMainActivity extends AppCompatActivity implements PaginatedMovieAdapter.PaginationListener {

    private RecyclerView recyclerView;
    private PaginatedMovieAdapter movieAdapter;
    private List<Entry> currentPageEntries = new ArrayList<>();
    private ViewPager2 carouselViewPager;
    private CarouselAdapter carouselAdapter;
    private ImageView gridViewIcon;
    private ImageView listViewIcon;
    private BottomNavigationView bottomNavigationView;
    private ImageView searchIcon;
    private ImageView closeSearchIcon;
    private LinearLayout titleLayout;
    private LinearLayout searchLayout;
    private AutoCompleteTextView searchBar;

    private boolean isGridView = true;
    private boolean isSearchVisible = false;
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 3;
    private DataRepository dataRepository;
    
    // Pagination variables
    private int currentPage = 0;
    private int pageSize = DataRepository.DEFAULT_PAGE_SIZE;
    private boolean hasMorePages = false;
    private int totalCount = 0;
    private String currentCategory = "";
    private String currentSearchQuery = "";
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up our custom toolbar (no default ActionBar since we use NoActionBar theme)
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Hide the default title since we have our custom title layout
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        recyclerView = findViewById(R.id.recycler_view);
        carouselViewPager = findViewById(R.id.carousel_view_pager);
        gridViewIcon = findViewById(R.id.grid_view_icon);
        listViewIcon = findViewById(R.id.list_view_icon);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchIcon = findViewById(R.id.search_icon);
        closeSearchIcon = findViewById(R.id.close_search_icon);
        titleLayout = findViewById(R.id.title_layout);
        searchLayout = findViewById(R.id.search_layout);
        searchBar = findViewById(R.id.search_bar);

        setupRecyclerView();
        setupCarousel();
        setupBottomNavigation();
        setupViewSwitch();
        setupSearchToggle();

        // Initialize repository
        dataRepository = new DataRepository(this);

        // Load initial data and first page
        loadInitialData();
    }

    private void setupRecyclerView() {
        movieAdapter = new PaginatedMovieAdapter(this, currentPageEntries, isGridView);
        movieAdapter.setPaginationListener(this);
        
        if (isGridView) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        recyclerView.setAdapter(movieAdapter);
    }

    private void setupCarousel() {
        carouselAdapter = new CarouselAdapter(this, new ArrayList<>(), new ArrayList<>());
        carouselViewPager.setAdapter(carouselAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item selection
                String category = "";
                if (item.getItemId() == R.id.nav_movies) {
                    category = "Movies";
                } else if (item.getItemId() == R.id.nav_series) {
                    category = "TV Shows";
                } else if (item.getItemId() == R.id.nav_home) {
                    category = "";
                } else if (item.getItemId() == R.id.nav_live) {
                    category = "Live";
                }
                
                filterByCategory(category);
                return true;
            }
        });
    }

    private void setupViewSwitch() {
        gridViewIcon.setOnClickListener(v -> {
            if (!isGridView) {
                isGridView = true;
                updateViewMode();
            }
        });

        listViewIcon.setOnClickListener(v -> {
            if (isGridView) {
                isGridView = false;
                updateViewMode();
            }
        });
    }

    private void updateViewMode() {
        movieAdapter.setGridView(isGridView);
        
        if (isGridView) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            gridViewIcon.setVisibility(View.GONE);
            listViewIcon.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            gridViewIcon.setVisibility(View.VISIBLE);
            listViewIcon.setVisibility(View.GONE);
        }
    }

    private void setupSearchToggle() {
        searchIcon.setOnClickListener(v -> showSearchBar());
        closeSearchIcon.setOnClickListener(v -> hideSearchBar());
        
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() > 2) {
                    performSearch(query);
                } else if (query.isEmpty()) {
                    clearSearch();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void showSearchBar() {
        try {
            if (!isSearchVisible && titleLayout != null && searchLayout != null) {
                titleLayout.setVisibility(View.GONE);
                searchLayout.setVisibility(View.VISIBLE);
                isSearchVisible = true;
                searchBar.requestFocus();
            }
        } catch (Exception e) {
            Log.e("PaginatedMainActivity", "Error showing search bar: " + e.getMessage(), e);
        }
    }

    private void hideSearchBar() {
        try {
            if (isSearchVisible && titleLayout != null && searchLayout != null) {
                searchLayout.setVisibility(View.GONE);
                titleLayout.setVisibility(View.VISIBLE);
                isSearchVisible = false;
                
                if (searchBar != null) {
                    searchBar.setText("");
                    searchBar.clearFocus();
                    android.view.inputmethod.InputMethodManager imm = 
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                    }
                }
                
                clearSearch();
            }
        } catch (Exception e) {
            Log.e("PaginatedMainActivity", "Error hiding search bar: " + e.getMessage(), e);
        }
    }

    private void performSearch(String query) {
        currentSearchQuery = query.trim();
        currentPage = 0;
        loadSearchResults();
    }

    private void clearSearch() {
        currentSearchQuery = "";
        currentPage = 0;
        loadPage();
    }

    private void filterByCategory(String category) {
        currentCategory = category;
        currentPage = 0;
        currentSearchQuery = "";
        loadPage();
    }

    private void loadInitialData() {
        Log.d("PaginatedMainActivity", "Loading initial data");
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        
        // First ensure we have data cached
        dataRepository.getPlaylistData(new DataRepository.DataCallback() {
            @Override
            public void onSuccess(List<Entry> entries) {
                // Data is now cached, load first page
                loadFirstPage();
                setupCarouselFromCache();
            }
            
            @Override
            public void onError(String error) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                Log.e("PaginatedMainActivity", "Error loading initial data: " + error);
                Toast.makeText(PaginatedMainActivity.this, "Failed to load data: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupCarouselFromCache() {
        // Setup carousel with first few items from cache
        List<Entry> allEntries = dataRepository.getAllCachedEntries();
        List<Entry> carouselEntries = new ArrayList<>();
        for (int i = 0; i < 5 && i < allEntries.size(); i++) {
            carouselEntries.add(allEntries.get(i));
        }
        
        // Recreate adapter with all entries for proper navigation
        carouselAdapter = new CarouselAdapter(this, carouselEntries, allEntries);
        carouselViewPager.setAdapter(carouselAdapter);
        carouselAdapter.notifyDataSetChanged();
    }

    private void loadFirstPage() {
        currentPage = 0;
        loadPage();
    }

    private void loadPage() {
        if (isLoading) return;
        
        isLoading = true;
        movieAdapter.setLoading(true);
        
        if (!currentSearchQuery.isEmpty()) {
            loadSearchResults();
        } else if (!currentCategory.isEmpty()) {
            loadCategoryPage();
        } else {
            loadAllEntriesPage();
        }
    }

    private void loadAllEntriesPage() {
        dataRepository.getPaginatedData(currentPage, pageSize, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                updatePageData(entries, hasMorePages, totalCount);
            }
            
            @Override
            public void onError(String error) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                handlePageLoadError(error);
            }
        });
    }

    private void loadCategoryPage() {
        dataRepository.getPaginatedDataByCategory(currentCategory, currentPage, pageSize, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                updatePageData(entries, hasMorePages, totalCount);
            }
            
            @Override
            public void onError(String error) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                handlePageLoadError(error);
            }
        });
    }

    private void loadSearchResults() {
        dataRepository.searchPaginated(currentSearchQuery, currentPage, pageSize, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                updatePageData(entries, hasMorePages, totalCount);
            }
            
            @Override
            public void onError(String error) {
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                handlePageLoadError(error);
            }
        });
    }

    private void updatePageData(List<Entry> entries, boolean hasMorePages, int totalCount) {
        this.hasMorePages = hasMorePages;
        this.totalCount = totalCount;
        this.isLoading = false;
        
        currentPageEntries.clear();
        currentPageEntries.addAll(entries);
        
        movieAdapter.setEntryList(currentPageEntries);
        movieAdapter.updatePaginationState(currentPage, hasMorePages, totalCount);
        
        // Scroll to top of the list
        recyclerView.scrollToPosition(0);
        
        Log.d("PaginatedMainActivity", "Loaded page " + currentPage + " with " + entries.size() + " items");
    }

    private void handlePageLoadError(String error) {
        isLoading = false;
        movieAdapter.setLoading(false);
        Log.e("PaginatedMainActivity", "Error loading page: " + error);
        Toast.makeText(this, "Failed to load page: " + error, Toast.LENGTH_SHORT).show();
    }

    // PaginationListener implementation
    @Override
    public void onPreviousPage() {
        if (currentPage > 0 && !isLoading) {
            currentPage--;
            loadPage();
        }
    }

    @Override
    public void onNextPage() {
        if (hasMorePages && !isLoading) {
            currentPage++;
            loadPage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if we need to refresh data
        if (currentPageEntries.isEmpty()) {
            loadInitialData();
        }
    }

    public void refreshData() {
        retryCount = 0;
        currentPage = 0;
        if (dataRepository != null) {
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            dataRepository.refreshData(new DataRepository.DataCallback() {
                @Override
                public void onSuccess(List<Entry> entries) {
                    loadFirstPage();
                    setupCarouselFromCache();
                    Toast.makeText(PaginatedMainActivity.this, "Data refreshed (" + dataRepository.getTotalEntriesCount() + " items)", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    Toast.makeText(PaginatedMainActivity.this, "Failed to refresh: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (isSearchVisible) {
                hideSearchBar();
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e("PaginatedMainActivity", "Error handling back press: " + e.getMessage(), e);
            super.onBackPressed();
        }
    }
}