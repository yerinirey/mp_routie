package com.example.mpteamproj;

import android.graphics.PointF;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.Poi;
import com.kakao.vectormap.camera.CameraUpdate;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RouteCreateActivity extends AppCompatActivity {

    private EditText etRouteTitle;
    private TextView tvStartPoint, tvEndPoint;
    private Button btnSelectStart, btnSelectEnd, btnSaveRoute;
    private MapView mapView;
    private KakaoMap kakaoMap;

    private LatLng startLatLng;
    private LatLng endLatLng;
    private Label startLabel;
    private Label endLabel;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private enum SelectMode { NONE, START, END }
    private SelectMode currentMode = SelectMode.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_create);

        etRouteTitle = findViewById(R.id.etRouteTitle);
        tvStartPoint = findViewById(R.id.tvStartPoint);
        tvEndPoint = findViewById(R.id.tvEndPoint);
        btnSelectStart = findViewById(R.id.btnSelectStart);
        btnSelectEnd = findViewById(R.id.btnSelectEnd);
        btnSaveRoute = findViewById(R.id.btnSaveRoute);
        mapView = findViewById(R.id.mapView);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 지도 시작
        initMap();

        // 시작/도착 선택 모드 버튼
        btnSelectStart.setOnClickListener(v -> {
            currentMode = SelectMode.START;
            Toast.makeText(this, "지도를 탭해서 시작 지점을 선택하세요.", Toast.LENGTH_SHORT).show();
        });

        btnSelectEnd.setOnClickListener(v -> {
            currentMode = SelectMode.END;
            Toast.makeText(this, "지도를 탭해서 도착 지점을 선택하세요.", Toast.LENGTH_SHORT).show();
        });

        // 루트 저장 버튼
        btnSaveRoute.setOnClickListener(v -> saveRoute());
    }

    private void initMap() {
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                // 지도 정상 종료 시
            }

            @Override
            public void onMapError(@NonNull Exception error) {
                Toast.makeText(RouteCreateActivity.this,
                        "지도 초기화 실패: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;

                // 기본 위치 : 서울 시청 근처
                LatLng seoul = LatLng.from(37.5665, 126.9780);
                CameraUpdate cameraUpdate =
                        CameraUpdateFactory.newCenterPosition(seoul, 10);
                kakaoMap.moveCamera(cameraUpdate);

                // 지도 클릭 리스너
                kakaoMap.setOnMapClickListener(new KakaoMap.OnMapClickListener() {
                    @Override
                    public void onMapClicked(KakaoMap map,
                                             LatLng position,
                                             PointF screenPoint,
                                             Poi poi) {
                        handleMapClick(position);
                    }
                });
            }
        });
    }

    private void handleMapClick(LatLng position) {
        if (currentMode == SelectMode.NONE) {
            Toast.makeText(this,
                    "먼저 '시작 지점 선택' 또는 '도착 지점 선택' 버튼을 눌러주세요.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (kakaoMap == null) return;

        if (currentMode == SelectMode.START) {
            startLatLng = position;
            tvStartPoint.setText(
                    String.format(Locale.getDefault(),
                            "시작 지점: %.6f, %.6f",
                            position.latitude, position.longitude)
            );
        } else if (currentMode == SelectMode.END) {
            endLatLng = position;
            tvEndPoint.setText(
                    String.format(Locale.getDefault(),
                            "도착 지점: %.6f, %.6f",
                            position.latitude, position.longitude)
            );
        }

        drawMarkers();
    }

    // 지도 위에 마커(라벨) 찍기
    private void drawMarkers() {
        if (kakaoMap == null || kakaoMap.getLabelManager() == null) return;

        LabelLayer layer = kakaoMap.getLabelManager().getLayer();
        // 기존 라벨 제거
        layer.removeAll();

        if (startLatLng != null) {
            LabelOptions startOptions = LabelOptions
                    .from(startLatLng)
                    // 시스템 기본 아이콘 사용
                    .setStyles(android.R.drawable.ic_menu_mylocation);
            startLabel = layer.addLabel(startOptions);
        }

        if (endLatLng != null) {
            LabelOptions endOptions = LabelOptions
                    .from(endLatLng)
                    .setStyles(android.R.drawable.ic_menu_mylocation);
            endLabel = layer.addLabel(endOptions);
        }
    }

    private void saveRoute() {
        String title = etRouteTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "루트 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startLatLng == null || endLatLng == null) {
            Toast.makeText(this, "시작/도착 지점을 모두 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid()
                : "anonymous";

        Map<String, Object> route = new HashMap<>();
        route.put("title", title);
        route.put("startLat", startLatLng.latitude);
        route.put("startLng", startLatLng.longitude);
        route.put("endLat", endLatLng.latitude);
        route.put("endLng", endLatLng.longitude);

        // 문자열 버전도 같이 저장하고 싶으면 (리스트 화면에서 쓰기 좋음)
        route.put("startPlace", tvStartPoint.getText().toString());
        route.put("endPlace", tvEndPoint.getText().toString());

        route.put("memo", "");           // 메모 아직 안 쓰면 그냥 빈 문자열
        route.put("hostUid", uid);       // 필드 이름 hostUid로 통일
        route.put("createdAt", System.currentTimeMillis());

        db.collection("routes")
                .add(route)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this,
                            "루트가 저장되었습니다.",
                            Toast.LENGTH_SHORT).show();
                    finish(); // 이전 화면으로 돌아가기
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "루트 저장 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.resume();
    }

    @Override
    protected void onPause() {
        if (mapView != null) mapView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) mapView.finish();
        super.onDestroy();
    }
}
