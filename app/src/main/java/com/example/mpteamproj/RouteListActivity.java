package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RouteListActivity extends AppCompatActivity {

    private RecyclerView rvRoutes;
    private RouteAdapter adapter;

    private Button btnRouteSortRecent;
    private Button btnRouteSortLikes;

    private FirebaseFirestore db;

    private final List<RoutePost> routeItems = new ArrayList<>();

    private static final int SORT_RECENT = 0;
    private static final int SORT_LIKES  = 1;
    private int currentSort = SORT_RECENT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        rvRoutes = findViewById(R.id.rvRoutes);
        btnRouteSortRecent = findViewById(R.id.btnRouteSortRecent);
        btnRouteSortLikes  = findViewById(R.id.btnRouteSortLikes);

        db = FirebaseFirestore.getInstance();

        adapter = new RouteAdapter(routeItems);
        rvRoutes.setLayoutManager(new LinearLayoutManager(this));
        rvRoutes.setAdapter(adapter);

        // 아이템 클릭 → RouteDetailActivity로 이동 (기존 코드 사용)
        adapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(RouteListActivity.this, RouteDetailActivity.class);
            intent.putExtra(RouteDetailActivity.EXTRA_ROUTE_ID, post.getId());
            startActivity(intent);
        });

        btnRouteSortRecent.setOnClickListener(v -> {
            currentSort = SORT_RECENT;
            applySort();
            updateSortButtonUI();
        });

        btnRouteSortLikes.setOnClickListener(v -> {
            currentSort = SORT_LIKES;
            applySort();
            updateSortButtonUI();
        });

        updateSortButtonUI();
        subscribeRoutes();
    }

    private void subscribeRoutes() {
        db.collection("routes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this,
                                "루트 목록 불러오기 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    routeItems.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            RoutePost post = doc.toObject(RoutePost.class);
                            if (post != null) {
                                post.setId(doc.getId());

                                Long likeCountLong = doc.getLong("likeCount");
                                if (likeCountLong != null) {
                                    post.setLikeCount(likeCountLong.intValue());
                                }

                                routeItems.add(post);
                            }
                        }
                    }
                    applySort();
                });
    }

    private void applySort() {
        if (currentSort == SORT_LIKES) {
            Collections.sort(routeItems,
                    (a, b) -> Integer.compare(b.getLikeCount(), a.getLikeCount()));
        } else {
            // createdAt 기준 최신순
            Collections.sort(routeItems, new Comparator<RoutePost>() {
                @Override
                public int compare(RoutePost a, RoutePost b) {
                    Long ca = a.getCreatedAt();
                    Long cb = b.getCreatedAt();
                    long va = (ca != null) ? ca : 0L;
                    long vb = (cb != null) ? cb : 0L;
                    return Long.compare(vb, va);
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    private void updateSortButtonUI() {
        float selected  = 1.0f;
        float unselected = 0.5f;
        btnRouteSortRecent.setAlpha(currentSort == SORT_RECENT ? selected : unselected);
        btnRouteSortLikes.setAlpha(currentSort == SORT_LIKES  ? selected : unselected);
    }
}
