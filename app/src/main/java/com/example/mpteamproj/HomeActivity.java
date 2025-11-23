package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mpteamproj.R;

public class HomeActivity extends AppCompatActivity {

    private EditText etSearchRoute;
    private Button btnCreateRoute;
    private Button btnBrowseRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        etSearchRoute = findViewById(R.id.etSearchRoute);
        btnCreateRoute = findViewById(R.id.btnCreateRoute);
        btnBrowseRoute = findViewById(R.id.btnBrowseRoute);

        // 검색창은 일단 Toast만
        etSearchRoute.setOnEditorActionListener((textView, actionId, event) -> {
            String keyword = etSearchRoute.getText().toString().trim();
            if (!keyword.isEmpty()) {
                Toast.makeText(this, "검색: " + keyword, Toast.LENGTH_SHORT).show();
                // TODO: 검색 결과 화면 or 리스트로 넘기기
            }
            return true;
        });

        // 루트 생성 버튼 (나중에 맵 화면으로 연결할 예정)
        btnCreateRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(HomeActivity.this, RouteCreateMapActivity.class);
//                startActivity(intent);
                Toast.makeText(HomeActivity.this, "루트 생성 화면으로 이동 예정", Toast.LENGTH_SHORT).show();
            }
        });

        // 루트 둘러보기 버튼
        btnBrowseRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(HomeActivity.this, RouteListActivity.class);
//                startActivity(intent);
                Toast.makeText(HomeActivity.this, "루트 리스트 화면으로 이동 예정", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
