package com.tgbot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @RequestMapping(value = "/health-check", method = RequestMethod.HEAD)
    public String check() {
        return "OK";
    }
}
