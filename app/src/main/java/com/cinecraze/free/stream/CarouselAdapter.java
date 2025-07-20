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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private Context context;
    private List<Entry> entryList;

    public CarouselAdapter(Context context, List<Entry> entryList) {
        this.context = context;
        this.entryList = entryList;
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
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        TextView country;
        TextView year;
        TextView duration;
        FloatingActionButton playButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
            country = itemView.findViewById(R.id.country);
            year = itemView.findViewById(R.id.year);
            duration = itemView.findViewById(R.id.duration);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }
}
