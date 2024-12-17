package com.example.smartshutter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // 권한 확인 및 카메라 시작
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        findViewById(R.id.captureButton).setOnClickListener(v -> takePhoto());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) { // CAMERA 권한 요청 코드
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우
                startCamera();
            } else {
                // 권한이 거부된 경우
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish(); // 권한 거부 시 앱 종료
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // CameraProvider 초기화
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                PreviewView previewView = findViewById(R.id.previewView);

                // Preview 설정
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // ImageCapture 설정
                imageCapture = new ImageCapture.Builder().build();

                // 후면 카메라 선택
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // 카메라와 Lifecycle 바인딩
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                Log.d("CameraX", "Camera initialized successfully");
            } catch (Exception e) {
                Log.e("CameraX", "카메라 초기화 실패", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void takePhoto() {
        Log.d("CameraActivity", "takePhoto() called");
        // 유니크 파일 이름 생성
        String fileName = generateUniqueFileName();
        File photoFile = new File(getExternalFilesDir(null), fileName);

        Log.d("CameraActivity", "Photo will be saved at: " + photoFile.getAbsolutePath());

        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(ImageCapture.OutputFileResults output) {
                runOnUiThread(() -> {
                    Toast.makeText(CameraActivity.this, "사진 저장 성공: " + fileName, Toast.LENGTH_SHORT).show();
                    releaseCameraResources(() -> {
                        Log.d("CameraActivity", "Photo saved successfully: " + photoFile.getAbsolutePath());
                        // ResultActivity로 전환
                        Intent intent = new Intent(CameraActivity.this, ResultActivity.class);
                        intent.putExtra("photoPath", photoFile.getAbsolutePath());
                        startActivity(intent);
                        finish();
                    });
                });
            }

            @Override
            public void onError(ImageCaptureException error) {
                // 사진 촬영 실패
                runOnUiThread(() -> Toast.makeText(CameraActivity.this, "사진 촬영 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e("CameraX", "사진 촬영 실패", error);
            }
        });
    }

    private void releaseCameraResources(Runnable onReleaseComplete) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                Log.d("CameraX", "카메라 리소스 해제 완료");
                if (onReleaseComplete != null) {
                    onReleaseComplete.run();
                }
            } catch (Exception e) {
                Log.e("CameraX", "카메라 리소스 해제 실패", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private String generateUniqueFileName() {
        // 날짜와 시간을 기반으로 유니크한 파일 이름 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "SS_" + timeStamp + ".jpg";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
