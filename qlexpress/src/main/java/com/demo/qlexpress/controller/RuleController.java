package com.demo.qlexpress.controller;

import com.demo.qlexpress.model.LogEvent;
import com.demo.qlexpress.model.SecurityAlert;
import com.demo.qlexpress.model.SecurityRule;
import com.demo.qlexpress.service.RuleManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @Autowired
    private RuleManagerService ruleManager;

    @PostMapping("/analyze")
    public SecurityAlert analyzeLog(@RequestBody LogEvent event) {
        System.out.println("📥 Received log event: " + event);
        return ruleManager.processLogEvent(event);
    }

    @GetMapping("/active")
    public List<SecurityRule> listActiveRules() {
        return ruleManager.getActiveRules();
    }

    @GetMapping("/reload")
    public String reloadRules() {
        ruleManager.reloadRules();
        return "✅ Rules reloaded successfully!";
    }
}