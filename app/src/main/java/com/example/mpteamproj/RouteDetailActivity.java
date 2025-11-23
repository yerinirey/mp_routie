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
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.shape.MapPoints;
import com.kakao.vectormap.shape.Polyline;
import com.kakao.vectormap.shape.PolylineOptions;
import com.kakao.vectormap.shape.ShapeLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RouteDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTE_ID = "routeId";

    private TextView tvDetailRouteTitle;
    private TextView tvDetailStart;
    private TextView tvDetailEnd;
    private MapView detailMapView;

    private FirebaseFirestore db;
    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;
    private ShapeLayer shapeLayer;
    private Polyline routePolyline;

    // 전체 경로 포인트 (출발 + 경유 + 도착)
    private final List<LatLng> routePoints = new ArrayList<>();

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
                labelLayer = kakaoMap.getLabelManager().getLayer();
                shapeLayer = kakaoMap.getShapeManager().getLayer();
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

        tvDetailRouteTitle.setText(title.isEmpty() ? "루트 상세" : title);

        if (sLat != null && sLng != null) {
            tvDetailStart.setText(String.format(
                    Locale.getDefault(),
                    "출발: %s (%.5f, %.5f)",
                    startPlace, sLat, sLng
            ));
        } else {
            tvDetailStart.setText("출발: 정보 없음");
        }

        if (eLat != null && eLng != null) {
            tvDetailEnd.setText(String.format(
                    Locale.getDefault(),
                    "도착: %s (%.5f, %.5f)",
                    endPlace, eLat, eLng
            ));
        } else {
            tvDetailEnd.setText("도착: 정보 없음");
        }

        // points 배열에서 전체 경로 복원
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

        // 혹시 points가 없다면 최소 start/end로라도 구성
        if (routePoints.isEmpty()) {
            if (sLat != null && sLng != null) {
                routePoints.add(LatLng.from(sLat, sLng));
            }
            if (eLat != null && eLng != null) {
                LatLng end = LatLng.from(eLat, eLng);
                if (routePoints.isEmpty() || !routePoints.get(routePoints.size() - 1).equals(end)) {
                    routePoints.add(end);
                }
            }
        }

        routeLoaded = true;
        updateMapIfReady();
    }

    private void updateMapIfReady() {
        if (!mapReady || !routeLoaded || kakaoMap == null) return;
        if (labelLayer == null || shapeLayer == null) return;

        labelLayer.removeAll();
        shapeLayer.removeAll();
        routePolyline = null;

        // 1) 전체 Polyline 그리기 (중간 경유 포함)
        if (routePoints.size() >= 2) {
            MapPoints mapPoints = MapPoints.fromLatLng(routePoints);
            PolylineOptions options =
                    PolylineOptions.from(mapPoints, 6f, 0xFF00796B);
            routePolyline = shapeLayer.addPolyline(options);
        }

        // 2) 핀은 출발/도착만
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

        // 3) 여기서는 카메라를 루트 전체에 맞춰 이동 (둘러보기 전용)
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
