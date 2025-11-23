package com.example.mpteamproj;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;

public class RouteDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTE_ID = "routeId";

    private TextView tvDetailRouteTitle;
    private TextView tvDetailStart;
    private TextView tvDetailEnd;
    private MapView detailMapView;

    private FirebaseFirestore db;
    private KakaoMap kakaoMap;

    private LatLng startLatLng;
    private LatLng endLatLng;

    private boolean mapReady = false;
    private boolean routeLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        tvDetailRouteTitle = findViewById(R.id.tvDetailRouteTitle);
        tvDetailStart = findViewById(R.id.tvDetailStart);
        tvDetailEnd = findViewById(R.id.tvDetailEnd);
        detailMapView = findViewById(R.id.detailMapView);

        db = FirebaseFirestore.getInstance();

        String routeId = getIntent().getStringExtra(EXTRA_ROUTE_ID);
        if (routeId == null || routeId.isEmpty()) {
            Toast.makeText(this, "잘못된 루트 ID입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initMap();
        loadRoute(routeId);
    }

    private void initMap() {
        detailMapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                // nothing
            }

            @Override
            public void onMapError(@NonNull Exception error) {
                Toast.makeText(RouteDetailActivity.this,
                        "지도 로딩 실패: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                mapReady = true;
                updateMapIfReady();
            }
        });
    }

    private void loadRoute(String routeId) {
        db.collection("routes")
                .document(routeId)
                .get()
                .addOnSuccessListener(this::onRouteLoaded)
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "루트 정보 불러오기 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("RouteDetail", "loadRoute failed", e);
                    finish();
                });
    }

    private void onRouteLoaded(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "루트가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = safeString(doc.getString("title"));
        String startPlace = safeString(doc.getString("startPlace"));
        String endPlace = safeString(doc.getString("endPlace"));

        Double sLat = doc.getDouble("startLat");
        Double sLng = doc.getDouble("startLng");
        Double eLat = doc.getDouble("endLat");
        Double eLng = doc.getDouble("endLng");

        if (sLat != null && sLng != null) {
            startLatLng = LatLng.from(sLat, sLng);
        }
        if (eLat != null && eLng != null) {
            endLatLng = LatLng.from(eLat, eLng);
        }

        tvDetailRouteTitle.setText(title.isEmpty() ? "루트 상세" : title);
        tvDetailStart.setText("시작 지점: " + (startPlace.isEmpty() ? "좌표 (" + sLat + ", " + sLng + ")" : startPlace));
        tvDetailEnd.setText("도착 지점: " + (endPlace.isEmpty() ? "좌표 (" + eLat + ", " + eLng + ")" : endPlace));

        routeLoaded = true;
        updateMapIfReady();
    }

    private void updateMapIfReady() {
        if (!mapReady || !routeLoaded || kakaoMap == null) return;
        if (kakaoMap.getLabelManager() == null) return;

        LabelLayer layer = kakaoMap.getLabelManager().getLayer();
        layer.removeAll();

        // 시작/도착 마커 찍기
        if (startLatLng != null) {
            LabelOptions startOptions = LabelOptions
                    .from(startLatLng)
                    .setStyles(android.R.drawable.ic_menu_mylocation);
            Label startLabel = layer.addLabel(startOptions);
        }

        if (endLatLng != null) {
            LabelOptions endOptions = LabelOptions
                    .from(endLatLng)
                    .setStyles(android.R.drawable.ic_menu_mylocation);
            Label endLabel = layer.addLabel(endOptions);
        }

        // 카메라를 두 점의 중간쯤으로 이동
        LatLng center = null;
        if (startLatLng != null && endLatLng != null) {
            double cLat = (startLatLng.latitude + endLatLng.latitude) / 2.0;
            double cLng = (startLatLng.longitude + endLatLng.longitude) / 2.0;
            center = LatLng.from(cLat, cLng);
        } else if (startLatLng != null) {
            center = startLatLng;
        } else if (endLatLng != null) {
            center = endLatLng;
        }

        if (center != null) {
            CameraUpdate cameraUpdate =
                    CameraUpdateFactory.newCenterPosition(center, 10); // 줌 레벨은 적당히
            kakaoMap.moveCamera(cameraUpdate);
        }
    }

    private String safeString(String v) {
        return v != null ? v : "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (detailMapView != null) detailMapView.resume();
    }

    @Override
    protected void onPause() {
        if (detailMapView != null) detailMapView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (detailMapView != null) detailMapView.finish();
        super.onDestroy();
    }
}
