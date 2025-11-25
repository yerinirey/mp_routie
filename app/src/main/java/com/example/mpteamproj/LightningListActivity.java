package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private Button btnCreateLightningFromList;

    // 현재 로그인 유저
    private FirebaseAuth auth;
    private String currentUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_list);

        rvLightnings = findViewById(R.id.rvLightnings);
        btnCreateLightningFromList = findViewById(R.id.btnCreateLightningFromList);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            currentUid = user.getUid();
        }

        adapter = new LightningAdapter(items);
        rvLightnings.setLayoutManager(new LinearLayoutManager(this));
        rvLightnings.setAdapter(adapter);

        // 아이템 클릭 → 상세로 이동
        adapter.setOnItemClickListener(item -> {
            if (item.getId() == null || item.getId().isEmpty()) return;
            Intent intent = new Intent(
                    LightningListActivity.this,
                    LightningDetailActivity.class
            );
            intent.putExtra(LightningDetailActivity.EXTRA_LIGHTNING_ID, item.getId());
            startActivity(intent);
        });

        // "번개 생성" 버튼 → 루트 없이 번개 생성
        btnCreateLightningFromList.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LightningListActivity.this,
                    LightningCreateActivity.class
            );
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

                    // 🔹 각 번개에 대해 참가자 요약 정보 로딩
                    for (int i = 0; i < items.size(); i++) {
                        LightningPost post = items.get(i);
                        loadParticipantSummary(post, i);
                    }
                });
    }

    // participants 컬렉션에서 참가자 수 + 참여 여부 확인
    private void loadParticipantSummary(LightningPost post, int position) {
        if (post.getId() == null || post.getId().isEmpty()) return;

        db.collection("lightnings")
                .document(post.getId())
                .collection("participants")
                .get()
                .addOnSuccessListener(snap -> {
                    int count = 0;
                    boolean joined = false;

                    if (snap != null) {
                        count = snap.size();
                        if (currentUid != null) {
                            for (DocumentSnapshot d : snap) {
                                if (currentUid.equals(d.getId())) {
                                    joined = true;
                                    break;
                                }
                            }
                        }
                    }

                    post.setParticipantCount(count);
                    post.setJoined(joined);

                    // 해당 아이템만 갱신
                    if (position >= 0 && position < items.size()) {
                        adapter.notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(err -> {
                    // 실패해도 리스트 전체는 사용 가능하니까 토스트 정도만
                    // (원하면 조용히 무시해도 됨)
                    // Toast.makeText(this, "참가자 정보 로딩 실패", Toast.LENGTH_SHORT).show();
                });
    }
}
