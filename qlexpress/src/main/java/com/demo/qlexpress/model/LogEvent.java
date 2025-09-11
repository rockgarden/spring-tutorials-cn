package com.demo.qlexpress.model;

import lombok.Data;

@Data
public class LogEvent {
    private String userId;
    private String action;      // LOGIN, DELETE_USER, etc.
    private String ip;
    private String status;      // SUCCESS, FAILED
    private String userAgent;
    private String resource;    // URL path
    private long timestamp;
    private int hour;           // 0-23
    private int failCount;      // 外部统计：该IP近期失败次数
}