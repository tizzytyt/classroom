package com.edu.classroom.dto.exam;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/** 教师端：某试卷下已提交答卷列表 */
public class TeacherExamAttemptListItem {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long attemptId;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long studentId;
  private String studentUsername;
  private String studentName;
  private LocalDateTime submittedAt;
  private Double totalScore;
  private Double objectiveScore;
  private Boolean graded;

  public Long getAttemptId() { return attemptId; }
  public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
  public Long getStudentId() { return studentId; }
  public void setStudentId(Long studentId) { this.studentId = studentId; }
  public String getStudentUsername() { return studentUsername; }
  public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }
  public String getStudentName() { return studentName; }
  public void setStudentName(String studentName) { this.studentName = studentName; }
  public LocalDateTime getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
  public Double getTotalScore() { return totalScore; }
  public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
  public Double getObjectiveScore() { return objectiveScore; }
  public void setObjectiveScore(Double objectiveScore) { this.objectiveScore = objectiveScore; }
  public Boolean getGraded() { return graded; }
  public void setGraded(Boolean graded) { this.graded = graded; }
}
