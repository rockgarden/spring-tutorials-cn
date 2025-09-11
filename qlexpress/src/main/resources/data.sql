USE security_rule_db;

INSERT INTO security_rules (name, script, description, risk_level, enabled) VALUES
('暴力登录检测', 'action == "LOGIN" && status == "FAILED" && failCount >= 3', '5分钟内同一IP失败登录3次以上', 5, TRUE),
('敏感操作非管理员', 'action in ("DELETE_USER", "MODIFY_ROLE", "SHUTDOWN") && userId != "admin"', '非管理员执行高危操作', 4, TRUE),
('渗透工具检测', 'userAgent != null && (userAgent.indexOf("sqlmap") > -1 || userAgent.indexOf("nmap") > -1 || userAgent.indexOf("burp") > -1)', '检测常见渗透测试工具', 5, TRUE),
('非工作时间访问后台', 'resource.startsWith("/admin") && (hour < 8 || hour > 19)', '非工作时间访问管理路径', 3, TRUE),
('高危IP访问', 'isBlacklistedIP(ip)', 'IP在黑名单中', 5, TRUE);