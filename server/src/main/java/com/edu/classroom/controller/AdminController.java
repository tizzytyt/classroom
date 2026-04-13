package com.edu.classroom.controller;

import com.edu.classroom.dto.admin.*;
import com.edu.classroom.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminUserService adminUserService;
  private final JdbcTemplate jdbcTemplate;

  public AdminController(AdminUserService adminUserService, JdbcTemplate jdbcTemplate) {
    this.adminUserService = adminUserService;
    this.jdbcTemplate = jdbcTemplate;
  }

  private boolean isAdmin(Object role) {
    return "ADMIN".equals(String.valueOf(role));
  }

  private ResponseEntity<Map<String, Object>> forbidden() {
    Map<String, Object> body = new HashMap<>();
    body.put("message", "无权限");
    return ResponseEntity.status(403).body(body);
  }

  @GetMapping("/users")
  public ResponseEntity<?> listUsers(@RequestAttribute("role") Object role,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String roleCode,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size) {
    if (!isAdmin(role)) return forbidden();
    return ResponseEntity.ok(adminUserService.listUsers(keyword, roleCode, status, page, size));
  }

  @PostMapping("/users")
  public ResponseEntity<?> createUser(@RequestAttribute("role") Object role, @RequestBody AdminUserCreateRequest req) {
    if (!isAdmin(role)) return forbidden();
    return ResponseEntity.ok(adminUserService.createUser(req));
  }

  @PutMapping("/users/{id}")
  public ResponseEntity<?> updateUser(@RequestAttribute("role") Object role, @PathVariable Long id, @RequestBody AdminUserUpdateRequest req) {
    if (!isAdmin(role)) return forbidden();
    return ResponseEntity.ok(adminUserService.updateUser(id, req));
  }

  @PatchMapping("/users/{id}/role")
  public ResponseEntity<?> updateRole(@RequestAttribute("role") Object role, @PathVariable Long id, @RequestBody AdminUserRoleRequest req) {
    if (!isAdmin(role)) return forbidden();
    String rc = req == null ? null : req.getRoleCode();
    return ResponseEntity.ok(adminUserService.updateRole(id, rc));
  }

  @PostMapping("/users/{id}/role")
  public ResponseEntity<?> updateRolePost(@RequestAttribute("role") Object role, @PathVariable Long id, @RequestBody AdminUserRoleRequest req) {
    return updateRole(role, id, req);
  }

  @PatchMapping("/users/{id}/status")
  public ResponseEntity<?> updateStatus(@RequestAttribute("role") Object role, @PathVariable Long id, @RequestBody AdminUserStatusRequest req) {
    if (!isAdmin(role)) return forbidden();
    Integer s = req == null ? null : req.getStatus();
    return ResponseEntity.ok(adminUserService.updateStatus(id, s));
  }

  @PostMapping("/users/{id}/status")
  public ResponseEntity<?> updateStatusPost(@RequestAttribute("role") Object role, @PathVariable Long id, @RequestBody AdminUserStatusRequest req) {
    return updateStatus(role, id, req);
  }

  @PostMapping("/users/{id}/reset-password")
  public ResponseEntity<?> resetPassword(@RequestAttribute("role") Object role, @PathVariable Long id, @RequestBody(required = false) AdminResetPasswordRequest req) {
    if (!isAdmin(role)) return forbidden();
    String pwd = req == null ? null : req.getPassword();
    adminUserService.resetPassword(id, pwd);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(@RequestAttribute("role") Object role, @PathVariable Long id) {
    if (!isAdmin(role)) return forbidden();
    adminUserService.deleteUser(id);
    Map<String, Object> body = new HashMap<>();
    body.put("message", "ok");
    return ResponseEntity.ok(body);
  }

  @GetMapping("/stats")
  public ResponseEntity<?> stats(@RequestAttribute("role") Object role) {
    if (!isAdmin(role)) return forbidden();
    Map<String, Object> res = new HashMap<>();
    res.put("usersTotal", jdbcTemplate.queryForObject("select count(1) from sys_user", Long.class));
    res.put("usersActive", jdbcTemplate.queryForObject("select count(1) from sys_user where status = 1", Long.class));
    res.put("admins", jdbcTemplate.queryForObject("select count(1) from sys_user where role_code = 'ADMIN'", Long.class));
    res.put("teachers", jdbcTemplate.queryForObject("select count(1) from sys_user where role_code = 'TEACHER'", Long.class));
    res.put("students", jdbcTemplate.queryForObject("select count(1) from sys_user where role_code = 'STUDENT'", Long.class));
    res.put("courses", jdbcTemplate.queryForObject("select count(1) from course", Long.class));
    res.put("resources", jdbcTemplate.queryForObject("select count(1) from course_resource", Long.class));
    res.put("assignments", jdbcTemplate.queryForObject("select count(1) from assignment", Long.class));
    res.put("checkins", jdbcTemplate.queryForObject("select count(1) from checkin", Long.class));
    return ResponseEntity.ok(res);
  }
}
