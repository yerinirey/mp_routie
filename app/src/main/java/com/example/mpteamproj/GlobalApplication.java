package com.example.mpteamproj;

import android.app.Application;
import com.kakao.vectormap.KakaoMapSdk;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 카카오맵 SDK 초기화
        KakaoMapSdk.init(this, "f9720c1b4df81961877a1c77c2669c20");
    }
}