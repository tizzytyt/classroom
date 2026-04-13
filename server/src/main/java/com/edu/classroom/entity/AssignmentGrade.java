package com.edu.classroom.entity;

import java.time.LocalDateTime;

public class AssignmentGrade {
  private Long id;
  private Long submissionId;
  private Long teacherId;
  private Double score;
  private String comment;
  private LocalDateTime gradedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getSubmissionId() { return submissionId; }
  public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
  public Long getTeacherId() { return teacherId; }
  public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
  public Double getScore() { return score; }
  public void setScore(Double score) { this.score = score; }
  public String getComment() { return comment; }
  public void setComment(String comment) { this.comment = comment; }
  public LocalDateTime getGradedAt() { return gradedAt; }
  public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
