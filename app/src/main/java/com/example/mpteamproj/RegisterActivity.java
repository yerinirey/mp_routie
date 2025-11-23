package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegisterEmail;
    private EditText etRegisterId;      // 아이디는 일단 UI만, 서버 저장은 나중에
    private EditText etRegisterPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterId = findViewById(R.id.etRegisterId);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> {

            String email = etRegisterEmail.getText().toString().trim();
            String userId = etRegisterId.getText().toString().trim();   // (지금은 안 씀)
            String pw = etRegisterPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(userId) || TextUtils.isEmpty(pw)) {
                Toast.makeText(
                        RegisterActivity.this,
                        "이메일, 아이디, 비밀번호를 모두 입력하세요.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // ✅ 여기까지 오면 Auth 계정 생성은 무조건 성공한 상태
                            showRegisterSuccessDialog();

                        } else {
                            String msg = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "회원가입 실패";
                            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // 🔹 회원가입 완료 모달 + 로그인 Activity로 이동
    private void showRegisterSuccessDialog() {
        new AlertDialog.Builder(RegisterActivity.this)
                .setTitle("회원가입 완료")
                .setMessage("회원가입이 완료되었습니다.\n로그인해주세요.")
                .setCancelable(false)
                .setPositiveButton("확인", (dialog, which) -> {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // 회원가입 화면 종료
                })
                .show();
    }
}
