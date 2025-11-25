package com.example.mpteamproj;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LightningAdapter extends RecyclerView.Adapter<LightningAdapter.LightningViewHolder> {

    private final List<LightningPost> items;

    public interface OnItemClickListener {
        void onItemClick(LightningPost item);
    }

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
        LightningPost item = items.get(position);

        holder.tvTitle.setText(item.getTitle().isEmpty() ? "제목 없음" : item.getTitle());

        // 호스트
        String host = item.getHostUid().isEmpty()
                ? "호스트 미정"
                : item.getHostUid();

        // 모임 시간 우선, 없으면 생성 시간
        String timeText;
        if (item.getEventTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
            timeText = sdf.format(item.getEventTime());
        } else if (item.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
            timeText = sdf.format(item.getCreatedAt());
        } else {
            timeText = "미정";
        }

        holder.tvMeta.setText("호스트: " + host + " / 모임 시간: " + timeText);


        int count = item.getParticipantCount();
        int maxP = item.getMaxParticipants();
        if (maxP > 0) {
            holder.tvParticipants.setText("참가자: " + count + " / " + maxP + "명");
        } else {
            holder.tvParticipants.setText("참가자: " + count + "명");
        }

        if (item.isJoined()) {
            holder.tvJoinedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvJoinedBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LightningViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvMeta;
        TextView tvParticipants;
        TextView tvJoinedBadge;

        public LightningViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLightningItemTitle);
            tvMeta = itemView.findViewById(R.id.tvLightningItemMeta);
            tvParticipants = itemView.findViewById(R.id.tvLightningItemParticipants);
            tvJoinedBadge = itemView.findViewById(R.id.tvJoinedBadge);
        }
    }
}
