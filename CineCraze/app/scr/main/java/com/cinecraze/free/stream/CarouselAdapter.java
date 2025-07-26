package com.cinecraze.free.stream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinecraze.free.stream.models.Entry;
import com.google.android.material.button.MaterialButton;

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

        // Set text content
        holder.title.setText(entry.getTitle());
        holder.description.setText(entry.getDescription());
        
        // Set rating if available
        if (entry.getRating() > 0) {
            holder.ratingText.setText(String.format("%.1f", entry.getRating()));
            holder.ratingText.setVisibility(View.VISIBLE);
        } else {
            holder.ratingText.setVisibility(View.GONE);
        }

        // Load background image using the existing ImageUtils
        ImageUtils.loadBannerImage(context, entry.getPoster(), holder.backgroundImage);

        // Set up click listeners
        holder.playButton.setOnClickListener(v -> {
            DetailsActivity.start(context, entry, allEntries);
        });

        holder.infoButton.setOnClickListener(v -> {
            DetailsActivity.start(context, entry, allEntries);
        });

        // Make the entire item clickable
        holder.itemView.setOnClickListener(v -> {
            DetailsActivity.start(context, entry, allEntries);
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView backgroundImage;
        TextView title;
        TextView description;
        TextView ratingText;
        MaterialButton playButton;
        MaterialButton infoButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            backgroundImage = itemView.findViewById(R.id.background_image);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            ratingText = itemView.findViewById(R.id.rating_text);
            playButton = itemView.findViewById(R.id.btn_play);
            infoButton = itemView.findViewById(R.id.btn_info);
        }
    }
}
