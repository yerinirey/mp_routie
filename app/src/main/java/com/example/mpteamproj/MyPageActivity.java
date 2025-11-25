package com.example.mpteamproj;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyPageActivity extends AppCompatActivity {

    private static final int REQ_LOCATION_PERMISSION = 1001;

    private TextView tvCurrentEmail;
    private TextView tvCurrentNickname;
    private EditText etNewNickname;
    private Button btnSaveNickname;

    private TextView tvCurrentTown;
    private Button btnRefreshLocation;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        tvCurrentEmail = findViewById(R.id.tvCurrentEmail);
        tvCurrentNickname = findViewById(R.id.tvCurrentNickname);
        etNewNickname = findViewById(R.id.etNewNickname);
        btnSaveNickname = findViewById(R.id.btnSaveNickname);

        tvCurrentTown = findViewById(R.id.tvCurrentTown);
        btnRefreshLocation = findViewById(R.id.btnRefreshLocation);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 이메일 / 닉네임 초기 표시
        String email = currentUser.getEmail();
        tvCurrentEmail.setText("이메일: " + (email != null ? email : "알 수 없음"));

        String nickname = currentUser.getDisplayName();
        if (TextUtils.isEmpty(nickname)) {
            // displayName이 없으면 이메일을 기본으로 사용
            nickname = (email != null) ? email : currentUser.getUid();
        }
        tvCurrentNickname.setText(nickname);
        etNewNickname.setText(nickname);

        // 닉네임 저장 버튼
        btnSaveNickname.setOnClickListener(v -> saveNickname());

        // 위치 새로고침 버튼
        btnRefreshLocation.setOnClickListener(v -> refreshLocation());
    }

    //  닉네임 저장
    private void saveNickname() {
        String newNick = etNewNickname.getText().toString().trim();
        if (TextUtils.isEmpty(newNick)) {
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfileChangeRequest profileUpdates =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(newNick)
                        .build();

        currentUser.updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    tvCurrentNickname.setText(newNick);

                    // Firestore users/{uid}에도 반영 (있으면 merge)
                    String uid = currentUser.getUid();
                    Map<String, Object> data = new HashMap<>();
                    data.put("nickname", newNick);

                    db.collection("users")
                            .document(uid)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(MyPageActivity.this,
                                            "닉네임이 저장되었습니다.",
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(MyPageActivity.this,
                                            "닉네임 저장은 되었지만 프로필 동기화 실패: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(MyPageActivity.this,
                                "닉네임 변경 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // 위치 새로고침
    private void refreshLocation() {
        // 권한 체크
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION_PERMISSION
            );
            return;
        }

        // 권한이 이미 있을 때
        fetchLastLocation();
    }

    private void fetchLastLocation() {
        try {
            Task<Location> task = fusedLocationClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location == null) {
                    Toast.makeText(this,
                            "마지막 위치 정보를 가져올 수 없습니다.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                double lat = location.getLatitude();
                double lng = location.getLongitude();

                String townText = getTownNameFromLatLng(lat, lng);
                tvCurrentTown.setText(townText);
            }).addOnFailureListener(e -> {
                Toast.makeText(this,
                        "위치 정보를 가져오는 중 오류: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        } catch (SecurityException se) {
            Toast.makeText(this,
                    "위치 권한이 없습니다.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // 좌표 → "xx도 xx시 xx동" 텍스트로 변환
    private String getTownNameFromLatLng(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        try {
            List<Address> list = geocoder.getFromLocation(lat, lng, 1);
            if (list == null || list.isEmpty()) {
                return "동네를 알 수 없습니다.";
            }
            Address addr = list.get(0);

            String admin = addr.getAdminArea();      // 도
            String city = addr.getLocality();        // 시
            String gu = addr.getSubLocality();       // 구 (보통)
            String dong = addr.getThoroughfare();    // 동 or 도로명

            // 기본: 도/시
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(admin)) sb.append(admin).append(" ");
            if (!TextUtils.isEmpty(city)) sb.append(city).append(" ");

            // 구/동까지 붙여보기
            if (!TextUtils.isEmpty(gu)) sb.append(gu).append(" ");
            if (!TextUtils.isEmpty(dong)) sb.append(dong);

            String result = sb.toString().trim();
            if (TextUtils.isEmpty(result)) {
                return "동네를 알 수 없습니다.";
            }
            return result;

        } catch (IOException e) {
            return "동네 정보 변환 실패";
        }
    }

    // 권한 요청 결과 콜백
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용 → 다시 위치 요청
                fetchLastLocation();
            } else {
                Toast.makeText(this,
                        "위치 권한이 거부되어 동네를 표시할 수 없습니다.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
