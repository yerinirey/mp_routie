package com.example.mpteamproj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private AutoCompleteTextView etSearchRoute;
    private Button btnCreateRoute;
    private Button btnBrowseRoute;
    private Button btnLightning;
    private Button btnMyPage;

    private FirebaseFirestore db;

    private final List<String> tagSuggestions = new ArrayList<>();
    private ArrayAdapter<String> tagAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        etSearchRoute   = findViewById(R.id.etSearchRoute);
        btnCreateRoute  = findViewById(R.id.btnCreateRoute);
        btnBrowseRoute  = findViewById(R.id.btnBrowseRoute);
        btnLightning    = findViewById(R.id.btnLightning);
        btnMyPage       = findViewById(R.id.btnMyPage);

        db = FirebaseFirestore.getInstance();

        tagAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                tagSuggestions
        );
        etSearchRoute.setAdapter(tagAdapter);
        etSearchRoute.setThreshold(1);

        loadTagSuggestions();

        etSearchRoute.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openRouteListWithTag();
                return true;
            }
            return false;
        });

        btnCreateRoute.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RouteCreateActivity.class);
            startActivity(intent);
        });

        btnBrowseRoute.setOnClickListener(v -> openRouteListWithTag());

        btnLightning.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LightningListActivity.class);
            startActivity(intent);
        });

        btnMyPage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MyPageActivity.class);
            startActivity(intent);
        });
    }

    private void loadTagSuggestions() {
        db.collection("routes")
                .get()
                .addOnSuccessListener(snap -> {
                    Set<String> uniqueTags = new HashSet<>();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap) {
                            String tag = doc.getString("tag");
                            if (tag != null) {
                                tag = tag.trim();
                                if (!tag.isEmpty()) {
                                    uniqueTags.add(tag);
                                }
                            }
                        }
                    }
                    tagSuggestions.clear();
                    tagSuggestions.addAll(uniqueTags);
                    tagAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "태그 목록을 불러오지 못했습니다: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void openRouteListWithTag() {
        String tag = etSearchRoute.getText().toString().trim();

        Intent intent = new Intent(HomeActivity.this, RouteListActivity.class);
        if (!TextUtils.isEmpty(tag)) {
            intent.putExtra("tagFilter", tag);
        }
        startActivity(intent);
    }
}
