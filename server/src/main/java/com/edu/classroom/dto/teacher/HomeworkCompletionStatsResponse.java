package com.edu.classroom.dto.teacher;

import java.time.LocalDateTime;

public class HomeworkCompletionStatsResponse {
  private Long homeworkId;
  private String title;
  private LocalDateTime publishTime;
  private LocalDateTime deadline;
  private Integer submitCount;
  private Integer studentCount;
  private Double completionRate;

  public Long getHomeworkId() {
    return homeworkId;
  }

  public void setHomeworkId(Long homeworkId) {
    this.homeworkId = homeworkId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDateTime getPublishTime() {
    return publishTime;
  }

  public void setPublishTime(LocalDateTime publishTime) {
    this.publishTime = publishTime;
  }

  public LocalDateTime getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDateTime deadline) {
    this.deadline = deadline;
  }

  public Integer getSubmitCount() {
    return submitCount;
  }

  public void setSubmitCount(Integer submitCount) {
    this.submitCount = submitCount;
  }

  public Integer getStudentCount() {
    return studentCount;
  }

  public void setStudentCount(Integer studentCount) {
    this.studentCount = studentCount;
  }

  public Double getCompletionRate() {
    return completionRate;
  }

  public void setCompletionRate(Double completionRate) {
    this.completionRate = completionRate;
  }
}

