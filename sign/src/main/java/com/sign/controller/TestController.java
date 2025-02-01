package com.sign.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String getTest() {
        return "UnbSign-API is running successfully!";
    }

    @PostMapping("/{name}")
    public String postTest(@PathVariable String name) {
        return "Hello, " + name + "! POST request was successful!";
    }

}
