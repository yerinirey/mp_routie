package com.example.mpteamproj;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RoutePost item);
    }

    private final List<RoutePost> items;
    private OnItemClickListener listener;

    public RouteAdapter(List<RoutePost> items) {
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        RoutePost post = items.get(position);

        holder.tvTitle.setText(post.getTitle());

        String startText;
        if (!TextUtils.isEmpty(post.getStartLabel())) {
            startText = post.getStartLabel();
        } else if (!TextUtils.isEmpty(post.getStartPlace())) {
            startText = post.getStartPlace();
        } else {
            startText = "출발 미지정";
        }

        String endText;
        if (!TextUtils.isEmpty(post.getEndLabel())) {
            endText = post.getEndLabel();
        } else if (!TextUtils.isEmpty(post.getEndPlace())) {
            endText = post.getEndPlace();
        } else {
            endText = "도착 미지정";
        }

        holder.tvInfo.setText(startText + " → " + endText);
        holder.tvMemo.setText(post.getMemo());

        holder.tvLikes.setText("🧡 " + post.getLikeCount());

        if (!TextUtils.isEmpty(post.getTag())) {
            holder.tvTag.setText("태그: " + post.getTag());
            holder.tvTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvTag.setVisibility(View.GONE);
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

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvInfo;
        TextView tvMemo;
        TextView tvLikes;
        TextView tvTag;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRouteTitle);
            tvInfo  = itemView.findViewById(R.id.tvRouteInfo);
            tvMemo  = itemView.findViewById(R.id.tvRouteMemo);
            tvLikes = itemView.findViewById(R.id.tvRouteLikes);
            tvTag   = itemView.findViewById(R.id.tvRouteTag);
        }
    }
}
