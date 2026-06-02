package com.nhnacademy.springaiirispractice.controller;

import com.nhnacademy.springaiirispractice.dto.IrisRequest;
import com.nhnacademy.springaiirispractice.dto.IrisResponse;
import com.nhnacademy.springaiirispractice.service.IrisModelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IrisController.class)
class IrisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IrisModelService irisModelService;

    @Test
    @DisplayName("Iris 품종 예측 API 테스트")
    void predictTest() throws Exception {
        // given
        IrisRequest request = new IrisRequest(5.1, 3.5, 1.4, 0.2);
        IrisResponse response = new IrisResponse("Setosa", Map.of(
                "Setosa", 0.9,
                "Versicolor", 0.05,
                "Virginica", 0.05
        ));

        given(irisModelService.predict(any(IrisRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/iris/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.predictedSpecies").value("Setosa"))
                .andExpect(jsonPath("$.probabilities.Setosa").value(0.9));
    }
}