package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.List;

public class RouteListActivity extends AppCompatActivity {

    private TextView tvRouteListTitle;
    private RecyclerView rvRoutes;
    private Button btnRouteSortRecent;
    private Button btnRouteSortLikes;
    private Button btnGoCreateRoute;

    private FirebaseFirestore db;
    private final List<RoutePost> routeItems = new ArrayList<>();
    private RouteAdapter adapter;

    private String tagFilter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        tvRouteListTitle    = findViewById(R.id.tvRouteListTitle);
        rvRoutes            = findViewById(R.id.rvRoutes);
        btnRouteSortRecent  = findViewById(R.id.btnRouteSortRecent);
        btnRouteSortLikes   = findViewById(R.id.btnRouteSortLikes);
        btnGoCreateRoute    = findViewById(R.id.btnGoCreateRoute);

        db = FirebaseFirestore.getInstance();

        adapter = new RouteAdapter(routeItems);
        rvRoutes.setLayoutManager(new LinearLayoutManager(this));
        rvRoutes.setAdapter(adapter);

        adapter.setOnItemClickListener(post -> {
            String routeId = post.getId();
            if (TextUtils.isEmpty(routeId)) {
                Toast.makeText(this, "루트 ID가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(RouteListActivity.this, RouteDetailActivity.class);
            intent.putExtra(RouteDetailActivity.EXTRA_ROUTE_ID, routeId);
            startActivity(intent);
        });

        tagFilter = getIntent().getStringExtra("tagFilter");
        if (!TextUtils.isEmpty(tagFilter)) {
            tvRouteListTitle.setText("루트 둘러보기 (태그: " + tagFilter + ")");
        } else {
            tvRouteListTitle.setText("루트 둘러보기");
        }

        btnRouteSortRecent.setOnClickListener(v -> loadRoutesSortedByRecent());
        btnRouteSortLikes.setOnClickListener(v -> loadRoutesSortedByLikes());

        btnGoCreateRoute.setOnClickListener(v -> {
            Intent intent = new Intent(RouteListActivity.this, RouteCreateActivity.class);
            startActivity(intent);
        });

        loadRoutesSortedByRecent();
    }

    private void loadRoutesSortedByRecent() {
        Query query = db.collection("routes");

        if (TextUtils.isEmpty(tagFilter)) {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);

            query.get()
                    .addOnSuccessListener(snap -> {
                        routeItems.clear();
                        if (snap != null) {
                            for (DocumentSnapshot doc : snap) {
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
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "루트 불러오기 실패: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());

        } else {
            query = query.whereEqualTo("tag", tagFilter);

            query.get()
                    .addOnSuccessListener(snap -> {
                        routeItems.clear();
                        if (snap != null) {
                            for (DocumentSnapshot doc : snap) {
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

                        Collections.sort(routeItems, (a, b) -> {
                            long ca = a.getCreatedAt() != null ? a.getCreatedAt() : 0L;
                            long cb = b.getCreatedAt() != null ? b.getCreatedAt() : 0L;
                            return Long.compare(cb, ca); // 최신순
                        });

                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "루트 불러오기 실패: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }
    }

    private void loadRoutesSortedByLikes() {
        Query query = db.collection("routes");

        if (TextUtils.isEmpty(tagFilter)) {
            query = query.orderBy("likeCount", Query.Direction.DESCENDING);

            query.get()
                    .addOnSuccessListener(snap -> {
                        routeItems.clear();
                        if (snap != null) {
                            for (DocumentSnapshot doc : snap) {
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
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "루트 불러오기 실패: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());

        } else {
            query = query.whereEqualTo("tag", tagFilter);

            query.get()
                    .addOnSuccessListener(snap -> {
                        routeItems.clear();
                        if (snap != null) {
                            for (DocumentSnapshot doc : snap) {
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

                        Collections.sort(routeItems, (a, b) -> {
                            int la = a.getLikeCount();
                            int lb = b.getLikeCount();
                            return Integer.compare(lb, la); // 좋아요 많은 순
                        });

                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "루트 불러오기 실패: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }
    }
}
