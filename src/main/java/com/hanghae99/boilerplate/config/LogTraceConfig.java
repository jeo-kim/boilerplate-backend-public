package com.hanghae99.boilerplate.config;

import com.hanghae99.boilerplate.chat.trace.logtrace.LogTrace;
import com.hanghae99.boilerplate.chat.trace.logtrace.ThreadLocalLogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogTraceConfig {

    @Bean
    public LogTrace logTrace() {
        return new ThreadLocalLogTrace();
    }
}
