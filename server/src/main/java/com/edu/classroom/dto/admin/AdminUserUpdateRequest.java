package com.edu.classroom.dto.admin;

public class AdminUserUpdateRequest {
  private String realName;
  private String phone;
  private String email;
  private String avatarUrl;

  public String getRealName() { return realName; }
  public void setRealName(String realName) { this.realName = realName; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getAvatarUrl() { return avatarUrl; }
  public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
