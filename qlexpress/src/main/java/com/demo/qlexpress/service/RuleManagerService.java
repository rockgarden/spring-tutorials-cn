package com.demo.qlexpress.service;

import com.demo.qlexpress.model.LogEvent;
import com.demo.qlexpress.model.SecurityAlert;
import com.demo.qlexpress.model.SecurityRule;
import com.demo.qlexpress.repository.SecurityRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RuleManagerService {

    @Autowired
    private SecurityRuleRepository ruleRepository;

    @Autowired
    private SecurityRuleEngine ruleEngine;

    private List<SecurityRule> activeRules = new CopyOnWriteArrayList<>();

    @Scheduled(initialDelay = 5000, fixedRate = 30000)
    public void reloadRules() {
        List<SecurityRule> rules = ruleRepository.findByEnabledTrue();
        activeRules.clear();
        activeRules.addAll(rules);
        System.out.println("✅ [RuleManager] Reloaded " + rules.size() + " active rules.");
    }

    public SecurityAlert processLogEvent(LogEvent event) {
        for (SecurityRule rule : activeRules) {
            SecurityAlert alert = ruleEngine.evaluate(event, rule.getName(), rule.getScript());
            if (alert != null) {
                System.out.println("🚨 ALERT: " + alert.getAlertType() + " by rule: " + alert.getRuleName());
                return alert; // 可改为收集所有命中
            }
        }
        return null;
    }

    public List<SecurityRule> getActiveRules() {
        return activeRules;
    }
}