# SmartShutter
SmartShutter는 셔터를 누르는 순간, 원하는 정보를 즉시 찾아주는 이미지 기반 검색 앱입니다.

## **1. 프로젝트 개요**

### **1.1 프로젝트 목적**

**스마트셔터(SmartShutter)** 프로젝트는 사용자가 사진을 촬영하고 이를 기반으로 **이미지 분석** 및 **검색**을 수행하는 애플리케이션입니다. 사용자가 촬영한 이미지를 분석하여 텍스트, 로고, 라벨과 같은 의미 있는 데이터를 추출하고, 이를 통해 **맞춤형 검색 결과**를 제공하는 것을 목표로 합니다.

### **1.2 기대 효과**

- **편의성 제공**: 사용자가 사진만 촬영하면 직관적으로 검색 및 정보를 탐색할 수 있음.
- **이미지 기반 검색**: 시각적 데이터를 텍스트로 변환해 실제 필요 정보를 제공.
- **개선된 사용자 경험**: 분석 결과에 기반한 **정확하고 최적화된 추천** 제공.

### **1.3 주요 기능**

1. **CameraX 기반 사진 촬영**
2. **Google Vision API를 통한 이미지 분석**
    - 텍스트 인식
    - 로고 인식
    - 라벨 추출
    - 웹 이미지 기반 인식
3. **구글 Knowledge Graph 기반으로 Vision API 응답을 통해 추출한 키워드의 조합을 연관 관계 파악 후 검색 API에 전달**
4. **Google Custom Search API를 통한 이미지 및 웹 검색**
5. **결과 처리 및 키워드 기반 검색 쿼리 생성**

### 1.4 개발 도구 : Android Studio - JAVA, API 34

---

## **2. 시스템 아키텍처**

### **2.1 전체 흐름도**

1. **CameraX**를 이용한 **사진 촬영**
2. 촬영된 이미지를 내부 저장소에 저장 
3. **Google Vision API**에 이미지 전달 → 텍스트, 라벨, 로고 분석
4. 분석된 결과를 **데이터 필터링** 및 병합
5. 생성된 키워드 기반으로 외부 검색 **API 쿼리** 생성 및 결과 반환
---

## **3. 구현 현황**

### **3.1 카메라 기능 (CameraX)**

### **기능 설명**

- **CameraX 라이브러리**를 사용하여 카메라 기능 구현
- 프리뷰 화면(`PreviewView`)에 실시간 화면 표시
- 버튼 클릭 시 사진 촬영 후 파일 저장

### **주요 코드**

```java
ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
    @Override
    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
        Log.d("CameraX", "Photo saved: " + file.getAbsolutePath());
    }

    @Override
    public void onError(@NonNull ImageCaptureException exception) {
        Log.e("CameraX", "Photo capture failed: " + exception.getMessage());
    }
});

```

### **개선 방안**

- 촬영 후 프리뷰 화면에 **이미지 썸네일** 제공
- **카메라 리소스 관리 최적화** (오류 방지를 위해 리소스 즉시 해제)

---

### **3.2 이미지 저장 및 경로 관리**

### **기능 설명**

- 촬영된 이미지는 내부 저장소에 시간 기준으로 저장
- 파일 경로를 로그로 출력 및 검증

### **주요 로그 출력**

```
Photo will be saved at: /storage/emulated/0/Android/data/com.example.smartshutter/files/SS_20241218_010308.jpg

```

---

### **3.3 이미지 분석 (Google Vision API)**

### **기능 설명**

- Google Vision API를 통해 텍스트, 로고, 라벨 데이터 추출

### **분석 결과 예시**

1. **텍스트 인식**
    
    ```
    Extracted Texts: [Samsung]
    
    ```
    
2. **로고 인식**
    
    ```
    Extracted Logos: [Samsung]
    
    ```
    
3. **라벨 인식**
    
    ```
    Extracted Labels: [electronic device, technology, silver, plastic]
    
    ```
    

### **결과 병합 및 쿼리 생성**

- 분석 결과를 기반으로 **중복 제거 및 조합**하여 검색 쿼리 생성

```
Generated Queries:
- Samsung technology
- Samsung plastic
- Samsung electronic device

```

### **개선 방안**

- **정확도 향상**: Vision API 분석 결과의 정밀도 개선을 위해 **Confidence Threshold** 설정 추가
- **비용 절감**: Google Vision API 호출 시 불필요한 중복 호출 방지

---

## **4. 차별점**

1. **CameraX 기반 실시간 프리뷰 및 고속 사진 촬영**
2. **다중 이미지 분석**: 텍스트, 라벨, 로고 분석 결과 병합
3. **지능형 검색 쿼리 생성**: 이미지 기반으로 키워드를 자동 생성해 맞춤형 검색 가능
4. **사용자 편의성**: 촬영-저장-분석-검색의 **단일 워크플로우** 구축

---

## **5. 주요 이슈 및 해결 방안**

### **5.1 카메라 리소스 문제**

- **이슈**: 카메라 리소스를 즉시 해제하지 않으면 **메모리 누수** 발생
- **해결**: 사진 촬영 후 `CameraX` 리소스 명확히 해제

### **5.2 비정상 URL 반환**

- **이슈**: Vision API 분석 결과에 유효하지 않은 URL 포함, 결과 페이지에 이미지 표시가 안되는 경우 발생.
- **해결 방안**:
    - URL 유효성 검사 추가
    - 불필요한 URL은 필터링
        

### 5.3 검색 키워드 설정

- **이슈**: 검색 키워드가 촬영된 사진에 대해 너무 포괄적인 정보를 포함
- **예시**: 마우스 사진 촬영 시 설정되는 키워드가 PC Components, mouse 정도..
- **해결 방안**:
    - Google Vision API 활용 → Label 외에도 Web Detection, Logo Detection, OCR 등의 키워드 추출 기능을 종합하여 키워드 생성
    - Knowledge Graph 활용 키워드 연관관계 파악 후 이미지 검색 및 구글 웹 검색 API에 전달
    - 설정된 키워드에 대한 응답을 효과적으로 반환 가능.

---

## **6. 향후 계획**

1. **사용자 인터페이스 개선**
    - 분석 결과를 시각적으로 표시 (이미지와 함께 텍스트 제공)
    - 썸네일 및 세부 정보 화면 추가
2. **정확도 향상**
    - 다른 분석 도구(예: OpenCV, Tesseract) 추가 검토
    - Confidence 점수를 기반으로 유효한 데이터만 필터링
3. **검색 기능 강화**
    - 이미지 기반 검색 API 연동 (예: Google Custom Search API)
    - 사용자 선호도에 따른 **추천 알고리즘** 적용
4. **성능 최적화**
    - 이미지 처리 속도 개선 (멀티스레딩 활용)
    - Vision API 호출 비용 최적화
