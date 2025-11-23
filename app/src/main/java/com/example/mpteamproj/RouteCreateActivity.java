package com.example.mpteamproj;

import android.graphics.PointF;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdate;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.shape.MapPoints;
import com.kakao.vectormap.shape.Polyline;
import com.kakao.vectormap.shape.PolylineOptions;
import com.kakao.vectormap.shape.ShapeLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RouteCreateActivity extends AppCompatActivity {

    private EditText etRouteTitle;
    private TextView tvPointCount;
    private TextView tvStartPoint;
    private TextView tvEndPoint;
    private Button btnUndoPoint;
    private Button btnClearPoints;
    private Button btnSaveRoute;
    private MapView mapView;

    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;
    private ShapeLayer shapeLayer;
    private Polyline routePolyline;

    // 사용자가 지도에서 찍은 지점들(출발 + 경유 + 도착)
    private final List<LatLng> routePoints = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_create);

        // View 바인딩 (XML id와 1:1 매칭)
        etRouteTitle = findViewById(R.id.etRouteTitle);
        tvPointCount = findViewById(R.id.tvPointCount);
        tvStartPoint = findViewById(R.id.tvStartPoint);
        tvEndPoint = findViewById(R.id.tvEndPoint);
        btnUndoPoint = findViewById(R.id.btnUndoPoint);
        btnClearPoints = findViewById(R.id.btnClearPoints);
        btnSaveRoute = findViewById(R.id.btnSaveRoute);
        mapView = findViewById(R.id.mapView);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initMap();
        initButtons();
        updateInfoTexts();
    }

    // 지도 초기화
    private void initMap() {
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                // 필요 시 정리
            }

            @Override
            public void onMapError(@NonNull Exception error) {
                Toast.makeText(RouteCreateActivity.this,
                        "지도 로딩 실패: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                labelLayer = kakaoMap.getLabelManager().getLayer();
                shapeLayer = kakaoMap.getShapeManager().getLayer();

                // 기본 카메라 위치 (서울 시청 근처)
                LatLng seoul = LatLng.from(37.5665, 126.9780);
                CameraUpdate update =
                        CameraUpdateFactory.newCenterPosition(seoul, 10);
                kakaoMap.moveCamera(update);

                // 지도 클릭 리스너: 지점 추가
                kakaoMap.setOnMapClickListener((kMap, position, screenPoint, poi) -> {
                    addRoutePoint(position);
                });
            }
        });
    }

    // 버튼 클릭 리스너 설정
    private void initButtons() {
        // 마지막 지점 되돌리기
        btnUndoPoint.setOnClickListener(v -> {
            if (!routePoints.isEmpty()) {
                routePoints.remove(routePoints.size() - 1);
                redrawRoute();
            } else {
                Toast.makeText(this, "되돌릴 지점이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 모든 지점 초기화
        btnClearPoints.setOnClickListener(v -> {
            if (!routePoints.isEmpty()) {
                routePoints.clear();
                redrawRoute();
            }
        });

        // Firestore에 루트 저장
        btnSaveRoute.setOnClickListener(v -> saveRoute());
    }

    // 지도 탭해서 지점 추가
    private void addRoutePoint(LatLng position) {
        routePoints.add(position);
        redrawRoute();
    }

    // 지점 수/출발/도착 텍스트 갱신
    private void updateInfoTexts() {
        int size = routePoints.size();
        tvPointCount.setText("지점: " + size + "개");

        if (size == 0) {
            tvStartPoint.setText("출발: 미지정");
            tvEndPoint.setText("도착: 미지정");
            return;
        }

        LatLng start = routePoints.get(0);
        tvStartPoint.setText(String.format(
                Locale.getDefault(),
                "출발: %.5f, %.5f",
                start.latitude, start.longitude
        ));

        if (size >= 2) {
            LatLng end = routePoints.get(size - 1);
            tvEndPoint.setText(String.format(
                    Locale.getDefault(),
                    "도착: %.5f, %.5f",
                    end.latitude, end.longitude
            ));
        } else {
            tvEndPoint.setText("도착: 미지정");
        }
    }

    // 라벨 + 폴리라인 + 카메라까지 전체 다시 그림
    private void redrawRoute() {
        if (kakaoMap == null || labelLayer == null || shapeLayer == null) return;

        // 하단 텍스트 갱신
        updateInfoTexts();

        // 기존 라벨/선 제거
        labelLayer.removeAll();
        shapeLayer.removeAll();
        routePolyline = null;

        // 지점마다 라벨(아이콘) 추가
        for (LatLng p : routePoints) {
            LabelOptions options = LabelOptions
                    .from(p)
                    .setStyles(android.R.drawable.ic_menu_mylocation);
            labelLayer.addLabel(options);
        }

        // 선 그리기 (2개 이상일 때만)
        if (routePoints.size() >= 2) {
            MapPoints mapPoints = MapPoints.fromLatLng(routePoints);
            PolylineOptions polyOptions =
                    PolylineOptions.from(mapPoints, 6f, 0xFF00796B);
            routePolyline = shapeLayer.addPolyline(polyOptions);
        }

        // 카메라 이동 (전체 지점이 보이도록)
        if (!routePoints.isEmpty()) {
            LatLng[] arr = routePoints.toArray(new LatLng[0]);
            CameraUpdate update;
            if (arr.length == 1) {
                update = CameraUpdateFactory.newCenterPosition(arr[0], 15);
            } else {
                update = CameraUpdateFactory.fitMapPoints(arr, 80);
            }
            kakaoMap.moveCamera(update);
        }
    }

    // Firestore에 루트 저장
    private void saveRoute() {
        String title = etRouteTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "루트 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (routePoints.size() < 2) {
            Toast.makeText(this, "출발과 도착을 포함해 최소 2개 지점을 선택해주세요.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng start = routePoints.get(0);
        LatLng end = routePoints.get(routePoints.size() - 1);

        // points 배열 (lat, lng 리스트)
        List<Map<String, Object>> pointList = new ArrayList<>();
        for (LatLng p : routePoints) {
            Map<String, Object> m = new HashMap<>();
            m.put("lat", p.latitude);
            m.put("lng", p.longitude);
            pointList.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("startLat", start.latitude);
        data.put("startLng", start.longitude);
        data.put("endLat", end.latitude);
        data.put("endLng", end.longitude);
        data.put("startPlace", String.format(Locale.getDefault(),
                "%.5f, %.5f", start.latitude, start.longitude));
        data.put("endPlace", String.format(Locale.getDefault(),
                "%.5f, %.5f", end.latitude, end.longitude));
        data.put("memo", "");
        data.put("hostUid", user.getUid());
        data.put("createdAt", System.currentTimeMillis());
        data.put("points", pointList);

        db.collection("routes")
                .add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "루트가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "루트 저장 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // MapView 라이프사이클 연동
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
