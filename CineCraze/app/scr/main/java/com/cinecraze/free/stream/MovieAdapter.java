package com.cinecraze.free.stream;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.Intent;
import com.bumptech.glide.Glide;
import com.cinecraze.free.stream.models.Entry;
import com.google.gson.Gson;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private Context context;
    private List<Entry> entryList;
    private boolean isGridView;

    public MovieAdapter(Context context, List<Entry> entryList, boolean isGridView) {
        this.context = context;
        this.entryList = entryList;
        this.isGridView = isGridView;
    }

    public void setGridView(boolean gridView) {
        isGridView = gridView;
    }

    public void setEntryList(List<Entry> entryList) {
        this.entryList = entryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isGridView) {
            view = LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = entryList.get(position);

        holder.title.setText(entry.getTitle());
        Glide.with(context).load(entry.getPoster()).into(holder.poster);
        holder.rating.setRating(entry.getRating());

        if (holder.description != null) {
            holder.description.setText(entry.getDescription());
        }
        if (holder.year != null) {
            holder.year.setText(String.valueOf(entry.getYear()));
        }
        if (holder.country != null) {
            holder.country.setText(entry.getCountry());
        }
        if (holder.duration != null) {
            holder.duration.setText(entry.getDuration());
        }
        
        // Set category badge
        if (holder.categoryBadge != null) {
            setCategoryBadge(holder.categoryBadge, entry.getSubCategory());
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("entry", new Gson().toJson(entry));
            // For backward compatibility - pass the current entry list
            intent.putExtra("allEntries", new Gson().toJson(entryList));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    private void setCategoryBadge(TextView badge, String category) {
        String badgeText;
        int badgeColor;
        
        if (category == null || category.trim().isEmpty()) {
            category = "Other";
        }
        
        // Determine badge text and color based on category
        if (category.toLowerCase().contains("movie") || category.toLowerCase().contains("film")) {
            badgeText = "MOVIE";
            badgeColor = ContextCompat.getColor(context, R.color.badge_movies);
        } else if (category.toLowerCase().contains("series") || category.toLowerCase().contains("tv")) {
            badgeText = "SERIES";
            badgeColor = ContextCompat.getColor(context, R.color.badge_series);
        } else if (category.toLowerCase().contains("live") || category.toLowerCase().contains("tv")) {
            badgeText = "LIVE TV";
            badgeColor = ContextCompat.getColor(context, R.color.badge_live_tv);
        } else {
            badgeText = category.toUpperCase();
            if (badgeText.length() > 8) {
                badgeText = badgeText.substring(0, 8);
            }
            badgeColor = ContextCompat.getColor(context, R.color.badge_default);
        }
        
        badge.setText(badgeText);
        
        // Create colored background
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(badgeColor);
        background.setCornerRadius(4 * context.getResources().getDisplayMetrics().density);
        badge.setBackground(background);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        RatingBar rating;
        TextView description;
        TextView year;
        TextView country;
        TextView duration;
        TextView categoryBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
            rating = itemView.findViewById(R.id.rating);
            description = itemView.findViewById(R.id.description);
            year = itemView.findViewById(R.id.year);
            country = itemView.findViewById(R.id.country);
            duration = itemView.findViewById(R.id.duration);
            categoryBadge = itemView.findViewById(R.id.category_badge);
        }
    }
}
