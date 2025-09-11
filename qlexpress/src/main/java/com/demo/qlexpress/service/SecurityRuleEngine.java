package com.demo.qlexpress.service;

import com.alibaba.fastjson2.JSON;
import com.demo.qlexpress.config.QLExpressConfig;
import com.demo.qlexpress.model.LogEvent;
import com.demo.qlexpress.model.SecurityAlert;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SecurityRuleEngine {

    private final ExpressRunner runner;
    private final Set<String> blacklistedIPs;

    @Autowired
    public SecurityRuleEngine(QLExpressConfig config) {
        this.blacklistedIPs = config.blacklistedIPs();
        this.runner = new ExpressRunner();

        // 🔒 安全沙箱设置
        com.ql.util.express.RunStrategy.setForbiddenInvoke(true);
        com.ql.util.express.RunStrategy.setForbiddenMethodMode(true);
        com.ql.util.express.RunStrategy.setForbiddenOp(true);

        // ✅ 注册自定义函数
        try {
            runner.addFunction("isBlacklistedIP", new Operator() {
                @Override
                public Object executeInner(Object[] args) {
                    if (args.length == 0 || args[0] == null) return false;
                    String ip = args[0].toString();
                    return blacklistedIPs.contains(ip);
                }
            });

            runner.addFunction("containsKeyword", new Operator() {
                @Override
                public Object executeInner(Object[] args) {
                    if (args.length < 2) return false;
                    String text = args[0] == null ? "" : args[0].toString();
                    String keyword = args[1] == null ? "" : args[1].toString();
                    return text.contains(keyword);
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize QLExpress functions", e);
        }
    }

    public SecurityAlert evaluate(LogEvent event, String ruleName, String script) {
        DefaultContext<String, Object> context = new DefaultContext<>();
        context.put("userId", event.getUserId());
        context.put("action", event.getAction());
        context.put("ip", event.getIp());
        context.put("status", event.getStatus());
        context.put("userAgent", event.getUserAgent());
        context.put("resource", event.getResource());
        context.put("timestamp", event.getTimestamp());
        context.put("hour", event.getHour());
        context.put("failCount", event.getFailCount());

        try {
            Object result = runner.execute(script, context, null, false, false);

            boolean triggered = false;
            String alertType = "TRIGGERED";

            if (result instanceof Boolean && (Boolean) result) {
                triggered = true;
            } else if (result instanceof String && !((String) result).isBlank()) {
                triggered = true;
                alertType = (String) result;
            } else if (result instanceof Number && ((Number) result).doubleValue() > 0) {
                triggered = true;
                alertType = "RISK_SCORE:" + result;
            }

            if (triggered) {
                SecurityAlert alert = new SecurityAlert();
                alert.setRuleName(ruleName);
                alert.setAlertType(alertType);
                alert.setLogEvent(JSON.toJSONString(event));
                alert.setTriggeredAt(java.time.LocalDateTime.now());
                return alert;
            }

        } catch (Exception e) {
            System.err.println("❌ Rule execution failed [" + ruleName + "]: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}