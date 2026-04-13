package com.edu.classroom.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public class CourseResource {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long courseId;
  private String title;
  private String description;
  private String category;
  private String fileUrl;
  private String fileName;
  private Long fileSize;
  private String fileType;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long uploaderId;
  private LocalDateTime publishedAt;
  private Integer status;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getCourseId() { return courseId; }
  public void setCourseId(Long courseId) { this.courseId = courseId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getFileUrl() { return fileUrl; }
  public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
  public String getFileName() { return fileName; }
  public void setFileName(String fileName) { this.fileName = fileName; }
  public Long getFileSize() { return fileSize; }
  public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
  public String getFileType() { return fileType; }
  public void setFileType(String fileType) { this.fileType = fileType; }
  public Long getUploaderId() { return uploaderId; }
  public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
  public LocalDateTime getPublishedAt() { return publishedAt; }
  public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
}
