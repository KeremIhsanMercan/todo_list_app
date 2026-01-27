package com.kerem.todoApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Value("${app.maxDependencyDepth}")
    private Long maxDependencyDepth;
    
    @Value("${app.maxTodoItemPerPage}")
    private Integer defaultPageSize;
    
    public Long getMaxDependencyDepth() {
        return maxDependencyDepth;
    }
    
    public Integer getDefaultPageSize() {
        return defaultPageSize;
    }
    
    public String getDefaultPageSizeAsString() {
        return String.valueOf(defaultPageSize);
    }
}
