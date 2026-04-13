package com.edu.classroom.entity;

import java.time.LocalDateTime;

public class ExamAttempt {
  private Long id;
  private Long paperId;
  private Long studentId;
  private LocalDateTime startedAt;
  private LocalDateTime submittedAt;
  private Double objectiveScore;
  private Double totalScore;
  private Integer status; // 1进行中 2已提交

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getPaperId() { return paperId; }
  public void setPaperId(Long paperId) { this.paperId = paperId; }
  public Long getStudentId() { return studentId; }
  public void setStudentId(Long studentId) { this.studentId = studentId; }
  public LocalDateTime getStartedAt() { return startedAt; }
  public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
  public LocalDateTime getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
  public Double getObjectiveScore() { return objectiveScore; }
  public void setObjectiveScore(Double objectiveScore) { this.objectiveScore = objectiveScore; }
  public Double getTotalScore() { return totalScore; }
  public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
}

