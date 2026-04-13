package com.edu.classroom.service;

import com.edu.classroom.dto.LoginResponse;
import com.edu.classroom.entity.SysUser;
import com.edu.classroom.mapper.SysUserMapper;
import com.edu.classroom.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
  private final SysUserMapper sysUserMapper;
  private final JwtUtil jwtUtil;

  public AuthService(SysUserMapper sysUserMapper, JwtUtil jwtUtil) {
    this.sysUserMapper = sysUserMapper;
    this.jwtUtil = jwtUtil;
  }

  public LoginResponse login(String username, String password) {
    if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
      throw new RuntimeException("用户名或密码不能为空");
    }
    SysUser user = sysUserMapper.findByUsername(username);
    if (user == null || user.getStatus() == null || user.getStatus() != 1) {
      throw new RuntimeException("用户不存在或已禁用");
    }
    String storedPassword = user.getPasswordHash();
    if (storedPassword == null || !storedPassword.equals(password)) {
      throw new RuntimeException("用户名或密码错误");
    }
    String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
    LoginResponse resp = new LoginResponse();
    resp.setToken(token);
    resp.setUserId(user.getId());
    resp.setUsername(user.getUsername());
    resp.setRealName(user.getRealName());
    resp.setRoleCode(user.getRoleCode());
    return resp;
  }
}
