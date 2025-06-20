package com.dionialves.AsteraComm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dionialves.AsteraComm.interceptor.RequestTimingInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    RequestTimingInterceptor timingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(timingInterceptor);
    }
}
