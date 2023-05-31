package org.vniizt.tabling.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Ilyin
 */

@Controller
public class RedirectionController {
    @GetMapping
    public String redirectHome(){
        return "redirect:/menu";
    }
}
