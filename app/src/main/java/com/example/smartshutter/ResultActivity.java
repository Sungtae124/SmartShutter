package com.example.smartshutter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private ProgressBar loadingSpinner;
    private RecyclerView resultsRecyclerView;
    private ImageButton captureButton;

    private ImageSearchManager imageSearchManager;

    private boolean isSnackbarShown = false; // Snackbar가 이미 표시되었는지 확인
    private int totalScrollDistance = 0; // 스크롤 거리 추적
    private int scrollEventCount = 0; // 스크롤 이벤트 횟수 추적

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // UI 요소 초기화
        loadingSpinner = findViewById(R.id.loadingSpinner);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        captureButton = findViewById(R.id.captureButton);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 사진 경로 확인
        String photoPath = getIntent().getStringExtra("photoPath");
        if (photoPath != null) {
            File imageFile = new File(photoPath); // String을 File 객체로 변환
            if (imageFile.exists()) {
                Log.d("ResultActivity", "Image file exists: " + photoPath);
                loadResults(imageFile); // File 객체 전달
            } else {
                Log.e("ResultActivity", "Image file does not exist: " + photoPath);
                showLoading(false);
                showSnackbarError("사진 파일을 찾을 수 없습니다.");
            }
        } else {
            Log.e("ResultActivity", "photoPath is null");
            showLoading(false);
            showSnackbarError("사진 경로가 전달되지 않았습니다.");
        }

        // 다시 촬영 버튼
        captureButton.setOnClickListener(v -> {
            finish(); // 현재 액티비티 종료
        });

        // RecyclerView 스크롤 리스너 추가
        resultsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalScrollDistance += dy; // 스크롤 거리 추적
                if (dy > 0) scrollEventCount++;

                // 조건: 스크롤 거리 1500px 이상 또는 이벤트 10번 이상일 때 Snackbar 표시
                if (!isSnackbarShown && (totalScrollDistance > 1500 || scrollEventCount >= 10)) {
                    showSnackbar();
                    isSnackbarShown = true;
                }
            }
        });
    }

    private void loadResults(File imageFile) {
        showLoading(true);

        // ImageSearchManager를 통해 검색 결과 가져오기
        imageSearchManager = new ImageSearchManager(this);

        imageSearchManager.searchGoogleImages(imageFile)
                .observe(this, resultItems -> {
                    if (resultItems != null && !resultItems.isEmpty()) {
                        updateRecyclerView(resultItems);
                    } else {
                        showSnackbarError("검색 결과가 없습니다.");
                    }
                    showLoading(false);
                });
    }

    private void showSnackbar() {
        Snackbar.make(resultsRecyclerView, "텍스트 검색 결과 보기를 사용해보세요!", Snackbar.LENGTH_INDEFINITE)
                .setAction("열기", v -> {
                    Log.d("ResultActivity", "Text Search is selected");
                })
                .show();
    }

    private void showSnackbarError(String message) {
        Snackbar.make(resultsRecyclerView, message, Snackbar.LENGTH_LONG).show();
    }

    private void showLoading(boolean isLoading) {
        loadingSpinner.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        resultsRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateRecyclerView(List<ResultItem> results) {
        ResultAdapter adapter = new ResultAdapter(results);
        resultsRecyclerView.setAdapter(adapter);
    }
}
