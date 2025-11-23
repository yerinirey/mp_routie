package com.example.mpteamproj;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LightningCreateActivity extends AppCompatActivity {

    private EditText etLightningTitle;
    private EditText etLightningPlace;
    private EditText etLightningTime;
    private Button btnLightningSubmit;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_create);

        etLightningTitle = findViewById(R.id.etLightningTitle);
        etLightningPlace = findViewById(R.id.etLightningPlace);
        etLightningTime = findViewById(R.id.etLightningTime);
        btnLightningSubmit = findViewById(R.id.btnLightningSubmit);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLightningSubmit.setOnClickListener(v -> {

            String title = etLightningTitle.getText().toString().trim();
            String place = etLightningPlace.getText().toString().trim();
            String timeText = etLightningTime.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(place) || TextUtils.isEmpty(timeText)) {
                Toast.makeText(this, "모든 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("place", place);
            data.put("timeText", timeText);
            data.put("hostUid", uid);
            data.put("createdAt", System.currentTimeMillis());

            db.collection("lightnings")
                    .add(data)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "번개가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        finish(); // 목록으로 돌아가기
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "등록 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
