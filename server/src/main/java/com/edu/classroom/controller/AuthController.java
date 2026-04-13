package com.edu.classroom.controller;

import com.edu.classroom.dto.LoginRequest;
import com.edu.classroom.dto.LoginResponse;
import com.edu.classroom.entity.SysUser;
import com.edu.classroom.mapper.SysUserMapper;
import com.edu.classroom.security.JwtUtil;
import com.edu.classroom.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final SysUserMapper sysUserMapper;
  private final JwtUtil jwtUtil;

  public AuthController(AuthService authService, SysUserMapper sysUserMapper, JwtUtil jwtUtil) {
    this.authService = authService;
    this.sysUserMapper = sysUserMapper;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
    LoginResponse resp = authService.login(req.getUsername(), req.getPassword());
    return ResponseEntity.ok(resp);
  }

  @GetMapping("/profile")
  public ResponseEntity<SysUser> profile(@RequestHeader("Authorization") String authorization) {
    String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    var claims = jwtUtil.parse(token);
    String uname = (String) claims.get("uname");
    SysUser user = sysUserMapper.findByUsername(uname);
    if (user != null) {
      user.setPasswordHash(null);
    }
    return ResponseEntity.ok(user);
  }
}
