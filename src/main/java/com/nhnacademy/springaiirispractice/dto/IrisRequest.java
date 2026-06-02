package com.nhnacademy.springaiirispractice.dto;

import lombok.Value;


/**
 * 입력 수치
 * 사용자가 입력하는 데이터
 * 사용자가 입력한 붓꽃의 수치 정보(꽃받침, 꽃잎의 길이와 너비)를 담는 객체
 * 클라이언트로부터 전달받는 붓꽃 특징 데이터
 */
@Value // 데이터의 무결성을 위해 @Value 사용하여 불변 객체로
public class IrisRequest {

    // 붓꽃(Iris) 예측에 필요한 4가지 특징(Feature)
    double sepalLength; // 꽃받침 길이
    double sepalWidth; // 꽃받침 너비
    double petalLength; // 꽃잎 길이
    double petalWidth; // 꽃잎 너비
}