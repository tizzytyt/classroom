package com.edu.classroom.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtAuthInterceptor implements HandlerInterceptor {
  private final JwtUtil jwtUtil;

  public JwtAuthInterceptor(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String uri = request.getRequestURI();
    if (uri.startsWith("/api/auth/login")) {
      return true;
    }
    String auth = request.getHeader("Authorization");
    // video 组件、部分客户端对 /api/files 的 GET 无法带 Authorization，允许用 query token（仅静态文件）
    if (!StringUtils.hasText(auth)
        && "GET".equalsIgnoreCase(request.getMethod())
        && uri.startsWith("/api/files/")) {
      String qToken = request.getParameter("token");
      if (StringUtils.hasText(qToken)) {
        auth = "Bearer " + qToken.trim();
      }
    }
    if (!StringUtils.hasText(auth)) {
      response.setStatus(401);
      return false;
    }
    String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
    try {
      var claims = jwtUtil.parse(token);
      request.setAttribute("uid", claims.get("uid"));
      request.setAttribute("role", claims.get("role"));
      return true;
    } catch (Exception e) {
      response.setStatus(401);
      return false;
    }
  }
}
