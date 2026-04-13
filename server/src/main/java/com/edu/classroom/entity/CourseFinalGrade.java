package com.edu.classroom.entity;

import java.time.LocalDateTime;

public class CourseFinalGrade {
  private Long id;
  private Long courseId;
  private Long studentId;
  private Double assignmentScore;
  private Double checkinScore;
  private Double resourceScore;
  private Double examScore;
  private Double finalScore;
  private LocalDateTime calculatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getCourseId() { return courseId; }
  public void setCourseId(Long courseId) { this.courseId = courseId; }
  public Long getStudentId() { return studentId; }
  public void setStudentId(Long studentId) { this.studentId = studentId; }
  public Double getAssignmentScore() { return assignmentScore; }
  public void setAssignmentScore(Double assignmentScore) { this.assignmentScore = assignmentScore; }
  public Double getCheckinScore() { return checkinScore; }
  public void setCheckinScore(Double checkinScore) { this.checkinScore = checkinScore; }
  public Double getResourceScore() { return resourceScore; }
  public void setResourceScore(Double resourceScore) { this.resourceScore = resourceScore; }
  public Double getExamScore() { return examScore; }
  public void setExamScore(Double examScore) { this.examScore = examScore; }
  public Double getFinalScore() { return finalScore; }
  public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
  public LocalDateTime getCalculatedAt() { return calculatedAt; }
  public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
