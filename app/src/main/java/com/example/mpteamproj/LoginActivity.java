package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mpteamproj.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginId;
    private EditText etLoginPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginId = findViewById(R.id.etLoginId);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = etLoginId.getText().toString().trim();
                String pw = etLoginPassword.getText().toString().trim();

                if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pw)) {
                    Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: 실제 로그인 로직 (서버/DB 연동)
                // 여기서는 그냥 성공했다고 치고 홈으로 이동
                Intent intent = new Intent(LoginActivity.this, com.example.mpteamproj.HomeActivity.class);
                startActivity(intent);
                finish(); // 로그인 화면 닫기
            }
        });

        // 회원가입 화면으로 이동
        tvGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, com.example.mpteamproj.RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
