package dev.toy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToyController {

    @GetMapping("/hi")
    public String getHello(){
        return "Hi??";
    }

    @GetMapping("/hi2")
    public String getHello2(@RequestParam(name = "sec", defaultValue = "5") int sec ) throws InterruptedException{
        sec = Math.max(0, Math.min(sec, 120));
        Thread.sleep(sec * 1000L);
        return "Hi!";
    }

}
