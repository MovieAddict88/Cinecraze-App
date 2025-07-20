package com.cinecraze.free.stream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        if (!isGridView) {
            holder.description.setText(entry.getDescription());
            holder.year.setText(String.valueOf(entry.getYear()));
            holder.country.setText(entry.getCountry());
            holder.duration.setText(entry.getDuration());
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("entry", new Gson().toJson(entry));
            if (context instanceof MainActivity) {
                intent.putExtra("allEntries", new Gson().toJson(((MainActivity) context).allEntries));
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        RatingBar rating;
        TextView description;
        TextView year;
        TextView country;
        TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
            rating = itemView.findViewById(R.id.rating);

            if (!isGridView) {
                description = itemView.findViewById(R.id.description);
                year = itemView.findViewById(R.id.year);
                country = itemView.findViewById(R.id.country);
                duration = itemView.findViewById(R.id.duration);
            }
        }
    }
}
