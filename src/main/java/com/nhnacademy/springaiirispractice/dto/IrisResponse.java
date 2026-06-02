package com.nhnacademy.springaiirispractice.dto;

import lombok.Value;

import java.util.Map;

/**
 * 예측 결과
 * 서버가 반환하는 예측 결과
 * 머신러닝 모델이 예측한 품종 이름과 각 품종 별 확률 분포를 담는 객체
 * AI 모델의 예측 결과 및 확률 분포 데이터
 */
@Value // 데이터의 무결성을 위해 @Value 사용하여 불변 객체로
public class IrisResponse {

    String predictedSpecies; // 예측된 품종명 (예: Setosa)
    Map<String, Double> probabilities; // 각 품종 별 확률 (예: {Setosa: 0.95, ...})
}