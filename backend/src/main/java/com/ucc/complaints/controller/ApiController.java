package com.ucc.complaints.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ApiController {
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
