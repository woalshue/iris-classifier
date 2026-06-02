package com.nhnacademy.springaiirispractice.config;

import com.nhnacademy.springaiirispractice.service.IrisModelService;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

// 모델 파일을 읽어 Spring Bean으로 등록하는 설정 클래스
// @Configuration + @Bean: 스프링이 뜰 때 multiLayerNetwork() 메서드를 호출하고, 반환된 모델 객체를 스프링 컨테이너가 관리
@Configuration
@RequiredArgsConstructor
public class MultiLayerNetworkConfig {

    /**
     * 순환 참조 해결:
     * IrisModelService는 MultiLayerNetwork가 필요하고, MultiLayerNetwork 설정 과정에서는 IrisModelService가 필요할 수 있음
     * 이를 해결하기 위해 서비스에서 MultiLayerNetwork model에 final을 제거하고 세터를 통해 나중에 모델을 전달받도록 함
     */

    private final IrisModelService irisModelService; // 학습(initModel)을 위해 주입
    private final ModelProperties modelProperties; // 모델 파일 경로를 알기 위해 주입

    @Bean
    public MultiLayerNetwork multiLayerNetwork() throws IOException {

        /**
         * 서비스의 모델 초기화(학습) 메서드 호출
         * 모델 파일이 없으면 -> 학습 후 저장
         * 모델 파일이 있으면 -> 그냥 리턴 (중복 학습 방지)
         * 파일을 읽기 전에 반드시 학습이 완료되어야 하므로 가장 먼저 호출
         */
        this.irisModelService.initModel();

        File modelFile = new File(this.modelProperties.getModelPath());

        MultiLayerNetwork model = null;
        if(modelFile.exists()) {
            // 저장된 모델 파일(zip)을 읽어와서 MultiLayerNetwork 객체로 복원 (역직렬화)
            model = ModelSerializer.restoreMultiLayerNetwork(modelFile);
        }

        // 서비스에 로드된 모델을 설정해줌 (주입)
        this.irisModelService.setModel(model);

        /**
         * Bean으로 반환
         * MultiLayerNetwork 객체가 Spring Bean으로 등록됨
         * 이후 다른 클래스에서 이를 주입받아서 사용 가능
         */
        return model;
    }
}