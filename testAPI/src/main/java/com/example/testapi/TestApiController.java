package com.example.testapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TestApiController {
    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/input")
    public String InputForm(@ModelAttribute Input input) {
        return "Input";
    }

    @PostMapping("/input")
    public String InputSave(@ModelAttribute Input input) {
        User n = new User();
        n.setID(input.getID());
        n.setName(input.getName());
        n.setGender(input.getGender());
        n.setBirthdate(input.getBirthdate());
        userRepository.save(n);
        return "Success";
    }

    @GetMapping("/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }
}


