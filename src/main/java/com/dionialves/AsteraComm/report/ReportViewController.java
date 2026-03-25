package com.dionialves.AsteraComm.report;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportViewController {

    @GetMapping
    public String index() {
        return "pages/reports/index";
    }
}
