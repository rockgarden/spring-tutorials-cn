package com.demo.qlexpress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QlexpressDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(QlexpressDemoApplication.class, args);
    }
}