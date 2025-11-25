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

    // 전체 목록 + 현재 화면에 보여줄 목록
    private final List<LightningPost> allItems = new ArrayList<>();
    private final List<LightningPost> displayItems = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUid;

    private Button btnCreateLightningFromList;
    private Button btnFilterAll;
    private Button btnFilterMine;
    private Button btnFilterJoined;

    // 필터 상태
    private static final int FILTER_ALL = 0;
    private static final int FILTER_MINE = 1;
    private static final int FILTER_JOINED = 2;

    private int currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_list);

        rvLightnings = findViewById(R.id.rvLightnings);
        btnCreateLightningFromList = findViewById(R.id.btnCreateLightningFromList);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterMine = findViewById(R.id.btnFilterMine);
        btnFilterJoined = findViewById(R.id.btnFilterJoined);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            currentUid = user.getUid();
        }

        adapter = new LightningAdapter(displayItems);
        rvLightnings.setLayoutManager(new LinearLayoutManager(this));
        rvLightnings.setAdapter(adapter);

        // 아이템 클릭 → 상세 화면으로 이동
        adapter.setOnItemClickListener(item -> {
            if (item.getId() == null || item.getId().isEmpty()) return;
            Intent intent = new Intent(
                    LightningListActivity.this,
                    LightningDetailActivity.class
            );
            intent.putExtra(LightningDetailActivity.EXTRA_LIGHTNING_ID, item.getId());
            startActivity(intent);
        });

        // 번개 생성 버튼 (루트 없이)
        btnCreateLightningFromList.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LightningListActivity.this,
                    LightningCreateActivity.class
            );
            startActivity(intent);
        });

        //  필터 버튼 리스너
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = FILTER_ALL;
            applyFilter();
            updateFilterButtonUI();
        });

        btnFilterMine.setOnClickListener(v -> {
            currentFilter = FILTER_MINE;
            applyFilter();
            updateFilterButtonUI();
        });

        btnFilterJoined.setOnClickListener(v -> {
            currentFilter = FILTER_JOINED;
            applyFilter();
            updateFilterButtonUI();
        });

        updateFilterButtonUI();
        subscribeLightnings();
    }

    // Firestore 실시간 구독
    private void subscribeLightnings() {
        db.collection("lightnings")
                .orderBy("eventTime", Query.Direction.ASCENDING) // 모임 시간 기준 정렬
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this,
                                "번개 목록 불러오기 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    allItems.clear();

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            LightningPost post = doc.toObject(LightningPost.class);
                            if (post != null) {
                                post.setId(doc.getId());
                                allItems.add(post);
                            }
                        }
                    }

                    // 일단 전체 목록 기준 필터 적용
                    applyFilter();

                    //  각 번개에 대해 참가자 요약 정보 로딩
                    for (LightningPost post : allItems) {
                        loadParticipantSummary(post);
                    }
                });
    }

    // participants 서브컬렉션에서 참가자 수 + 참여여부 로딩
    private void loadParticipantSummary(LightningPost post) {
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

                    // "참가한" 필터일 수도 있으니, 매번 필터 재적용
                    applyFilter();
                })
                .addOnFailureListener(err -> {
                    // 실패해도 조용히 무시해도 OK
                });
        String hostUidRaw = post.getHostUidRaw();  // 진짜 UID
        if (hostUidRaw == null || hostUidRaw.isEmpty()) return;

        db.collection("users")
                .document(hostUidRaw)
                .get()
                .addOnSuccessListener(userSnap -> {
                    if (userSnap != null && userSnap.exists()) {
                        String latestNick = userSnap.getString("nickname");
                        if (latestNick != null && !latestNick.isEmpty()) {
                            post.setHostNickname(latestNick);  // 닉네임 덮어쓰기

                            // 화면갱신
                            applyFilter();   // displayItems 다시 만들고 adapter.notifyDataSetChanged()
                        }
                    }
                });
    }

    // 현재 필터 displayItems 구성
    private void applyFilter() {
        displayItems.clear();

        if (currentFilter == FILTER_ALL) {
            displayItems.addAll(allItems);

        } else if (currentFilter == FILTER_MINE) {
            if (currentUid == null) {
            } else {
                for (LightningPost p : allItems) {
                    if (currentUid.equals(p.getHostUid())) {
                        displayItems.add(p);
                    }
                }
            }

        } else if (currentFilter == FILTER_JOINED) {
            if (currentUid == null) {
                // 로그인 안 했으면 참가한 번개도 없음
            } else {
                for (LightningPost p : allItems) {
                    if (p.isJoined()) {
                        displayItems.add(p);
                    }
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // 선택된 필터 버튼 시각적 구분
    private void updateFilterButtonUI() {
        float selectedAlpha = 1.0f;
        float unselectedAlpha = 0.5f;

        btnFilterAll.setAlpha(currentFilter == FILTER_ALL ? selectedAlpha : unselectedAlpha);
        btnFilterMine.setAlpha(currentFilter == FILTER_MINE ? selectedAlpha : unselectedAlpha);
        btnFilterJoined.setAlpha(currentFilter == FILTER_JOINED ? selectedAlpha : unselectedAlpha);
    }
}
