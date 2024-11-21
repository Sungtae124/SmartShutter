/*
package com.example.smartshutter;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    private ProgressBar loadingSpinner;
    private RecyclerView resultsRecyclerView;
    private ImageButton captureButton;

    // 추가: 스크롤 관련 변수
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

        // RecyclerView 설정
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ResultAdapter adapter = new ResultAdapter(getDummyResults());
        resultsRecyclerView.setAdapter(adapter);

        // RecyclerView 스크롤 리스너 추가
        resultsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 스크롤 거리와 이벤트 횟수 추적
                totalScrollDistance += dy; // 현재까지 스크롤한 거리
                if (dy > 0) scrollEventCount++; // 아래로 스크롤한 이벤트만 카운트

                // 조건: 스크롤 거리 500px 이상 또는 이벤트 3번 이상일 때 Snackbar 표시
                if (!isSnackbarShown && (totalScrollDistance > 1500 || scrollEventCount >= 10)) {
                    showSnackbar();
                    isSnackbarShown = true; // Snackbar는 한 번만 표시
                }
            }
        });

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
        new Handler().postDelayed(() -> showLoading(false), 5000); // 5초 후 로딩 해제
    }

    private void showSnackbar() {
        Snackbar.make(resultsRecyclerView, "텍스트 검색 결과 보기를 사용해보세요!", Snackbar.LENGTH_INDEFINITE)
                .setAction("열기", v -> {
                    // "열기" 버튼 클릭 시 동작
                    //Intent intent = new Intent(ResultActivity.this, TextSearchActivity.class); // 텍스트 검색 화면으로 이동
                    //startActivity(intent);
                    Log.d("ResultActivity","Text Search is selected");
                })
                .show();
    }

    private void showLoading(boolean isLoading) {
        loadingSpinner.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        resultsRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateRecyclerView(List<ResultItem> results) {
        ResultAdapter adapter = new ResultAdapter(results);
        resultsRecyclerView.setAdapter(adapter);
    }

    private List<ResultItem> getDummyResults() {
        // 더미 데이터 생성
        List<ResultItem> results = new ArrayList<>();
        results.add(new ResultItem("https://example.com/image1.jpg", "구글 검색 결과 1", R.drawable.ic_google));
        results.add(new ResultItem("https://example.com/image2.jpg", "네이버 검색 결과 1", R.drawable.ic_naver));
        results.add(new ResultItem("https://example.com/image3.jpg", "구글 검색 결과 2", R.drawable.ic_google));
        results.add(new ResultItem("https://example.com/image4.jpg", "네이버 검색 결과 2", R.drawable.ic_naver));
        results.add(new ResultItem("https://example.com/image5.jpg", "구글 검색 결과 3", R.drawable.ic_google));
        results.add(new ResultItem("https://example.com/image6.jpg", "네이버 검색 결과 3", R.drawable.ic_naver));
        return results;
    }
}
*/
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

import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private ProgressBar loadingSpinner;
    private RecyclerView resultsRecyclerView;
    private ImageButton captureButton;

    private ImageSearchManager imageSearchManager;

    // 추가: 스크롤 관련 변수
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

        // RecyclerView 설정
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ImageSearchManager 초기화
        ImageSearchManager imageSearchManager = new ImageSearchManager(this);

        imageSearchManager.searchGoogleImages("cat").observe(this, resultItems -> {
            updateRecyclerView(resultItems);
        });

        // RecyclerView에 결과 업데이트
        imageSearchManager.searchGoogleImages("cat").observe(this, new Observer<List<ResultItem>>() {
            @Override
            public void onChanged(List<ResultItem> resultItems) {
                updateRecyclerView(resultItems);
            }
        });

        // 다시 촬영 버튼
        captureButton.setOnClickListener(v -> {
            // 촬영 화면으로 이동
            finish();
        });

        // 데이터 로딩 시뮬레이션
        showLoading(true);

        // RecyclerView 스크롤 리스너 추가
        resultsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 스크롤 거리와 이벤트 횟수 추적
                totalScrollDistance += dy; // 현재까지 스크롤한 거리
                if (dy > 0) scrollEventCount++; // 아래로 스크롤한 이벤트만 카운트

                // 조건: 스크롤 거리 500px 이상 또는 이벤트 3번 이상일 때 Snackbar 표시
                if (!isSnackbarShown && (totalScrollDistance > 1500 || scrollEventCount >= 10)) {
                    showSnackbar();
                    isSnackbarShown = true; // Snackbar는 한 번만 표시
                }
            }
        });
    }

    private void showSnackbar() {
        Snackbar.make(resultsRecyclerView, "텍스트 검색 결과 보기를 사용해보세요!", Snackbar.LENGTH_INDEFINITE)
                .setAction("열기", v -> {
                    // "열기" 버튼 클릭 시 동작
                    //Intent intent = new Intent(ResultActivity.this, TextSearchActivity.class); // 텍스트 검색 화면으로 이동
                    //startActivity(intent);
                    Log.d("ResultActivity","Text Search is selected");
                })
                .show();
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
