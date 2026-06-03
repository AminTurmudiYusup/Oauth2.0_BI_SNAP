package com.dumy.emoney.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CallBackController {
    @GetMapping("/callback")
    public String callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            Model model
    ) {
        model.addAttribute("code", code);
        model.addAttribute("state", state);
        model.addAttribute("error", error);

        return "callback"; // never throws → no 500
    }
}
