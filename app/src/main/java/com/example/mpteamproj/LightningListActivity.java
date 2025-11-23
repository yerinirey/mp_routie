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
import java.util.List;

public class LightningListActivity extends AppCompatActivity {

    private RecyclerView rvLightnings;
    private LightningAdapter adapter;
    private final List<LightningPost> items = new ArrayList<>();
    private FirebaseFirestore db;

    private Button btnCreateLightningFromList; // 🔹 추가

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_list);

        rvLightnings = findViewById(R.id.rvLightnings);
        btnCreateLightningFromList = findViewById(R.id.btnCreateLightningFromList); // 🔹

        adapter = new LightningAdapter(items);
        rvLightnings.setLayoutManager(new LinearLayoutManager(this));
        rvLightnings.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 번개 목록에서 아이템 클릭 → 상세로 이동
        adapter.setOnItemClickListener(item -> {
            if (item.getId() == null || item.getId().isEmpty()) return;
            Intent intent = new Intent(
                    LightningListActivity.this,
                    LightningDetailActivity.class
            );
            intent.putExtra(LightningDetailActivity.EXTRA_LIGHTNING_ID, item.getId());
            startActivity(intent);
        });

        // "번개 생성" 버튼 → 루트 없이 LightningCreateActivity 열기
        btnCreateLightningFromList.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LightningListActivity.this,
                    LightningCreateActivity.class
            );
            // routeId, routeTitle 같은 extra 안 넣음 → "연결된 루트 없음" 상태로 열림
            startActivity(intent);
        });

        subscribeLightnings();
    }

    private void subscribeLightnings() {
        db.collection("lightnings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this,
                                "번개 목록 불러오기 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    items.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            LightningPost post = doc.toObject(LightningPost.class);
                            if (post != null) {
                                post.setId(doc.getId());
                                items.add(post);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
