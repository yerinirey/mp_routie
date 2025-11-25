package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyRoutesActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode"; // "created" or "liked"

    private TextView tvMyRoutesTitle;
    private RecyclerView rvMyRoutes;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String currentUid;
    private String mode;

    private final List<RoutePost> items = new ArrayList<>();
    private RouteAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_routes);

        tvMyRoutesTitle = findViewById(R.id.tvMyRoutesTitle);
        rvMyRoutes = findViewById(R.id.rvMyRoutes);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUid = user.getUid();

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = "created";

        if ("liked".equals(mode)) {
            tvMyRoutesTitle.setText("좋아요한 루트");
        } else {
            tvMyRoutesTitle.setText("내가 만든 루트");
        }

        adapter = new RouteAdapter(items);
        rvMyRoutes.setLayoutManager(new LinearLayoutManager(this));
        rvMyRoutes.setAdapter(adapter);

        adapter.setOnItemClickListener(post -> {
            String routeId = post.getId();
            if (routeId == null || routeId.isEmpty()) {
                Toast.makeText(this, "루트 ID가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MyRoutesActivity.this, RouteDetailActivity.class);
            intent.putExtra(RouteDetailActivity.EXTRA_ROUTE_ID, routeId);
            startActivity(intent);
        });

        if ("liked".equals(mode)) {
            loadLikedRoutes();
        } else {
            loadCreatedRoutes();
        }
    }

    private void loadCreatedRoutes() {
        db.collection("routes")
                .whereEqualTo("hostUid", currentUid)
                .get()
                .addOnSuccessListener(snap -> {
                    items.clear();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap) {
                            RoutePost post = doc.toObject(RoutePost.class);
                            if (post != null) {
                                post.setId(doc.getId());
                                Long likeCountLong = doc.getLong("likeCount");
                                if (likeCountLong != null) {
                                    post.setLikeCount(likeCountLong.intValue());
                                }
                                items.add(post);
                            }
                        }
                    }
                    sortByCreatedAtDesc();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "내가 만든 루트 불러오기 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void loadLikedRoutes() {
        db.collection("routes")
                .get()
                .addOnSuccessListener(snap -> {
                    items.clear();
                    if (snap != null) {
                        List<DocumentSnapshot> routes = snap.getDocuments();
                        for (DocumentSnapshot routeDoc : routes) {
                            routeDoc.getReference()
                                    .collection("likes")
                                    .document(currentUid)
                                    .get()
                                    .addOnSuccessListener(likeDoc -> {
                                        if (likeDoc.exists()) {
                                            RoutePost post = routeDoc.toObject(RoutePost.class);
                                            if (post != null) {
                                                post.setId(routeDoc.getId());
                                                Long likeCountLong = routeDoc.getLong("likeCount");
                                                if (likeCountLong != null) {
                                                    post.setLikeCount(likeCountLong.intValue());
                                                }
                                                items.add(post);
                                                sortByCreatedAtDesc();
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this,
                                                    "좋아요 정보 불러오기 실패: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "루트 목록 불러오기 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void sortByCreatedAtDesc() {
        Collections.sort(items, (a, b) -> {
            long ca = a.getCreatedAt() != null ? a.getCreatedAt() : 0L;
            long cb = b.getCreatedAt() != null ? b.getCreatedAt() : 0L;
            return Long.compare(cb, ca);
        });
    }
}
