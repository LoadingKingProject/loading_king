package com.loadingking.loading_king.core.common.view;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    private static final String APP_VERSION = String.valueOf(System.currentTimeMillis());

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("appVersion", APP_VERSION);
        return "login";
    }

    @GetMapping("/drive")
    public String drivePage(Model model) {
        model.addAttribute("appVersion", APP_VERSION);
        return "drive";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }


}
