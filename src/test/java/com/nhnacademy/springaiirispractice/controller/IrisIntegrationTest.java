package com.nhnacademy.springaiirispractice.controller;

import com.nhnacademy.springaiirispractice.dto.IrisRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class IrisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;  // API 엔드포인트 테스트를 위한 MockMvc 객체

    @Autowired
    private ObjectMapper objectMapper;  // 객체를 JSON으로 직렬화하기 위한 ObjectMapper

    /**
     * Setosa 품종 예측 테스트
     * - Setosa 품종의 전형적인 특징값을 입력하여 예측 결과 검증
     * - 예측된 품종이 'Setosa'이며, 해당 품종에 대한 확률이 90% 이상인지 검증
     */
    @Test
    @DisplayName("Setosa 품종 예측 통합 테스트")
    void predictSetosaTest() throws Exception {
        // Setosa 품종의 대표적인 측정값으로 요청 객체 생성
        IrisRequest request = new IrisRequest(5.1, 3.5, 1.4, 0.2);

        mockMvc.perform(post("/api/iris/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())  // HTTP 200 상태 코드 검증
                .andExpect(jsonPath("$.predictedSpecies").value("Setosa"))  // 예측 품종 검증
                .andExpect(jsonPath("$.probabilities.Setosa").value(org.hamcrest.Matchers.greaterThan(0.9)));  // 확률 검증
    }

    /**
     * Versicolor/Virginica 품종 예측 테스트
     * - 두 품종의 특징이 겹치는 경계값을 입력하여 예측 결과 검증
     * - 모델의 학습 상태에 따라 두 품종 중 하나로 예측될 수 있음을 고려
     */
    @Test
    @DisplayName("Versicolor 또는 Virginica 품종 예측 통합 테스트")
    void predictVersicolorOrVirginicaTest() throws Exception {
        // Versicolor와 Virginica의 경계값 특성을 가진 데이터
        IrisRequest request = new IrisRequest(6.1, 2.8, 4.0, 1.3);

        mockMvc.perform(post("/api/iris/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictedSpecies").value("Versicolor"))
                .andExpect(jsonPath("$.probabilities.Versicolor").value(org.hamcrest.Matchers.greaterThan(0.8)));
    }

    /**
     * Virginica 품종 예측 테스트
     * - Virginica 품종의 전형적인 특징값을 입력하여 예측 결과 검증
     */
    @Test
    @DisplayName("Virginica 품종 예측 통합 테스트")
    void predictVirginicaTest() throws Exception {
        // Virginica 품종의 대표적인 측정값으로 요청 객체 생성
        IrisRequest request = new IrisRequest(6.3, 3.3, 6.0, 2.5);

        mockMvc.perform(post("/api/iris/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictedSpecies").value("Virginica"));
    }

    /**
     * 잘못된 입력값 처리 테스트
     * - 유효하지 않은 JSON 형식으로 요청을 보내 에러 처리 검증
     * - HTTP 400 Bad Request 응답 확인
     */
    @Test
    @DisplayName("잘못된 입력 값 테스트 (400 Bad Request 확인)")
    void predictBadRequestTest() throws Exception {
        mockMvc.perform(post("/api/iris/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sepalLength\": \"invalid\"}"))  // 잘못된 형식의 JSON
                .andExpect(status().isBadRequest());  // HTTP 400 상태 코드 검증
    }
}