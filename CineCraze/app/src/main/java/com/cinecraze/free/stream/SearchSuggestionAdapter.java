package com.cinecraze.free.stream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.cinecraze.free.stream.models.Entry;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionAdapter extends ArrayAdapter<Entry> {
    private final LayoutInflater inflater;
    private final int resource;
    private final List<Entry> allEntries;
    private final List<Entry> filteredEntries;

    public SearchSuggestionAdapter(@NonNull Context context, @NonNull List<Entry> entries) {
        super(context, 0, new ArrayList<>(entries));
        this.inflater = LayoutInflater.from(context);
        this.resource = R.layout.item_search_suggestion;
        this.allEntries = new ArrayList<>(entries);
        this.filteredEntries = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return filteredEntries.size();
    }

    @Nullable
    @Override
    public Entry getItem(int position) {
        return filteredEntries.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                filteredEntries.clear();
                if (constraint != null && constraint.length() > 1) {
                    String query = constraint.toString().toLowerCase();
                    for (Entry entry : allEntries) {
                        if (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(query)) {
                            filteredEntries.add(entry);
                            if (filteredEntries.size() >= 10) break;
                        }
                    }
                }
                results.values = filteredEntries;
                results.count = filteredEntries.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }

    public void updateAllEntries(List<Entry> newEntries) {
        allEntries.clear();
        allEntries.addAll(newEntries);
        filteredEntries.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createSuggestionView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createSuggestionView(position, convertView, parent);
    }

    private View createSuggestionView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(resource, parent, false);
        }

        Entry entry = getItem(position);
        ImageView imagePoster = view.findViewById(R.id.image_poster);
        TextView textTitle = view.findViewById(R.id.text_title);

        if (entry != null) {
            textTitle.setText(entry.getTitle());
            String imageUrl = entry.getPoster() != null ? entry.getPoster() : entry.getThumbnail();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .into(imagePoster);
            } else {
                imagePoster.setImageResource(R.drawable.image_placeholder); // fallback image
            }
        }

        return view;
    }
}