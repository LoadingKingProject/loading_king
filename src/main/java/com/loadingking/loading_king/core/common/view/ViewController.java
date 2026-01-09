package com.loadingking.loading_king.core.common.view;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/drive")
    public String drivePage() {
        return "drive";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }


}
