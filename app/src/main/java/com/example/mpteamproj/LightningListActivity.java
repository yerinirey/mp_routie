package com.example.mpteamproj;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mpteamproj.R;

import java.util.ArrayList;
import java.util.List;

public class LightningListActivity extends AppCompatActivity {

    private RecyclerView rvLightning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lightning_list);

        rvLightning = findViewById(R.id.rvLightning);

        rvLightning.setLayoutManager(new LinearLayoutManager(this));

        // TODO: 실제 서버에서 데이터 가져오기 전까지는 예시 데이터 사용
        List<com.example.mpteamproj.LightningPost> dummy = new ArrayList<>();
        dummy.add(new com.example.mpteamproj.LightningPost("홍대 카페 번개", "홍대입구역 · 오늘 19:00"));
        dummy.add(new com.example.mpteamproj.LightningPost("한강 야간 산책", "뚝섬 한강공원 · 내일 20:30"));
        dummy.add(new com.example.mpteamproj.LightningPost("밤도깨비 야시장 구경", "여의도 한강공원 · 토요일 18:00"));

        com.example.mpteamproj.LightningAdapter adapter = new com.example.mpteamproj.LightningAdapter(dummy);
        rvLightning.setAdapter(adapter);
    }
}
