package com.cinecraze.free.stream;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cinecraze.free.stream.models.Entry;
import com.cinecraze.free.stream.repository.DataRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    
    private String query;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout errorLayout;
    private LinearLayout loadingLayout;
    private RecyclerView recyclerView;
    private ImageView emptyImageView;
    private ProgressBar loadMoreProgressBar;
    
    private GridLayoutManager gridLayoutManager;
    private MovieAdapter adapter;
    private DataRepository dataRepository;
    
    private int currentPage = 1;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    
    private ArrayList<Entry> entryList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        // Get search query from intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            this.query = bundle.getString("query", "");
        }
        
        dataRepository = new DataRepository(this);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupRefreshLayout();
        
        if (!query.isEmpty()) {
            loadSearchResults();
        }
    }
    
    private void initViews() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_search);
        errorLayout = findViewById(R.id.linear_layout_error);
        loadingLayout = findViewById(R.id.linear_layout_loading);
        recyclerView = findViewById(R.id.recycler_view_search);
        emptyImageView = findViewById(R.id.image_view_empty);
        loadMoreProgressBar = findViewById(R.id.progress_bar_load_more);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(query);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        
        adapter = new MovieAdapter(entryList, this, MovieAdapter.VIEW_TYPE_GRID);
        recyclerView.setAdapter(adapter);
        
        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = gridLayoutManager.getChildCount();
                int totalItemCount = gridLayoutManager.getItemCount();
                int pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();
                
                if (!isLoading && hasMoreData && 
                    (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    loadMoreResults();
                }
            }
        });
    }
    
    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            entryList.clear();
            adapter.notifyDataSetChanged();
            loadSearchResults();
        });
        
        swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.netflix_red
        );
    }
    
    private void loadSearchResults() {
        if (query.isEmpty()) return;
        
        showLoading(true);
        isLoading = true;
        
        dataRepository.searchPaginated(query, currentPage, pageSize, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMore) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isLoading = false;
                    hasMoreData = hasMore;
                    
                    if (currentPage == 1) {
                        entryList.clear();
                    }
                    
                    entryList.addAll(entries);
                    adapter.notifyDataSetChanged();
                    
                    if (entryList.isEmpty()) {
                        showEmptyState();
                    } else {
                        showContent();
                    }
                    
                    Log.d("SearchActivity", "Search '" + query + "' page " + currentPage + ": " + entries.size() + " results");
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isLoading = false;
                    
                    if (currentPage == 1) {
                        showError();
                    }
                    
                    Log.e("SearchActivity", "Search error: " + error);
                });
            }
        });
    }
    
    private void loadMoreResults() {
        if (isLoading || !hasMoreData) return;
        
        loadMoreProgressBar.setVisibility(View.VISIBLE);
        isLoading = true;
        currentPage++;
        
        dataRepository.searchPaginated(query, currentPage, pageSize, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMore) {
                runOnUiThread(() -> {
                    loadMoreProgressBar.setVisibility(View.GONE);
                    isLoading = false;
                    hasMoreData = hasMore;
                    
                    entryList.addAll(entries);
                    adapter.notifyDataSetChanged();
                    
                    Log.d("SearchActivity", "Load more '" + query + "' page " + currentPage + ": " + entries.size() + " results");
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    loadMoreProgressBar.setVisibility(View.GONE);
                    isLoading = false;
                    currentPage--; // Revert page increment on error
                    
                    Log.e("SearchActivity", "Load more error: " + error);
                });
            }
        });
    }
    
    private void showLoading(boolean show) {
        swipeRefreshLayout.setRefreshing(show);
        loadingLayout.setVisibility(show && currentPage == 1 ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show && currentPage == 1 ? View.GONE : View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
    }
    
    private void showContent() {
        loadingLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
    }
    
    private void showError() {
        loadingLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        emptyImageView.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        loadingLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}