package com.nhnacademy.springaiirispractice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 웹 페이지 보여주기 위한
@Controller
public class IrisViewController {

    @GetMapping("/")
    public String index() {
        return "index"; // templates/index.html 찾아서 렌더링함
    }
}