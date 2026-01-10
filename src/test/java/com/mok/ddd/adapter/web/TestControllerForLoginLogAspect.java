package com.mok.ddd.adapter.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestControllerForLoginLogAspect {

    @GetMapping("/test-success")
    public String testSuccess() {
        return "success";
    }

    @GetMapping("/test-failure")
    public void testFailure() {
        throw new RuntimeException("failure");
    }
}