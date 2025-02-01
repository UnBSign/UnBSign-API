package com.sign.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String getTest() {
        return "UnbSign-API is running successfully!";
    }

    @PostMapping
    public String postTest(@RequestBody String name) {
        return "Hello, " + name + "! POST request was successful!";
    }

}
