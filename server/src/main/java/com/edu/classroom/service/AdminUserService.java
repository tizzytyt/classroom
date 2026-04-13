package com.edu.classroom.service;

import com.edu.classroom.dto.admin.AdminUserCreateRequest;
import com.edu.classroom.dto.admin.AdminUserDto;
import com.edu.classroom.dto.admin.AdminUserUpdateRequest;
import com.edu.classroom.entity.SysUser;
import com.edu.classroom.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {
  private final SysUserMapper sysUserMapper;

  public AdminUserService(SysUserMapper sysUserMapper) {
    this.sysUserMapper = sysUserMapper;
  }

  private void ensureRoleCode(String roleCode) {
    if (!StringUtils.hasText(roleCode)) throw new RuntimeException("角色不能为空");
    String r = roleCode.trim().toUpperCase();
    if (!("ADMIN".equals(r) || "TEACHER".equals(r) || "STUDENT".equals(r))) throw new RuntimeException("角色不合法");
  }

  private AdminUserDto toDto(SysUser u) {
    AdminUserDto d = new AdminUserDto();
    d.setId(u.getId());
    d.setUsername(u.getUsername());
    d.setRealName(u.getRealName());
    d.setRoleCode(u.getRoleCode());
    d.setPhone(u.getPhone());
    d.setEmail(u.getEmail());
    d.setAvatarUrl(u.getAvatarUrl());
    d.setStatus(u.getStatus());
    d.setCreatedAt(u.getCreatedAt());
    d.setUpdatedAt(u.getUpdatedAt());
    return d;
  }

  public Map<String, Object> listUsers(String keyword, String roleCode, Integer status, int page, int size) {
    if (page < 1) page = 1;
    if (size < 1) size = 10;
    if (size > 100) size = 100;
    int offset = (page - 1) * size;
    long total = sysUserMapper.count(keyword, roleCode, status);
    List<SysUser> rows = sysUserMapper.list(keyword, roleCode, status, offset, size);
    List<AdminUserDto> items = new ArrayList<>();
    for (SysUser u : rows) items.add(toDto(u));
    Map<String, Object> res = new HashMap<>();
    res.put("total", total);
    res.put("page", page);
    res.put("size", size);
    res.put("items", items);
    return res;
  }

  public AdminUserDto createUser(AdminUserCreateRequest req) {
    if (req == null) throw new RuntimeException("参数不能为空");
    if (!StringUtils.hasText(req.getUsername())) throw new RuntimeException("用户名不能为空");
    ensureRoleCode(req.getRoleCode());
    String username = req.getUsername().trim();
    if (sysUserMapper.findByUsername(username) != null) throw new RuntimeException("用户名已存在");

    String rawPwd = StringUtils.hasText(req.getPassword()) ? req.getPassword() : "123456";
    SysUser u = new SysUser();
    u.setId(sysUserMapper.generateId());
    u.setUsername(username);
    // 明文存储：与 AuthService 的明文校验一致（password_hash 字段即明文密码）
    u.setPasswordHash(rawPwd);
    u.setRealName(req.getRealName());
    u.setRoleCode(req.getRoleCode().trim().toUpperCase());
    u.setPhone(req.getPhone());
    u.setEmail(req.getEmail());
    u.setAvatarUrl(req.getAvatarUrl());
    u.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    sysUserMapper.insert(u);
    SysUser created = sysUserMapper.findById(u.getId());
    return toDto(created);
  }

  public AdminUserDto updateUser(Long id, AdminUserUpdateRequest req) {
    if (id == null) throw new RuntimeException("用户ID不能为空");
    SysUser exist = sysUserMapper.findById(id);
    if (exist == null) throw new RuntimeException("用户不存在");
    if (req == null) throw new RuntimeException("参数不能为空");
    SysUser u = new SysUser();
    u.setId(id);
    u.setRealName(req.getRealName());
    u.setPhone(req.getPhone());
    u.setEmail(req.getEmail());
    u.setAvatarUrl(req.getAvatarUrl());
    sysUserMapper.updateProfile(u);
    SysUser updated = sysUserMapper.findById(id);
    return toDto(updated);
  }

  public AdminUserDto updateRole(Long id, String roleCode) {
    if (id == null) throw new RuntimeException("用户ID不能为空");
    if (id == 1L) throw new RuntimeException("不能修改系统管理员角色");
    SysUser exist = sysUserMapper.findById(id);
    if (exist == null) throw new RuntimeException("用户不存在");
    ensureRoleCode(roleCode);
    String r = roleCode.trim().toUpperCase();
    sysUserMapper.updateRole(id, r);
    SysUser updated = sysUserMapper.findById(id);
    return toDto(updated);
  }

  public AdminUserDto updateStatus(Long id, Integer status) {
    if (id == null) throw new RuntimeException("用户ID不能为空");
    if (id == 1L && status != null && status != 1) throw new RuntimeException("不能禁用系统管理员");
    SysUser exist = sysUserMapper.findById(id);
    if (exist == null) throw new RuntimeException("用户不存在");
    if (status == null || !(status == 0 || status == 1)) throw new RuntimeException("状态不合法");
    sysUserMapper.updateStatus(id, status);
    SysUser updated = sysUserMapper.findById(id);
    return toDto(updated);
  }

  public void resetPassword(Long id, String password) {
    if (id == null) throw new RuntimeException("用户ID不能为空");
    SysUser exist = sysUserMapper.findById(id);
    if (exist == null) throw new RuntimeException("用户不存在");
    String pwd = StringUtils.hasText(password) ? password : "123456";
    sysUserMapper.updatePasswordHash(id, pwd);
  }

  public void deleteUser(Long id) {
    if (id == null) throw new RuntimeException("用户ID不能为空");
    if (id == 1L) throw new RuntimeException("不能删除系统管理员");
    SysUser exist = sysUserMapper.findById(id);
    if (exist == null) throw new RuntimeException("用户不存在");
    int rows = sysUserMapper.hardDelete(id);
    if (rows <= 0) throw new RuntimeException("删除失败，请重试");
  }
}

