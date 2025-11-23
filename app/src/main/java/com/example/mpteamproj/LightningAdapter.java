package com.example.mpteamproj;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LightningAdapter extends RecyclerView.Adapter<LightningAdapter.LightningViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(LightningPost item);
    }

    private final List<LightningPost> items;
    private OnItemClickListener listener;

    public LightningAdapter(List<LightningPost> items) {
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public LightningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lightning, parent, false);
        return new LightningViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LightningViewHolder holder, int position) {
        LightningPost post = items.get(position);
        holder.tvTitle.setText(post.getTitle());
        holder.tvDesc.setText(post.getDescription());

        if (post.getRouteId() != null && !post.getRouteId().isEmpty()) {
            holder.tvRouteTag.setText("루트 연결됨");
            holder.tvRouteTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvRouteTag.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LightningViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDesc;
        TextView tvRouteTag;

        LightningViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLightningItemTitle);
            tvDesc = itemView.findViewById(R.id.tvLightningItemDesc);
            tvRouteTag = itemView.findViewById(R.id.tvLightningItemRouteTag);
        }
    }
}
