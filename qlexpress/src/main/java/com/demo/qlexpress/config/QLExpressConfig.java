package com.demo.qlexpress.config;

import com.ql.util.express.Operator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class QLExpressConfig {

    @Bean
    public Set<String> blacklistedIPs() {
        Set<String> ips = new HashSet<>();
        ips.add("192.168.1.100");
        ips.add("10.0.0.50");
        ips.add("172.16.0.1");
        return ips;
    }

    // 可选：全局 Runner（如需共享）
    // @Bean
    // public ExpressRunner expressRunner(@Autowired Set<String> blacklistedIPs) { ... }
}