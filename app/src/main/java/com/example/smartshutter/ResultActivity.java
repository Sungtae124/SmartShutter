package com.example.smartshutter;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private ProgressBar loadingSpinner;
    private RecyclerView resultsRecyclerView;
    private ImageButton captureButton;

    private ImageSearchManager imageSearchManager;

    private boolean isSnackbarShown = false; // Snackbar 표시 여부 확인
    private int totalScrollDistance = 0; // 스크롤 거리 추적
    private int scrollEventCount = 0; // 스크롤 이벤트 횟수 추적

    private List<ResultItem> resultItems; // 검색 결과 저장 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // UI 요소 초기화
        loadingSpinner = findViewById(R.id.loadingSpinner);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        captureButton = findViewById(R.id.captureButton);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (resultsRecyclerView.getAdapter() == null) {
            resultsRecyclerView.setAdapter(new ResultAdapter(new ArrayList<>())); // 빈 어댑터 설정
        }

        resultItems = new ArrayList<>(); // 검색 결과 리스트 초기화

        // 사진 경로 확인
        String photoPath = getIntent().getStringExtra("photoPath");
        if (photoPath != null) {
            File imageFile = new File(photoPath); // String을 File 객체로 변환
            if (imageFile.exists()) {
                Log.d("ResultActivity", "Image file exists: " + photoPath);
                // 촬영 후 분석 진행
                loadResults(imageFile); // 검색 결과 로드
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
        captureButton.setOnClickListener(v -> finish()); // 촬영 후 종료 (다시 촬영)

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

        // 이미지 분석 후 검색 결과를 가져오기
        imageSearchManager.analyzeImage(imageFile).observe(this, new Observer<List<ResultItem>>() {
            @Override
            public void onChanged(List<ResultItem> items) {
                if (items != null && !items.isEmpty()) {
                    resultItems.clear();
                    resultItems.addAll(items);
                    updateRecyclerView(resultItems);
                } else {
                    showSnackbarError("검색 결과가 없습니다.");
                }
                showLoading(false);
            }
        });
    }

    private void showSnackbar() {
        Snackbar.make(resultsRecyclerView, "텍스트 검색 결과 보기를 사용해보세요!", Snackbar.LENGTH_INDEFINITE)
                .setAction("열기", v -> Log.d("ResultActivity", "Text Search is selected"))
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
        if (results == null || results.isEmpty()) {
            results = new ArrayList<>(); // 빈 리스트로 초기화
            showSnackbarError("검색 결과가 없습니다."); // 에러 메시지 표시
        }
        ResultAdapter adapter = new ResultAdapter(results);
        resultsRecyclerView.setAdapter(adapter);
    }
}

/*
public class ResultActivity extends AppCompatActivity {

    private ProgressBar loadingSpinner;
    private RecyclerView resultsRecyclerView;
    private ImageButton captureButton;

    private ImageSearchManager imageSearchManager;

    private boolean isSnackbarShown = false; // Snackbar 표시 여부 확인
    private int totalScrollDistance = 0; // 스크롤 거리 추적
    private int scrollEventCount = 0; // 스크롤 이벤트 횟수 추적

    private List<ResultItem> resultItems; // 검색 결과 저장 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // UI 요소 초기화
        loadingSpinner = findViewById(R.id.loadingSpinner);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        captureButton = findViewById(R.id.captureButton);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (resultsRecyclerView.getAdapter() == null) {
            resultsRecyclerView.setAdapter(new ResultAdapter(new ArrayList<>())); // 빈 어댑터 설정
        }

        resultItems = new ArrayList<>(); // 검색 결과 리스트 초기화

        // 사진 경로 확인
        String photoPath = getIntent().getStringExtra("photoPath");
        if (photoPath != null) {
            File imageFile = new File(photoPath); // String을 File 객체로 변환
            if (imageFile.exists()) {
                Log.d("ResultActivity", "Image file exists: " + photoPath);
                loadResults(imageFile); // 검색 결과 로드
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
        captureButton.setOnClickListener(v -> finish());

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

        // RecyclerView 항목 클릭 리스너 설정
        resultsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, resultsRecyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        showImageDialog(resultItems.get(position)); // 클릭한 항목의 이미지를 다이얼로그로 표시
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // 필요 시 장기 클릭 처리
                    }
                }));
    }

    private void loadResults(File imageFile) {
        showLoading(true);

        // ImageSearchManager를 통해 검색 결과 가져오기
        imageSearchManager = new ImageSearchManager(this);

        imageSearchManager.analyzeImage(imageFile).observe(this, new Observer<List<ResultItem>>() {
            @Override
            public void onChanged(List<ResultItem> items) {
                if (items != null && !items.isEmpty()) {
                    resultItems.clear();
                    resultItems.addAll(items);
                    updateRecyclerView(resultItems);
                } else {
                    showSnackbarError("검색 결과가 없습니다.");
                }
                showLoading(false);
            }
        });
    }


    private boolean isValidImageUrl(String url) {
        try {
            new URL(url);  // URL이 유효한지 확인
            return true;
        } catch (MalformedURLException e) {
            Log.e("Image URL Error", "Invalid URL: " + url);
            return false;
        }
    }



    private void showSnackbar() {
        Snackbar.make(resultsRecyclerView, "텍스트 검색 결과 보기를 사용해보세요!", Snackbar.LENGTH_INDEFINITE)
                .setAction("열기", v -> Log.d("ResultActivity", "Text Search is selected"))
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
        if (results == null || results.isEmpty()) {
            results = new ArrayList<>(); // 빈 리스트로 초기화
            showSnackbarError("검색 결과가 없습니다."); // 에러 메시지 표시
        }
        ResultAdapter adapter = new ResultAdapter(results);
        resultsRecyclerView.setAdapter(adapter);
    }

    // 이미지를 Dialog로 표시
    private void showImageDialog(ResultItem item) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image_view);

        ImageView imageView = dialog.findViewById(R.id.dialog_image_view);

        // 결과 항목에서 Bitmap 가져와 설정
        Bitmap bitmap = item.getBitmap();
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // 기본 이미지 설정
        }

        dialog.show();
    }
}
*/