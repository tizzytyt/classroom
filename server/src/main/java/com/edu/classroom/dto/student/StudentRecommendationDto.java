package com.edu.classroom.dto.student;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public class StudentRecommendationDto {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long pushId;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long resourceId;
  private String title;
  private String category;
  private String fileUrl;
  private String fileName;
  private String fileType;
  private String reason;
  private Integer status;
  private LocalDateTime createdAt;

  public Long getPushId() { return pushId; }
  public void setPushId(Long pushId) { this.pushId = pushId; }
  public Long getResourceId() { return resourceId; }
  public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getFileUrl() { return fileUrl; }
  public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
  public String getFileName() { return fileName; }
  public void setFileName(String fileName) { this.fileName = fileName; }
  public String getFileType() { return fileType; }
  public void setFileType(String fileType) { this.fileType = fileType; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

