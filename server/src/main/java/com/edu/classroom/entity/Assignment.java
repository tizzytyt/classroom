package com.edu.classroom.entity;

import java.time.LocalDateTime;

public class Assignment {
  private Long id;
  private Long courseId;
  private String title;
  private String content;
  private LocalDateTime dueAt;
  private Double totalScore;
  private Long creatorId;
  private String attachmentUrl;
  private LocalDateTime publishedAt;
  private Integer status;
  /** 学生课程作业列表接口填充：教师已评分数，未评分为 null */
  private Double myScore;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getCourseId() { return courseId; }
  public void setCourseId(Long courseId) { this.courseId = courseId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public LocalDateTime getDueAt() { return dueAt; }
  public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
  public Double getTotalScore() { return totalScore; }
  public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
  public Long getCreatorId() { return creatorId; }
  public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
  public String getAttachmentUrl() { return attachmentUrl; }
  public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
  public LocalDateTime getPublishedAt() { return publishedAt; }
  public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
  public Double getMyScore() { return myScore; }
  public void setMyScore(Double myScore) { this.myScore = myScore; }
}
