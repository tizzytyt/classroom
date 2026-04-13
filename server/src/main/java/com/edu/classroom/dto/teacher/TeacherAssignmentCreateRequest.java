package com.edu.classroom.dto.teacher;

public class TeacherAssignmentCreateRequest {
  private String title;
  private String content;
  private String dueAt;
  private Double totalScore;
  private String attachmentUrl;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getDueAt() {
    return dueAt;
  }

  public void setDueAt(String dueAt) {
    this.dueAt = dueAt;
  }

  public Double getTotalScore() {
    return totalScore;
  }

  public void setTotalScore(Double totalScore) {
    this.totalScore = totalScore;
  }

  public String getAttachmentUrl() {
    return attachmentUrl;
  }

  public void setAttachmentUrl(String attachmentUrl) {
    this.attachmentUrl = attachmentUrl;
  }
}

