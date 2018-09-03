package ru.abr.etp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ROOTController {

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }
}
