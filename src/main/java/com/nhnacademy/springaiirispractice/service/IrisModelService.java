package com.nhnacademy.springaiirispractice.service;


import com.nhnacademy.springaiirispractice.config.ModelProperties;
import com.nhnacademy.springaiirispractice.dto.IrisRequest;
import com.nhnacademy.springaiirispractice.dto.IrisResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 서버가 시작될 때 모델이 없으면 자동으로 학습을 진행함
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IrisModelService {

    private final ModelProperties modelProperties;

    @Setter
    private MultiLayerNetwork model; // 학습된 신경망 객체

    /*
    Spring Boot가 완전히 구동된 직후(모든 Bean 초기화 완료 후) 자동 실행
    @PostConstruct 보다 늦은 시점 -> 모든 준비가 끝난 후 실행되므로 안전함
    서버 시작 직후 실행 (애플리케이션 구동이 완료된 시점에 initModel() 메서드를 자동으로 호출함)

    흐름: 서버 시작 -> ApplicationReadyEvent 발생 -> initModel() 호출
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initModel() throws IOException {

        File modelFile = new File(this.modelProperties.getModelPath());

        // 중복 학습 방지 위해 파일 존재 여부를 먼저 확인
        if (modelFile.exists()) {
            log.info("[IrisModelService] 학습된 모델이 이미 존재합니다. 자동 학습을 건너뜁니다.");
            return;
        }

        log.info("[IrisModelService] 모델 파일이 없습니다. 학습을 시작합니다...");
        this.trainAndSaveModel();
    }

    // 학습, 저장 로직
    private void trainAndSaveModel() throws IOException {

        /**
         * 1. 데이터 준비 (DataSetIterator)
         *         IrisDataSetIterator: DL4J 내장 Iris 데이터셋 로더
         *         첫 번째 인자 150: 전체 샘플 수 (Iris 데이터셋은 총 150개)
         *         두 번째 인자 150: 배치 사이즈 (배치 사이즈를 150으로 했으니, 전체를 한 번에 -> 풀 배치 학습 시킨다는 거임)
         */
        DataSetIterator trainIter = new IrisDataSetIterator(150, 150);

        // 2. 신경망 설계 (Configuration)
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123) // 재현성 보장 (랜덤 시드 고정) -> 매번 같은 결과를 위해 난수 고정
                .activation(Activation.RELU) // 은닉층 기본 활성화 함수
                .weightInit(WeightInit.XAVIER) // 가중치 초기화 전략 (Xavier: 층이 깊어져도 기울기 소실 방지함)
                .updater(new Sgd(0.1)) // 옵티마이저: SGD, 학습률: 0.1 (Stochastic Gradient Descent 1r = 0.1 (확률적 경사하강법으로 가중치 업뎃))
                .list()
                .layer(new DenseLayer.Builder().nIn(4).nOut(10).build()) // 은닉층(데이터의 피쳐 찾아냄) (nIn(4): 입력 데이터(꽃잎/꽃받침 길이, 너비)가 4개임) (nOut(10): 이 층을 통과하면서 10개의 추상적인 피쳐로 변환됨)
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD) // 출력층 로스함수: NEGATIVELOGLIKELIHOOD (다중 클래시피케이션에 적합 (소프트맥스와 함께 사용))
                        .activation(Activation.SOFTMAX) // 출력층 활성화함수: 소프트맥스 (3개 클래스(Setosa, Versicolor, Virginica)의 확률 합이 1.0)
                        .nIn(10).nOut(3).build()) // 출력층(nOut(3): 맞히려는 품종 3종류니까)
                .build();

        // 3. 모델 초기화 및 학습
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init(); // 위에서 설계한 conf 기반으로 가중치 초기화

        for (int i = 0; i < 300; i++) { // 에폭 300번 돌림 (반복 학습)
            model.fit(trainIter); // 매 에폭마다 전체 데이터로 순전파 + 역전파 수행
        }

        /**
         * 4. 모델 저장 (Serialization)
         *         ModelSerializer.writeModel(): 학습된 모델을 파일로 직렬화
         *         true: Updater 상태도 함께 저장 (이후 추가 학습 가능)
         *         저장 경로는 ModelProperties 에서 관리
         */
        ModelSerializer.writeModel(model, new File(this.modelProperties.getModelPath()), true);
        log.info("[IrisModelService] 모델 학습 및 저장이 완료되었습니다: {}", this.modelProperties.getModelPath());
    }

    /**
     * 사용자의 입력 데이터를 기반으로 붓꽃 품종을 예측
     * IrisRequest (4개 수치)
     * -> 모델 로드 확인
     * -> double[][] -> INDArray (행렬 변환)
     * -> model.output() -> 신경망 통과 -> 확률 행렬
     * -> 확률 해석 -> 최대값 인덱스 찾기
     * -> IrisResponse (품종명 + 확률 맵)
     */
    public IrisResponse predict(IrisRequest request) {

        if (Objects.isNull(this.model)) {
            throw new IllegalArgumentException("모델이 로드되지 않았습니다.");
        }

        /**
         * 입력을 INDArray(행렬)로 변환 (1행 4열)
         * ND4j.create(): 자바의 기본 double 데이터를 딥러닝 엔진이 이해할 수 있는 행렬(INDArray)로 변환 (4개의 피쳐를 입력받으므로 1x4 행렬 생성)
         *
         * double[][] -> 1행 4열 행렬
         * 1행 (샘플 1개)
         * 4열 (피쳐 4개)
         *
         * double[][]를 쓰는 이유: 배치 처리 형태 (여러 샘플을 한 번에 넣을 수 있는 구조 (지금은 1개))
         * 신경망의 입력층 nIn(4) 와 열 개수가 일치해야 함
         */
        INDArray input = Nd4j.create(new double[][]{{
                request.getSepalLength(),
                request.getSepalWidth(),
                request.getPetalLength(),
                request.getPetalWidth()
        }});

        /**
         * 모델에 입력을 넣어 결과를 얻음 (Interface)
         * 신경망에 데이터를 통과시켜 결과를 얻음
         * 결과값은 각 품종일 확률을 담은 행렬로 반환됨
         *
         * input (1 by 4 행렬)
         * -> 히든레이어에서 4 -> 10 (렐루)
         * -> 아웃풋레이어에서 10 -> 3 (소프트맥스)
         * -> output (1 by 3 행렬): 각 품종의 확률값
         *
         * [[0.95, 0.03, 0.02]]
         *    ↑      ↑     ↑
         * Setosa  Versicolor  Virginica
         *
         * 소프트맥스는 세 값의 합은 항상 1.0
         * 가장 높은 값이 모델이 예측한 품종
         */
        INDArray output = this.model.output(input);

        /**
         * 결과 해석 (가장 높은 확률을 가진 인덱스 찾기)
         * output.toDoubleVector(): INDArray -> 자바의 double[]로 변환 ([0.95, 0.03, 0.02] 형태)
         *
         * labels 배열 인덱스 매핑 (학습 시 IrisDataSetIterator의 레이블 순서와 반드시 일치해야 함):
         * index 0 -> "Setosa" -> probabilities[0]
         * index 1 -> "Versicolor" -> probabilities[1]
         * index 2 -> "Virginica" -> probabilities[2]
         *
         * 루프:
         * i=0: map에 {Setosa: 0.95}, 0.95 > 0.95? NO  → maxIndex = 0
         * i=1: map에 {Versicolor: 0.03}, 0.03 > 0.95? NO  → maxIndex = 0
         * i=2: map에 {Virginica: 0.02}, 0.02 > 0.95? NO  → maxIndex = 0
         *
         * 최종 maxIndex = 0 → "Setosa"
         */
        double[] probabilities = output.toDoubleVector();
        String[] labels = {"Setosa", "Versicolor", "Virginica"};

        int maxIndex = 0;
        Map<String, Double> probabilitiesMap = new HashMap<>();
        for (int i = 0; i < labels.length; i++) {
            probabilitiesMap.put(labels[i], probabilities[i]);

            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }

        /**
         * labels[maxIndex]: 가장 확률 높은 품종명
         * probabilitiesMap: 3개 품종 전체 확률
         *
         * {
         *   "predicatedSpecies": "Setosa",
         *   "probabilities": {
         *     "Setosa": 0.95,
         *     "Versicolor": 0.03,
         *     "Virginica": 0.02
         *   }
         * }
         */
        return new IrisResponse(labels[maxIndex], probabilitiesMap);
    }
}

/**
 * 전체 흐름:
 * Spring Boot 구동 완료
 * -> initModel() 자동 실행
 * -> 모델 파일 없으면 trainAndSaveModel()
 * -> IrisDataSetIterator 로 데이터 로드
 * -> NeuralNetConfiguration으로 신경망 설계 (4 -> 10 -> 3)
 * -> 300 에폭 돌림 (SGD, ReLU, Xavier)
 * -> ModelSerializer로 파일 저장
 * -> 이후 예측 요청 시 저장된 모델 사용
 */