package com.example.carphone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/move-car")
    public String moveCar() {
        return "forward:/index.html";
    }
}