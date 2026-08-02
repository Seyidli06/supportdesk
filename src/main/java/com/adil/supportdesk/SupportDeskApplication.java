package com.adil.supportdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.adil.supportdesk")
public class SupportDeskApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupportDeskApplication.class, args);
    }
}