package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private EditText etSearchRoute;
    private Button btnCreateRoute;
    private Button btnBrowseRoute;
    private Button btnLightning;
    private Button btnMyPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        etSearchRoute = findViewById(R.id.etSearchRoute);
        btnCreateRoute = findViewById(R.id.btnCreateRoute);
        btnBrowseRoute = findViewById(R.id.btnBrowseRoute);
        btnLightning = findViewById(R.id.btnLightning);
        btnMyPage = findViewById(R.id.btnMyPage);

        etSearchRoute.setOnEditorActionListener((textView, actionId, event) -> {
            String keyword = etSearchRoute.getText().toString().trim();
            if (!keyword.isEmpty()) {
                Toast.makeText(this, "검색: " + keyword, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        btnCreateRoute.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, RouteCreateActivity.class);
                    startActivity(intent);
                }
        );

        btnBrowseRoute.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, RouteListActivity.class);
                    startActivity(intent);
                }
        );

        // 번개 둘러보기 버튼 → LightningListActivity로 이동
        btnLightning.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LightningListActivity.class);
            startActivity(intent);
        });


        btnMyPage.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MyPageActivity.class));
        });
    }
}
