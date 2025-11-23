package com.example.mpteamproj;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LightningDetailActivity extends AppCompatActivity {

    public static final String EXTRA_LIGHTNING_ID = "lightningId";

    private TextView tvLightningTitle;
    private TextView tvLightningMeta;
    private TextView tvLightningDescription;
    private TextView tvLightningLocation;
    private TextView tvLinkedRouteInfo;
    private MapView lightningMapView;

    private FirebaseFirestore db;
    private String lightningId;

    // route 정보
    private String routeId;
    private String routeTitle;

    // 지도 관련
    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;
    private ShapeLayer shapeLayer;
    private Polyline routePolyline;
    private final List<LatLng> routePoints = new ArrayList<>();
    private boolean mapReady = false;
    private boolean routeLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_detail);

        tvLightningTitle = findViewById(R.id.tvLightningTitle);
        tvLightningMeta = findViewById(R.id.tvLightningMeta);
        tvLightningDescription = findViewById(R.id.tvLightningDescription);
        tvLightningLocation = findViewById(R.id.tvLightningLocation);
        tvLinkedRouteInfo = findViewById(R.id.tvLinkedRouteInfo);
        lightningMapView = findViewById(R.id.lightningMapView);

        db = FirebaseFirestore.getInstance();

        lightningId = getIntent().getStringExtra(EXTRA_LIGHTNING_ID);
        if (TextUtils.isEmpty(lightningId)) {
            Toast.makeText(this, "번개 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initMap();      // 지도 준비 시작
        loadLightning(); // 번개 데이터 로드
    }

    // 번개 문서 읽기
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
        String hostNickname = safeString(doc.getString("hostNickname"));
        String locationDesc = safeString(doc.getString("locationDesc")); // 🔹 새 필드

        Long createdAt = null;
        Object createdRaw = doc.get("createdAt");
        if (createdRaw instanceof Number) {
            createdAt = ((Number) createdRaw).longValue();
        }

        routeId = safeString(doc.getString("routeId"));
        routeTitle = safeString(doc.getString("routeTitle"));

        tvLightningTitle.setText(title.isEmpty() ? "번개 상세" : title);

        String timeText;
        if (createdAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            timeText = sdf.format(new Date(createdAt));
        } else {
            timeText = "시간 정보 없음";
        }

        // 호스트 이름: 닉네임 > UID > "알 수 없음"
        String hostLabel;
        if (!hostNickname.isEmpty()) {
            hostLabel = hostNickname;
        } else if (!hostUid.isEmpty()) {
            hostLabel = hostUid;
        } else {
            hostLabel = "알 수 없음";
        }

        tvLightningMeta.setText("호스트: " + hostLabel + " / 생성 시각: " + timeText);
        tvLightningDescription.setText(
                desc.isEmpty() ? "설명이 없습니다." : desc
        );

        // 모임 위치 소개 표시
        if (!locationDesc.isEmpty()) {
            tvLightningLocation.setText("모임 위치: " + locationDesc);
        } else {
            tvLightningLocation.setText("모임 위치: 미정");
        }

        // 루트 텍스트 (위경도 없이 제목/ID만)
        if (!TextUtils.isEmpty(routeId)) {
            String routeLabel = !routeTitle.isEmpty() ? routeTitle : routeId;
            tvLinkedRouteInfo.setText("연결된 루트: " + routeLabel);

            // 루트 geometry를 위해 routes 컬렉션에서 다시 로드
            loadRoute(routeId);
        } else {
            tvLinkedRouteInfo.setText("연결된 루트: 없음");
            // routeId 없으면 지도에 아무것도 안 그려짐
        }
    }

    // routes/{routeId} 문서에서 polyline 포인트 로드
    private void loadRoute(String routeId) {
        db.collection("routes")
                .document(routeId)
                .get()
                .addOnSuccessListener(this::onRouteLoaded)
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "루트 정보 불러오기 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void onRouteLoaded(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "루트가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        routePoints.clear();

        Object rawPoints = doc.get("points");
        if (rawPoints instanceof List) {
            List<?> list = (List<?>) rawPoints;
            for (Object o : list) {
                if (o instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) o;
                    Object latObj = m.get("lat");
                    Object lngObj = m.get("lng");
                    if (latObj instanceof Number && lngObj instanceof Number) {
                        double lat = ((Number) latObj).doubleValue();
                        double lng = ((Number) lngObj).doubleValue();
                        routePoints.add(LatLng.from(lat, lng));
                    }
                }
            }
        }

        // 혹시 points가 비어 있으면 startLat/endLat로 최소한의 선만
        Double sLat = doc.getDouble("startLat");
        Double sLng = doc.getDouble("startLng");
        Double eLat = doc.getDouble("endLat");
        Double eLng = doc.getDouble("endLng");

        if (routePoints.isEmpty()) {
            if (sLat != null && sLng != null) {
                routePoints.add(LatLng.from(sLat, sLng));
            }
            if (eLat != null && eLng != null) {
                LatLng end = LatLng.from(eLat, eLng);
                if (routePoints.isEmpty()
                        || !routePoints.get(routePoints.size() - 1).equals(end)) {
                    routePoints.add(end);
                }
            }
        }

        routeLoaded = true;
        updateMapIfReady();
    }

    // 지도 초기화
    private void initMap() {
        lightningMapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {}

            @Override
            public void onMapError(@NonNull Exception error) {
                Toast.makeText(LightningDetailActivity.this,
                        "지도 로딩 실패: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                labelLayer = kakaoMap.getLabelManager().getLayer();
                shapeLayer = kakaoMap.getShapeManager().getLayer();
                mapReady = true;
                updateMapIfReady();
            }
        });
    }

    // mapReady + routeLoaded 둘 다 true일 때 polyline + 핀 그리기
    private void updateMapIfReady() {
        if (!mapReady || !routeLoaded || kakaoMap == null) return;
        if (labelLayer == null || shapeLayer == null) return;

        labelLayer.removeAll();
        shapeLayer.removeAll();
        routePolyline = null;

        if (routePoints.size() >= 2) {
            MapPoints mapPoints = MapPoints.fromLatLng(routePoints);
            PolylineOptions options =
                    PolylineOptions.from(mapPoints, 6f, 0xFF00796B);
            routePolyline = shapeLayer.addPolyline(options);
        }

        if (!routePoints.isEmpty()) {
            LatLng start = routePoints.get(0);
            LabelOptions startOpt = LabelOptions.from(start)
                    .setStyles(android.R.drawable.presence_online); // 초록
            labelLayer.addLabel(startOpt);
        }

        if (routePoints.size() >= 2) {
            LatLng end = routePoints.get(routePoints.size() - 1);
            LabelOptions endOpt = LabelOptions.from(end)
                    .setStyles(android.R.drawable.presence_busy); // 빨강
            labelLayer.addLabel(endOpt);
        }

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

    private String safeString(String v) {
        return v != null ? v : "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightningMapView != null) lightningMapView.resume();
    }

    @Override
    protected void onPause() {
        if (lightningMapView != null) lightningMapView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (lightningMapView != null) lightningMapView.finish();
        super.onDestroy();
    }
}
