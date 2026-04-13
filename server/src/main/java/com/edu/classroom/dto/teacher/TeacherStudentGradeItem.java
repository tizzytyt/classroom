package com.edu.classroom.dto.teacher;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/** 教师查看：某课程下一名学生的总评成绩（含未生成总评时各分为 null） */
public class TeacherStudentGradeItem {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long studentId;
  private String studentName;
  private Double assignmentScore;
  private Double checkinScore;
  private Double resourceScore;
  private Double examScore;
  private Double finalScore;
  private LocalDateTime calculatedAt;

  public Long getStudentId() {
    return studentId;
  }

  public void setStudentId(Long studentId) {
    this.studentId = studentId;
  }

  public String getStudentName() {
    return studentName;
  }

  public void setStudentName(String studentName) {
    this.studentName = studentName;
  }

  public Double getAssignmentScore() {
    return assignmentScore;
  }

  public void setAssignmentScore(Double assignmentScore) {
    this.assignmentScore = assignmentScore;
  }

  public Double getCheckinScore() {
    return checkinScore;
  }

  public void setCheckinScore(Double checkinScore) {
    this.checkinScore = checkinScore;
  }

  public Double getResourceScore() {
    return resourceScore;
  }

  public void setResourceScore(Double resourceScore) {
    this.resourceScore = resourceScore;
  }

  public Double getExamScore() {
    return examScore;
  }

  public void setExamScore(Double examScore) {
    this.examScore = examScore;
  }

  public Double getFinalScore() {
    return finalScore;
  }

  public void setFinalScore(Double finalScore) {
    this.finalScore = finalScore;
  }

  public LocalDateTime getCalculatedAt() {
    return calculatedAt;
  }

  public void setCalculatedAt(LocalDateTime calculatedAt) {
    this.calculatedAt = calculatedAt;
  }
}
