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

    private final List<Entry> suggestions;

    public SearchSuggestionAdapter(@NonNull Context context, @NonNull List<Entry> suggestions) {
        super(context, 0, suggestions);
        this.suggestions = suggestions;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_search_suggestion, parent, false);
        }

        Entry entry = suggestions.get(position);

        ImageView imageView = convertView.findViewById(R.id.suggestion_image);
        TextView titleView = convertView.findViewById(R.id.suggestion_title);

        titleView.setText(entry.getTitle());

        Glide.with(getContext())
                .load(entry.getPoster())
                .placeholder(R.drawable.image_placeholder)
                .into(imageView);

        return convertView;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Nullable
    @Override
    public Entry getItem(int position) {
        return suggestions.get(position);
    }
}
