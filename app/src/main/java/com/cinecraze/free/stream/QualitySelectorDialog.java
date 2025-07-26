package com.cinecraze.free.stream;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cinecraze.free.stream.models.Server;

import java.util.List;

public class QualitySelectorDialog extends DialogFragment {

    private List<Server> servers;
    private OnQualitySelectedListener listener;
    private int currentServerIndex;

    public interface OnQualitySelectedListener {
        void onQualitySelected(Server server, int position);
    }

    public static QualitySelectorDialog newInstance(List<Server> servers, int currentServerIndex) {
        QualitySelectorDialog dialog = new QualitySelectorDialog();
        dialog.servers = servers;
        dialog.currentServerIndex = currentServerIndex;
        return dialog;
    }

    public void setOnQualitySelectedListener(OnQualitySelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Quality");

        // Create adapter for server list
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), 
                android.R.layout.simple_list_item_1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                
                if (position < servers.size()) {
                    Server server = servers.get(position);
                    textView.setText(server.getName());
                    
                    // Highlight current selection
                    if (position == currentServerIndex) {
                        textView.setTextColor(requireContext().getResources().getColor(android.R.color.holo_blue_dark));
                        textView.setBackgroundColor(requireContext().getResources().getColor(android.R.color.holo_blue_light));
                    } else {
                        textView.setTextColor(requireContext().getResources().getColor(android.R.color.black));
                        textView.setBackgroundColor(requireContext().getResources().getColor(android.R.color.transparent));
                    }
                }
                
                return view;
            }
        };

        // Add server names to adapter
        for (Server server : servers) {
            adapter.add(server.getName());
        }

        builder.setAdapter(adapter, (dialog, which) -> {
            if (listener != null && which < servers.size()) {
                listener.onQualitySelected(servers.get(which), which);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}