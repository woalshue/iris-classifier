# Iris Classifier

Spring Boot와 Deeplearning4j(DL4J)를 활용하여 붓꽃(Iris) 품종을 분류하는 로컬 머신러닝 웹 서비스입니다.

## 개요

붓꽃(Iris) 데이터셋의 4가지 수치 특징(꽃받침·꽃잎의 길이와 너비)을 입력받아 신경망 모델이 3가지 품종(Setosa, Versicolor, Virginica) 중 하나로 분류합니다. 서버 최초 실행 시 모델이 없으면 자동으로 학습을 진행하고 `model/iris-model.zip`으로 저장하며, 이후 실행에서는 저장된 모델을 재사용합니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| ML Engine | Deeplearning4j (DL4J) 1.0.0-M2.1 |
| Numeric Backend | ND4J Native (CPU) |
| Template Engine | Thymeleaf |
| Build Tool | Maven |
| Utility | Lombok |
| Frontend | Chart.js |

## 신경망 구조

```
입력층 (4)  →  은닉층 (10, ReLU)  →  출력층 (3, Softmax)
```

| 설정 항목 | 값 |
|----------|-----|
| 입력 피처 | 4개 (sepalLength, sepalWidth, petalLength, petalWidth) |
| 은닉층 뉴런 수 | 10 |
| 활성화 함수 (은닉층) | ReLU |
| 활성화 함수 (출력층) | Softmax |
| 손실 함수 | Negative Log-Likelihood |
| 가중치 초기화 | Xavier |
| 옵티마이저 | SGD (학습률 0.1) |
| 에폭 | 300 |
| 배치 크기 | 150 (Full Batch) |
| 랜덤 시드 | 123 |

## 프로젝트 구조

```
spring-ai-iris-practice/
├── model/
│   └── iris-model.zip                  # 학습된 모델 파일 (최초 실행 시 자동 생성)
├── http/
│   └── iris-test.http                  # API 테스트 예제
├── src/
│   ├── main/
│   │   ├── java/com/nhnacademy/springaiirispractice/
│   │   │   ├── SpringAiIrisPracticeApplication.java
│   │   │   ├── config/
│   │   │   │   ├── ModelProperties.java          # iris.model-path 설정 바인딩
│   │   │   │   └── MultiLayerNetworkConfig.java  # MultiLayerNetwork Spring Bean 등록
│   │   │   ├── controller/
│   │   │   │   ├── IrisController.java           # REST API (POST /api/iris/predict)
│   │   │   │   └── IrisViewController.java       # 웹 UI (GET /)
│   │   │   ├── dto/
│   │   │   │   ├── IrisRequest.java              # 요청 DTO (4개 수치)
│   │   │   │   └── IrisResponse.java             # 응답 DTO (품종명 + 확률 맵)
│   │   │   └── service/
│   │   │       └── IrisModelService.java         # 모델 학습·저장·예측 로직
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── static/js/iris-predict.js         # 예측 요청 및 Chart.js 렌더링
│   │       └── templates/index.html              # 대시보드 UI
│   └── test/
│       └── java/com/nhnacademy/springaiirispractice/
│           ├── SpringAiIrisPracticeApplicationTests.java
│           └── controller/
│               ├── IrisControllerTest.java       # MockMvc 단위 테스트
│               └── IrisIntegrationTest.java      # 전체 컨텍스트 통합 테스트
└── pom.xml
```

## 실행 방법

**요구 사항:** JDK 21, Maven

```bash
# 프로젝트 루트에서 실행
./mvnw spring-boot:run
```

서버가 기동되면 `http://localhost:8080` 에서 웹 UI에 접근할 수 있습니다.

> 모델 파일(`model/iris-model.zip`)이 없으면 최초 실행 시 자동으로 학습을 수행합니다. 학습에 수십 초가 소요될 수 있습니다.

## API

### POST /api/iris/predict

붓꽃 품종을 예측합니다.

**Request**

```json
{
  "sepalLength": 5.1,
  "sepalWidth": 3.5,
  "petalLength": 1.4,
  "petalWidth": 0.2
}
```

**Response**

```json
{
  "predictedSpecies": "Setosa",
  "probabilities": {
    "Setosa": 0.95,
    "Versicolor": 0.03,
    "Virginica": 0.02
  }
}
```

### 예제 데이터

| 품종 | sepalLength | sepalWidth | petalLength | petalWidth |
|------|------------|------------|------------|------------|
| Setosa | 5.1 | 3.5 | 1.4 | 0.2 |
| Versicolor | 6.1 | 2.8 | 4.0 | 1.3 |
| Virginica | 6.3 | 3.3 | 6.0 | 2.5 |

## 웹 UI 기능

- **예제 데이터 입력**: 품종별 버튼 클릭 시 대표 측정값이 자동 입력되고 즉시 예측
- **실시간 예측**: '예측하기' 버튼으로 서버의 DL4J 모델에 추론 요청
- **결과 시각화**: 예측 품종명과 함께 3개 품종별 확률 분포를 Chart.js 막대 그래프로 표시

## 애플리케이션 동작 흐름

```
Spring Boot 구동 완료
  └─ ApplicationReadyEvent → initModel() 호출
       ├─ model/iris-model.zip 존재 시 → 학습 건너뜀
       └─ 없을 시 → trainAndSaveModel()
            ├─ IrisDataSetIterator로 150개 데이터 로드
            ├─ 신경망 설계 (4 → 10 → 3)
            ├─ 300 에폭 학습 (SGD, ReLU, Xavier)
            └─ ModelSerializer로 모델 저장

예측 요청 (POST /api/iris/predict)
  └─ IrisRequest (4개 수치)
       └─ double[][] → INDArray 변환
            └─ model.output() → 확률 행렬 (1×3)
                 └─ 최대 확률 인덱스 → 품종명 + 확률 맵 → IrisResponse
```

## 테스트

```bash
./mvnw test
```

| 테스트 클래스 | 종류 | 설명 |
|------------|------|------|
| `IrisControllerTest` | 단위 테스트 | `@WebMvcTest` + Mockito로 컨트롤러 레이어 검증 |
| `IrisIntegrationTest` | 통합 테스트 | `@SpringBootTest`로 실제 모델을 사용한 예측 결과 검증 |

## 설정

`src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: spring-ai-iris-practice

iris:
  model-path: model/iris-model.zip  # 학습된 모델 파일 경로
```
