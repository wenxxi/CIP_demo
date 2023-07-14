package com.example.getip;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class GetIPController {
    @GetMapping("/input")
    public String InputIPform(@ModelAttribute("ip") IP x){
        return "input";
    }

    @PostMapping("/input")
    public String InputSave(@ModelAttribute("ip") IP ip) {
        ConfigUtils.saveIP(ip.getip());
        return "success";
    }

}
