package com.filmbooking.booking_service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ConditionalOnProperty("my.security.enabled")
@Import(SecurityAutoConfiguration.class)
public class BSSecurityConfig extends WebSecurityConfigurerAdapter {

}
