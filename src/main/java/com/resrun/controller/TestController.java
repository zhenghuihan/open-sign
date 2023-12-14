package com.resrun.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : zhenghuihan
 * create at:  2023/12/7  15:09
 * @description:
 */
@RestController
@RequestMapping("/api")
public class TestController {

    /**
     * @description 测试
     */
    @GetMapping(value = "/test")
    public String test() {
        return "ok";
    }

}