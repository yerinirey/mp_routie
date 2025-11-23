package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
    private Button btnCreateLightning;

    private FirebaseFirestore db;
    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;
    private ShapeLayer shapeLayer;
    private Polyline routePolyline;

    private final List<LatLng> routePoints = new ArrayList<>();

    private boolean mapReady = false;
    private boolean routeLoaded = false;
    private String routeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        tvDetailRouteTitle = findViewById(R.id.tvDetailRouteTitle);
        tvDetailStart = findViewById(R.id.tvDetailStart);
        tvDetailEnd = findViewById(R.id.tvDetailEnd);
        detailMapView = findViewById(R.id.detailMapView);
        btnCreateLightning = findViewById(R.id.btnCreateLightning);

        db = FirebaseFirestore.getInstance();

        routeId = getIntent().getStringExtra(EXTRA_ROUTE_ID);
        if (routeId == null || routeId.isEmpty()) {
            Toast.makeText(this, "루트 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (btnCreateLightning != null) {
            btnCreateLightning.setOnClickListener(v -> {
                Intent intent = new Intent(RouteDetailActivity.this, LightningCreateActivity.class);
                intent.putExtra("routeId", routeId);
                intent.putExtra("routeTitle", tvDetailRouteTitle.getText().toString());
                intent.putExtra("routeStart", tvDetailStart.getText().toString());
                intent.putExtra("routeEnd", tvDetailEnd.getText().toString());
                startActivity(intent);
            });
        }

        initMap();
        loadRoute(routeId);
    }

    private void initMap() {
        detailMapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {}

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
                    .setStyles(android.R.drawable.presence_online);
            labelLayer.addLabel(startOpt);
        }

        if (routePoints.size() >= 2) {
            LatLng end = routePoints.get(routePoints.size() - 1);
            LabelOptions endOpt = LabelOptions.from(end)
                    .setStyles(android.R.drawable.presence_busy);
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
