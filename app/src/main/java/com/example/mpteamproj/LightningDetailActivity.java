package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LightningDetailActivity extends AppCompatActivity {

    public static final String EXTRA_LIGHTNING_ID = "lightningId";

    private TextView tvLightningTitle;
    private TextView tvLightningMeta;
    private TextView tvLightningDescription;
    private TextView tvLinkedRouteInfo;
    private Button btnViewRoute;

    private FirebaseFirestore db;
    private String lightningId;

    private String routeId;
    private String routeTitle;
    private String routeStart;
    private String routeEnd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_detail);

        tvLightningTitle = findViewById(R.id.tvLightningTitle);
        tvLightningMeta = findViewById(R.id.tvLightningMeta);
        tvLightningDescription = findViewById(R.id.tvLightningDescription);
        tvLinkedRouteInfo = findViewById(R.id.tvLinkedRouteInfo);
        btnViewRoute = findViewById(R.id.btnViewRoute);

        db = FirebaseFirestore.getInstance();

        lightningId = getIntent().getStringExtra(EXTRA_LIGHTNING_ID);
        if (TextUtils.isEmpty(lightningId)) {
            Toast.makeText(this, "번개 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnViewRoute.setEnabled(false);
        btnViewRoute.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(routeId)) {
                Intent intent = new Intent(
                        LightningDetailActivity.this,
                        RouteDetailActivity.class
                );
                intent.putExtra(RouteDetailActivity.EXTRA_ROUTE_ID, routeId);
                startActivity(intent);
            }
        });

        loadLightning();
    }

    private void loadLightning() {
        db.collection("lightnings")
                .document(lightningId)
                .get()
                .addOnSuccessListener(this::onLightningLoaded)
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "번개 정보 불러오기 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onLightningLoaded(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "번개가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = safeString(doc.getString("title"));
        String desc = safeString(doc.getString("description"));
        String hostUid = safeString(doc.getString("hostUid"));
        String hostNickname = safeString(doc.getString("hostNickname"));  // 🔹 새 필드

        Long createdAt = null;
        Object createdRaw = doc.get("createdAt");
        if (createdRaw instanceof Number) {
            createdAt = ((Number) createdRaw).longValue();
        }

        routeId = safeString(doc.getString("routeId"));
        routeTitle = safeString(doc.getString("routeTitle"));
        routeStart = safeString(doc.getString("routeStart"));
        routeEnd = safeString(doc.getString("routeEnd"));

        tvLightningTitle.setText(title.isEmpty() ? "번개 상세" : title);

        String timeText;
        if (createdAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            timeText = sdf.format(new Date(createdAt));
        } else {
            timeText = "시간 정보 없음";
        }

        // 닉네임 > UID > "알 수 없음" 순으로 호스트 이름 결정
        String hostLabel;
        if (!hostNickname.isEmpty()) {
            hostLabel = hostNickname;
        } else if (!hostUid.isEmpty()) {
            hostLabel = hostUid;
        } else {
            hostLabel = "알 수 없음";
        }

        tvLightningMeta.setText("호스트: " + hostLabel
                + " / 생성 시각: " + timeText);

        tvLightningDescription.setText(
                desc.isEmpty() ? "설명이 없습니다." : desc
        );

        if (!TextUtils.isEmpty(routeId)) {
            StringBuilder sb = new StringBuilder();
            sb.append("연결된 루트: ");
            sb.append(routeTitle.isEmpty() ? routeId : routeTitle);
            if (!routeStart.isEmpty()) {
                sb.append("\n").append(routeStart);
            }
            if (!routeEnd.isEmpty()) {
                sb.append("\n").append(routeEnd);
            }
            tvLinkedRouteInfo.setText(sb.toString());
            btnViewRoute.setEnabled(true);
        } else {
            tvLinkedRouteInfo.setText("연결된 루트: 없음");
            btnViewRoute.setEnabled(false);
        }
    }

    private String safeString(String v) {
        return v != null ? v : "";
    }
}
