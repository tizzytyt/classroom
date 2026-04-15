package com.edu.classroom.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtAuthInterceptor implements HandlerInterceptor {
  private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);
  private final JwtUtil jwtUtil;

  public JwtAuthInterceptor(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String uri = request.getRequestURI();
    String method = request.getMethod();
    if (uri.startsWith("/api/auth/login")) {
      return true;
    }
    // 文件流接口放行：避免小程序 video 组件在不同环境下鉴权头/参数不稳定导致播放失败
    if (uri.startsWith("/api/files")) {
      log.info("[auth] bypass file auth method={}, uri={}", method, uri);
      return true;
    }
    String auth = request.getHeader("Authorization");
    boolean fromQueryToken = false;
    // video 组件、部分客户端对 /api/files 的 GET 无法带 Authorization，允许用 query token（仅静态文件）
    if (!StringUtils.hasText(auth)
        && "GET".equalsIgnoreCase(method)
        && uri.startsWith("/api/files")) {
      String qToken = request.getParameter("token");
      if (StringUtils.hasText(qToken)) {
        auth = "Bearer " + qToken.trim();
        fromQueryToken = true;
      }
    }
    if (!StringUtils.hasText(auth)) {
      log.warn("[auth] reject 401 missing token method={}, uri={}, query={}", method, uri, request.getQueryString());
      response.setStatus(401);
      return false;
    }
    String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
    try {
      var claims = jwtUtil.parse(token);
      request.setAttribute("uid", claims.get("uid"));
      request.setAttribute("role", claims.get("role"));
      Object uid = claims.get("uid");
      Object role = claims.get("role");
      log.info("[auth] pass method={}, uri={}, uid={}, role={}, fromQueryToken={}", method, uri, uid, role, fromQueryToken);
      return true;
    } catch (Exception e) {
      log.warn("[auth] reject 401 invalid token method={}, uri={}, fromQueryToken={}, message={}",
          method, uri, fromQueryToken, e.getMessage());
      response.setStatus(401);
      return false;
    }
  }
}
