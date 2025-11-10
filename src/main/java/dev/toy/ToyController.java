package dev.toy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToyController {

    @GetMapping("/hi")
    public String getHello(){
        return "Hi!";
    }

}
