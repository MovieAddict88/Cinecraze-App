package com.cinecraze.free.stream;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinecraze.free.stream.models.Entry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private Context context;
    private List<Entry> entryList;
    private List<Entry> allEntries;

    public CarouselAdapter(Context context, List<Entry> entryList, List<Entry> allEntries) {
        this.context = context;
        this.entryList = entryList;
        this.allEntries = allEntries;
    }

    public void setEntries(List<Entry> entries) {
        this.entryList = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = entryList.get(position);

        holder.title.setText(entry.getTitle());
        holder.country.setText(entry.getCountry());
        holder.year.setText(String.valueOf(entry.getYear()));
        holder.duration.setText(entry.getDuration());
        Glide.with(context).load(entry.getPoster()).into(holder.poster);
        
        // Set category badge (on poster)
        setCategoryBadge(holder.categoryBadge, entry.getSubCategory());
        
        // Set type badge (below info)
        setTypeBadge(holder.typeBadge, entry.getSubCategory());

        holder.playButton.setOnClickListener(v -> {
            DetailsActivity.start(context, entry, allEntries);
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
        background.setCornerRadius(6 * context.getResources().getDisplayMetrics().density);
        badge.setBackground(background);
    }
    
    private void setTypeBadge(TextView badge, String category) {
        String badgeText;
        int badgeColor;
        
        if (category == null || category.trim().isEmpty()) {
            category = "Other";
        }
        
        // Determine badge text and color based on category
        if (category.toLowerCase().contains("movie") || category.toLowerCase().contains("film")) {
            badgeText = "MOVIE";
            badgeColor = ContextCompat.getColor(context, R.color.type_badge_movies);
        } else if (category.toLowerCase().contains("series") || category.toLowerCase().contains("tv")) {
            badgeText = "SERIES";
            badgeColor = ContextCompat.getColor(context, R.color.type_badge_series);
        } else if (category.toLowerCase().contains("live")) {
            badgeText = "LIVE";
            badgeColor = ContextCompat.getColor(context, R.color.type_badge_live);
        } else {
            badgeText = category.toUpperCase();
            if (badgeText.length() > 6) {
                badgeText = badgeText.substring(0, 6);
            }
            badgeColor = ContextCompat.getColor(context, R.color.type_badge_default);
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
        TextView country;
        TextView year;
        TextView duration;
        TextView categoryBadge;
        TextView typeBadge;
        FloatingActionButton playButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
            country = itemView.findViewById(R.id.country);
            year = itemView.findViewById(R.id.year);
            duration = itemView.findViewById(R.id.duration);
            categoryBadge = itemView.findViewById(R.id.category_badge);
            typeBadge = itemView.findViewById(R.id.type_badge);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }
}
