package com.edu.classroom.dto.admin;

public class AdminUserCreateRequest {
  private String username;
  private String password;
  private String realName;
  private String roleCode;
  private String phone;
  private String email;
  private String avatarUrl;
  private Integer status;

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getRealName() { return realName; }
  public void setRealName(String realName) { this.realName = realName; }
  public String getRoleCode() { return roleCode; }
  public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getAvatarUrl() { return avatarUrl; }
  public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
}
