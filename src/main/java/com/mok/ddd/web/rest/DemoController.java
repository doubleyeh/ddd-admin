package com.mok.ddd.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class DemoController {

    @GetMapping("/demo")
    public RestResponse<String> demo() {
        return RestResponse.success("ok");
    }
}
