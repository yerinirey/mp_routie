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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegisterEmail;
    private EditText etRegisterId;      // 아이디 = 닉네임
    private EditText etRegisterPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;      // 🔹 Firestore 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterId = findViewById(R.id.etRegisterId);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();  // 🔹 초기화

        btnRegister.setOnClickListener(v -> {

            String email = etRegisterEmail.getText().toString().trim();
            String userId = etRegisterId.getText().toString().trim();   // 아이디 = 닉네임
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
                            // 🔹 계정 생성은 성공
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser == null) {
                                // 이 케이스는 거의 없지만, 방어적으로 처리
                                showRegisterSuccessDialog();
                                return;
                            }

                            String nickname = userId; // 아이디를 그대로 닉네임으로 사용

                            // 1) Auth 프로필에 displayName 설정
                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(nickname)
                                            .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {

                                        // 2) Firestore users 컬렉션에 유저 정보 저장
                                        Map<String, Object> userDoc = new HashMap<>();
                                        userDoc.put("email", email);
                                        userDoc.put("nickname", nickname);
                                        userDoc.put("createdAt", System.currentTimeMillis());

                                        mDb.collection("users")
                                                .document(firebaseUser.getUid())
                                                .set(userDoc)
                                                .addOnCompleteListener(userDocTask -> {

                                                    // 프로필/유저 문서 저장 중 하나라도 실패하면 토스트만 띄우고 회원가입은 계속 진행
                                                    if (!profileTask.isSuccessful() || !userDocTask.isSuccessful()) {
                                                        Toast.makeText(
                                                                RegisterActivity.this,
                                                                "회원가입은 완료되었지만 프로필 저장 중 오류가 발생했습니다.",
                                                                Toast.LENGTH_SHORT
                                                        ).show();
                                                    }

                                                    // 최종적으로는 기존처럼 성공 모달 + 로그인 화면으로 이동
                                                    showRegisterSuccessDialog();
                                                });
                                    });

                        } else {
                            String msg = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "회원가입 실패";
                            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // 🔹 회원가입 완료 모달 + 로그인 Activity로 이동 (기존 로직 그대로)
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
