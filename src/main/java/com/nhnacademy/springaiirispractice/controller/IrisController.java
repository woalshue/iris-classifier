package com.nhnacademy.springaiirispractice.controller;

import com.nhnacademy.springaiirispractice.dto.IrisRequest;
import com.nhnacademy.springaiirispractice.dto.IrisResponse;
import com.nhnacademy.springaiirispractice.service.IrisModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스의 모든 응답은 제이슨 형태로 반환됨
@RequestMapping("/api/iris")
@RequiredArgsConstructor
public class IrisController {

    private final IrisModelService irisModelService;

    @PostMapping("/predict")
    public IrisResponse predict(@RequestBody IrisRequest request) {

        // @RequestBody: 클라가 보낸 제이슨 데이터를 자바의 IrisRequest 객체로 자동으로 변환해줌

        // 클라로부터 전달받은 제이슨 데이터를 IrisRequest 객체로 매핑하여 처리
        return this.irisModelService.predict(request);
    }
}