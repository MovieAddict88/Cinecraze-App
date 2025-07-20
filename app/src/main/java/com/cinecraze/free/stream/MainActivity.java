package com.cinecraze.free.stream;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Entry> entryList = new ArrayList<>();
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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(movieAdapter);
    }

    private void setupCarousel() {
        carouselAdapter = new CarouselAdapter(this, new ArrayList<>());
        carouselViewPager.setAdapter(carouselAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                return true;
            }
        });
    }

    private void setupViewSwitch() {
        gridViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGridView = true;
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
                movieAdapter.setGridView(true);
                movieAdapter.notifyDataSetChanged();
            }
        });

        listViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGridView = false;
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                movieAdapter.setGridView(false);
                movieAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchPlaylist() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Playlist> call = apiService.getPlaylist();
        call.enqueue(new Callback<Playlist>() {
            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body().getCategories();
                    List<Entry> allEntries = new ArrayList<>();
                    for (Category category : categories) {
                        allEntries.addAll(category.getEntries());
                    }
                    entryList.clear();
                    entryList.addAll(allEntries);
                    movieAdapter.notifyDataSetChanged();

                    // For now, just use the first 5 entries for the carousel
                    List<Entry> carouselEntries = new ArrayList<>();
                    for (int i = 0; i < 5 && i < allEntries.size(); i++) {
                        carouselEntries.add(allEntries.get(i));
                    }
                    carouselAdapter.setEntries(carouselEntries);
                    carouselAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                // Handle failure
            }
        });
    }
}
