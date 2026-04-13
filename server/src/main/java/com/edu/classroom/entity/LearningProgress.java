package com.edu.classroom.entity;

import java.time.LocalDateTime;

public class LearningProgress {
  private Long id;
  private Long resourceId;
  private Long userId;
  private Integer learnStatus;
  private Integer progressPercent;
  private LocalDateTime lastViewedAt;
  private LocalDateTime updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getResourceId() { return resourceId; }
  public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Integer getLearnStatus() { return learnStatus; }
  public void setLearnStatus(Integer learnStatus) { this.learnStatus = learnStatus; }
  public Integer getProgressPercent() { return progressPercent; }
  public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
  public LocalDateTime getLastViewedAt() { return lastViewedAt; }
  public void setLastViewedAt(LocalDateTime lastViewedAt) { this.lastViewedAt = lastViewedAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
