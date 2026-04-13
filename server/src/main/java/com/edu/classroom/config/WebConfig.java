package com.edu.classroom.config;

import com.edu.classroom.security.JwtAuthInterceptor;
import com.edu.classroom.security.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final JwtUtil jwtUtil;

  public WebConfig(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new JwtAuthInterceptor(jwtUtil))
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/auth/login");
  }
}
