package org.example.gametgweb.gameplay.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class FrontController {
    @GetMapping("/")
    public String root() {
        return "redirect:/index.html"; // отдаёт статический файл
    }
}
