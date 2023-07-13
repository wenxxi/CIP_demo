package com.example.getip;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class GetIPController {
    @GetMapping("/inputip")
    public String InputIPform(@ModelAttribute InputIP ipinput){
        ipinput.IPaddress
        return "inputip";
    }

    @PostMapping("/inputip")
    public String InputSave(@ModelAttribute InputIP ipinput) {
        ConfigUtils.saveIP(ipinput.getIP());
        return "inputip";
    }

}
