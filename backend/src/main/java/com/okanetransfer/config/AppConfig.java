package com.okanetransfer.config;

import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(basePackages = "com.okanetransfer")
@PropertySource("classpath:application.properties")
public class AppConfig {
    // Spring beans that don't fit elsewhere
}