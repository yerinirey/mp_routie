package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class RouteListActivity extends AppCompatActivity {

    private RecyclerView rvRoutes;
    private Button btnGoCreateRoute;

    private FirebaseFirestore db;
    private RouteAdapter adapter;
    private List<RoutePost> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        rvRoutes = findViewById(R.id.rvRoutes);
        btnGoCreateRoute = findViewById(R.id.btnGoCreateRoute);

        db = FirebaseFirestore.getInstance();

        rvRoutes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RouteAdapter(items);
        rvRoutes.setAdapter(adapter);

        btnGoCreateRoute.setOnClickListener(v -> {
            Intent intent = new Intent(RouteListActivity.this, RouteCreateActivity.class);
            startActivity(intent);
        });

        subscribeRoutes();
    }

    private void subscribeRoutes() {
        db.collection("routes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("RouteListActivity", "Listen failed", e);
                        Toast.makeText(this,
                                "루트 목록 불러오기 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    items.clear();

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            try {
                                RoutePost post = new RoutePost();
                                post.setId(doc.getId());

                                // 문자열 필드들
                                post.setTitle(safeString(doc.getString("title")));
                                post.setStartPlace(safeString(doc.getString("startPlace")));
                                post.setEndPlace(safeString(doc.getString("endPlace")));
                                post.setMemo(safeString(doc.getString("memo")));
                                post.setHostUid(safeString(doc.getString("hostUid")));

                                // createdAt은 Long 또는 Timestamp 둘 다 받을 수 있게 처리
                                Object createdRaw = doc.get("createdAt");
                                Long createdAt = 0L;

                                if (createdRaw instanceof Long) {
                                    createdAt = (Long) createdRaw;
                                } else if (createdRaw instanceof Timestamp) {
                                    createdAt = ((Timestamp) createdRaw).toDate().getTime();
                                } else if (createdRaw == null) {
                                    // 필드가 아예 없는 경우도 방어
                                    createdAt = 0L;
                                }

                                post.setCreatedAt(createdAt);

                                items.add(post);

                            } catch (Exception ex) {
                                Log.e("RouteListActivity",
                                        "문서 파싱 오류 id=" + doc.getId(), ex);
                                // 여기서 그냥 continue 해서 나머지 문서들은 계속 읽게 함
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // null이면 빈 문자열로 처리하는 헬퍼
    private String safeString(String value) {
        return value != null ? value : "";
    }
}
