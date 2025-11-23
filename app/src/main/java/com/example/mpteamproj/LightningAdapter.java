package com.example.mpteamproj;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mpteamproj.R;

import java.util.List;

public class LightningAdapter extends RecyclerView.Adapter<LightningAdapter.LightningViewHolder> {

    private final List<com.example.mpteamproj.LightningPost> items;

    public LightningAdapter(List<com.example.mpteamproj.LightningPost> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public LightningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lightning, parent, false);
        return new LightningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LightningViewHolder holder, int position) {
        com.example.mpteamproj.LightningPost post = items.get(position);
        holder.tvTitle.setText(post.getTitle());
        holder.tvInfo.setText(post.getPlaceAndTime());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LightningViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvInfo;

        public LightningViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLightningTitle);
            tvInfo = itemView.findViewById(R.id.tvLightningInfo);
        }
    }
}
