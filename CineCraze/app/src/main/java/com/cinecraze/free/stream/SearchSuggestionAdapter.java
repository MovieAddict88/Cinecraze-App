package com.cinecraze.free.stream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.cinecraze.free.stream.models.Entry;

import java.util.List;

public class SearchSuggestionAdapter extends ArrayAdapter<Entry> {
    private final LayoutInflater inflater;
    private final int resource;

    public SearchSuggestionAdapter(@NonNull Context context, @NonNull List<Entry> entries) {
        super(context, 0, entries);
        this.inflater = LayoutInflater.from(context);
        this.resource = R.layout.item_search_suggestion;
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