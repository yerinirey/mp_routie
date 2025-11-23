package com.example.mpteamproj;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mpteamproj.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegisterEmail;
    private EditText etRegisterId;
    private EditText etRegisterPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterId = findViewById(R.id.etRegisterId);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // 회원가입 버튼 클릭
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = etRegisterEmail.getText().toString().trim();
                String id = etRegisterId.getText().toString().trim();
                String pw = etRegisterPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(id) || TextUtils.isEmpty(pw)) {
                    Toast.makeText(RegisterActivity.this, "모든 정보를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: 실제 회원가입 로직 (서버/DB 저장)
                // 지금은 그냥 완료 메세지만 띄우고 화면 닫기
                Toast.makeText(RegisterActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                finish(); // LoginActivity로 복귀
            }
        });
    }
}
