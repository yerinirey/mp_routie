package com.example.mpteamproj;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LightningCreateActivity extends AppCompatActivity {

    private EditText etLightningTitle;
    private EditText etLightningDescription;
    private EditText etLightningLocation;
    private TextView tvLinkedRoute;

    private TextView tvLightningEventTime;
    private Button btnSelectEventTime;
    private Button btnLightningSave;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String routeId;
    private String routeTitle;
    private String routeStart;
    private String routeEnd;

    // 🔹 선택된 모임 시간 (epoch millis)
    private long eventTimeMillis = -1L;

    private final SimpleDateFormat eventTimeFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_create);

        etLightningTitle = findViewById(R.id.etLightningTitle);
        etLightningDescription = findViewById(R.id.etLightningDescription);
        etLightningLocation = findViewById(R.id.etLightningLocation);
        tvLinkedRoute = findViewById(R.id.tvLinkedRoute);

        tvLightningEventTime = findViewById(R.id.tvLightningEventTime);
        btnSelectEventTime = findViewById(R.id.btnSelectEventTime);

        btnLightningSave = findViewById(R.id.btnLightningSave);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        // 🔹 모임 날짜/시간 선택 버튼
        btnSelectEventTime.setOnClickListener(v -> openDateTimePicker());

        btnLightningSave.setOnClickListener(v -> saveLightning());
    }

    private void openDateTimePicker() {
        final Calendar cal = Calendar.getInstance();

        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // 날짜 고른 뒤 시간 선택
                    TimePickerDialog tp = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                cal.set(Calendar.MINUTE, minute);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);

                                eventTimeMillis = cal.getTimeInMillis();
                                tvLightningEventTime.setText(
                                        eventTimeFormat.format(cal.getTime())
                                );
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                    );
                    tp.show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dp.show();
    }

    private void saveLightning() {
        String title = etLightningTitle.getText().toString().trim();
        String desc = etLightningDescription.getText().toString().trim();
        String location = etLightningLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "번개 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventTimeMillis <= 0) {
            Toast.makeText(this, "모임 날짜/시간을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String hostUid = user.getUid();
        String hostNickname = user.getDisplayName();
        if (TextUtils.isEmpty(hostNickname)) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                hostNickname = user.getEmail();
            } else {
                hostNickname = hostUid;
            }
        }
        final String finalHostNickname = hostNickname;

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", desc);
        data.put("hostUid", hostUid);
        data.put("hostNickname", finalHostNickname);
        data.put("createdAt", System.currentTimeMillis());

        // 모임 시간 저장
        data.put("eventTime", eventTimeMillis);

        if (!TextUtils.isEmpty(location)) {
            data.put("locationDesc", location);
        }

        if (!TextUtils.isEmpty(routeId)) {
            data.put("routeId", routeId);
            data.put("routeTitle", routeTitle);
            data.put("routeStart", routeStart);
            data.put("routeEnd", routeEnd);
        }

        db.collection("lightnings")
                .add(data)
                .addOnSuccessListener((DocumentReference ref) -> {
                    // 방장을 자동 참가자로 등록
                    Map<String, Object> participant = new HashMap<>();
                    participant.put("nickname", finalHostNickname);
                    participant.put("joinedAt", System.currentTimeMillis());

                    ref.collection("participants")
                            .document(hostUid)
                            .set(participant);

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
