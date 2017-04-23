package com.example.controllor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by 40383 on 2017/4/20.
 */
@Controller
public class IndexController {
    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return "index";
    }

    @GetMapping("/hw")
    public String helloWorld() {
        return "helloWorld";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }
}
