package com.cinecraze.free.stream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cinecraze.free.stream.models.Entry;
import com.cinecraze.free.stream.repository.DataRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private String query;
    private SwipeRefreshLayout swipe_refresh_layout_list_search_search;
    private Button button_try_again;
    private LinearLayout linear_layout_layout_error;
    private RecyclerView recycler_view_activity_search;
    private ImageView image_view_empty_list;
    private GridLayoutManager gridLayoutManager;
    private MovieAdapter adapter;

    private boolean loading = true;
    private ArrayList<Entry> entryArrayList = new ArrayList<>();
    private LinearLayout linear_layout_load_search_activity;

    private boolean tabletSize;
    private DataRepository dataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        initAction();
        loadSearchResults();
    }

    private void initView() {
        tabletSize = getResources().getBoolean(R.bool.isTablet);
        
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            this.query = bundle.getString("query");
        }
        
        // Handle empty query case
        if (query == null || query.trim().isEmpty()) {
            query = "";
        }
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            if (query.isEmpty()) {
                toolbar.setTitle("Search");
            } else {
                toolbar.setTitle(query);
            }
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        this.linear_layout_load_search_activity = findViewById(R.id.linear_layout_load_search_activity);
        this.swipe_refresh_layout_list_search_search = findViewById(R.id.swipe_refresh_layout_list_search_search);
        button_try_again = findViewById(R.id.button_try_again);
        image_view_empty_list = findViewById(R.id.image_view_empty_list);
        linear_layout_layout_error = findViewById(R.id.linear_layout_layout_error);
        recycler_view_activity_search = findViewById(R.id.recycler_view_activity_search);
        
        adapter = new MovieAdapter(this, entryArrayList, true); // Use grid view by default
        recycler_view_activity_search.setHasFixedSize(true);
        recycler_view_activity_search.setAdapter(adapter);
        
        // Initialize repository
        dataRepository = new DataRepository(this);
    }

    private void loadSearchResults() {
        if (query == null || query.trim().isEmpty()) {
            showEmptyState();
            return;
        }
        
        swipe_refresh_layout_list_search_search.setRefreshing(false);
        linear_layout_load_search_activity.setVisibility(View.VISIBLE);
        
        // Search using the existing DataRepository
        dataRepository.searchPaginated(query, 0, 50, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount) {
                entryArrayList.clear();
                entryArrayList.addAll(entries);
                
                setupGridLayout();
                
                if (entryArrayList.size() == 0) {
                    showEmptyState();
                } else {
                    showResults();
                }
                
                linear_layout_load_search_activity.setVisibility(View.GONE);
                swipe_refresh_layout_list_search_search.setRefreshing(false);
                adapter.notifyDataSetChanged();
                
                Log.d("SearchActivity", "Search completed: " + entries.size() + " results for '" + query + "'");
            }

            @Override
            public void onError(String error) {
                showErrorState();
                linear_layout_load_search_activity.setVisibility(View.GONE);
                swipe_refresh_layout_list_search_search.setRefreshing(false);
                Log.e("SearchActivity", "Search error: " + error);
            }
        });
    }
    
    private void setupGridLayout() {
        if (tabletSize) {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 6, RecyclerView.VERTICAL, false);
        } else {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3, RecyclerView.VERTICAL, false);
        }
        recycler_view_activity_search.setLayoutManager(gridLayoutManager);
    }
    
    private void showResults() {
        linear_layout_layout_error.setVisibility(View.GONE);
        recycler_view_activity_search.setVisibility(View.VISIBLE);
        image_view_empty_list.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        linear_layout_layout_error.setVisibility(View.GONE);
        recycler_view_activity_search.setVisibility(View.GONE);
        image_view_empty_list.setVisibility(View.VISIBLE);
    }
    
    private void showErrorState() {
        linear_layout_layout_error.setVisibility(View.VISIBLE);
        recycler_view_activity_search.setVisibility(View.GONE);
        image_view_empty_list.setVisibility(View.GONE);
    }

    private void initAction() {
        swipe_refresh_layout_list_search_search.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loading = true;
                entryArrayList.clear();
                adapter.notifyDataSetChanged();
                loadSearchResults();
            }
        });
        
        if (button_try_again != null) {
            button_try_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loading = true;
                    entryArrayList.clear();
                    adapter.notifyDataSetChanged();
                    loadSearchResults();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem itemMenu) {
        switch (itemMenu.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(itemMenu);
    }
}