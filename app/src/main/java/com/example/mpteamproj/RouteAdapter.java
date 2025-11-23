package com.example.mpteamproj;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private final List<RoutePost> items;

    public RouteAdapter(List<RoutePost> items) {
        this.items = items;
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
        String info = post.getStartPlace() + " → " + post.getEndPlace();
        holder.tvInfo.setText(info);
        holder.tvMemo.setText(post.getMemo());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvInfo;
        TextView tvMemo;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRouteTitle);
            tvInfo = itemView.findViewById(R.id.tvRouteInfo);
            tvMemo = itemView.findViewById(R.id.tvRouteMemo);
        }
    }
}
