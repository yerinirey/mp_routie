package com.example.mpteamproj;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LightningCreateActivity extends AppCompatActivity {

    private EditText etLightningTitle;
    private EditText etLightningDescription;
    private TextView tvLinkedRoute;
    private Button btnLightningSave;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // route 정보 (있을 수도, 없을 수도 있음)
    private String routeId;
    private String routeTitle;
    private String routeStart;
    private String routeEnd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_create);

        etLightningTitle = findViewById(R.id.etLightningTitle);
        etLightningDescription = findViewById(R.id.etLightningDescription);
        tvLinkedRoute = findViewById(R.id.tvLinkedRoute);
        btnLightningSave = findViewById(R.id.btnLightningSave);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // RouteDetailActivity 에서 넘어온 값들 (없을 수도 있음)
        routeId = getIntent().getStringExtra("routeId");
        routeTitle = getIntent().getStringExtra("routeTitle");
        routeStart = getIntent().getStringExtra("routeStart");
        routeEnd = getIntent().getStringExtra("routeEnd");

        if (!TextUtils.isEmpty(routeId) && !TextUtils.isEmpty(routeTitle)) {
            tvLinkedRoute.setText("연결된 루트: " + routeTitle);
            if (TextUtils.isEmpty(etLightningTitle.getText().toString().trim())) {
                etLightningTitle.setText(routeTitle + " 번개");
            }
        } else {
            tvLinkedRoute.setText("연결된 루트 없음");
        }

        btnLightningSave.setOnClickListener(v -> saveLightning());
    }

    private void saveLightning() {
        String title = etLightningTitle.getText().toString().trim();
        String desc = etLightningDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "번개 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String hostUid = user.getUid();

        // 🔹 회원가입 때 RegisterActivity에서 넣어준 displayName = 닉네임
        String hostNickname = user.getDisplayName();
        if (hostNickname == null || hostNickname.isEmpty()) {
            // 혹시 displayName이 비어있는 옛 계정이면 fallback
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                hostNickname = user.getEmail();
            } else {
                hostNickname = hostUid;
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", desc);
        data.put("hostUid", hostUid);           // UID
        data.put("hostNickname", hostNickname); // 🔹 닉네임
        data.put("createdAt", System.currentTimeMillis());

        if (!TextUtils.isEmpty(routeId)) {
            data.put("routeId", routeId);
            data.put("routeTitle", routeTitle);
            data.put("routeStart", routeStart);
            data.put("routeEnd", routeEnd);
        }

        db.collection("lightnings")
                .add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "번개가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "번개 생성 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
