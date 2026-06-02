package com.nhnacademy.springaiirispractice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// 모델 경로와 같은 설정을 외부 설정 파일(yml)에서 관리하기 위해
@ConfigurationProperties(prefix = "iris")
@Component
@Getter
@Setter
public class ModelProperties {

    private String modelPath;
}