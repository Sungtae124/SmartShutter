package com.example.smartshutter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    private ProgressBar loadingSpinner;
    private RecyclerView resultsRecyclerView;
    private Button textSearchButton;
    private ImageButton captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // UI 요소 초기화
        loadingSpinner = findViewById(R.id.loadingSpinner);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        textSearchButton = findViewById(R.id.textSearchButton);
        captureButton = findViewById(R.id.captureButton);

        // RecyclerView 설정
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ResultAdapter adapter = new ResultAdapter(getDummyResults());
        resultsRecyclerView.setAdapter(adapter);

        // 텍스트 검색 결과 보기 버튼
        textSearchButton.setOnClickListener(v -> {
            Snackbar.make(v, "텍스트 검색 결과 보기 클릭됨", Snackbar.LENGTH_SHORT).show();
        });

        // 다시 촬영 버튼
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, CameraActivity.class);
            startActivity(intent);
            finish();
        });

        // 데이터 로딩 시뮬레이션
        showLoading(true);
        new Handler().postDelayed(() -> showLoading(false), 2000); // 2초 후 로딩 해제
    }

    private void showLoading(boolean isLoading) {
        loadingSpinner.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        resultsRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private List<ResultItem> getDummyResults() {
        // 더미 데이터 생성
        List<ResultItem> results = new ArrayList<>();
        results.add(new ResultItem("https://example.com/image1.jpg", "구글 검색 결과 1", R.drawable.ic_google));
        results.add(new ResultItem("https://example.com/image2.jpg", "네이버 검색 결과 1", R.drawable.ic_naver));
        return results;
    }
}
