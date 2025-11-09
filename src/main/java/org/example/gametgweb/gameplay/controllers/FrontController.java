package org.example.gametgweb.gameplay.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontController {
    @GetMapping("/")
    public String root() {
        return "redirect:/index.html"; // отдаёт статический файл
    }
}
