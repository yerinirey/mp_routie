package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class LightningListActivity extends AppCompatActivity {

    private RecyclerView rvLightning;
    private Button btnGoCreateLightning;

    private FirebaseFirestore db;
    private LightningAdapter adapter;
    private List<LightningPost> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_list);

        rvLightning = findViewById(R.id.rvLightning);
        btnGoCreateLightning = findViewById(R.id.btnGoCreateLightning);

        db = FirebaseFirestore.getInstance();

        rvLightning.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LightningAdapter(items);
        rvLightning.setAdapter(adapter);

        btnGoCreateLightning.setOnClickListener(v -> {
            Intent intent = new Intent(LightningListActivity.this, LightningCreateActivity.class);
            startActivity(intent);
        });

        // 실시간으로 번개 목록 구독
        subscribeLightningPosts();
    }

    private void subscribeLightningPosts() {
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
                        snapshots.forEach(doc -> {
                            LightningPost post = doc.toObject(LightningPost.class);
                            post.setId(doc.getId());
                            items.add(post);
                        });
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
