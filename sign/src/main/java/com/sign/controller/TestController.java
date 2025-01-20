package com.sign.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String getTest() {
        return "UnbSign-API is online successful!";
    }

    @PostMapping
    public String postTest(@RequestBody String name) {
        return "Hello, " + name + "! POST request was successful!";
    }
}
