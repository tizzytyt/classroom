package com.edu.classroom.dto.teacher;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public class StudentLearningMonitorDto {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long studentId;
  private String studentName;

  private Integer totalResources;
  private Integer completedResources;
  private Double resourceCompletionRate;
  private Double avgProgressPercent;
  private LocalDateTime lastActiveAt;

  private Integer totalAssignments;
  private Integer submittedAssignments;
  private Double homeworkCompletionRate;

  private Integer totalExams;
  private Integer completedExams;
  private Double examCompletionRate;

  private Integer totalCheckins;
  private Integer checkedInCount;
  private Double attendanceRate;
  /** 按课程权重计算得到的最终成绩（0-100） */
  private Double finalScore;

  private String riskLevel; // LOW/MEDIUM/HIGH

  public Long getStudentId() { return studentId; }
  public void setStudentId(Long studentId) { this.studentId = studentId; }
  public String getStudentName() { return studentName; }
  public void setStudentName(String studentName) { this.studentName = studentName; }
  public Integer getTotalResources() { return totalResources; }
  public void setTotalResources(Integer totalResources) { this.totalResources = totalResources; }
  public Integer getCompletedResources() { return completedResources; }
  public void setCompletedResources(Integer completedResources) { this.completedResources = completedResources; }
  public Double getResourceCompletionRate() { return resourceCompletionRate; }
  public void setResourceCompletionRate(Double resourceCompletionRate) { this.resourceCompletionRate = resourceCompletionRate; }
  public Double getAvgProgressPercent() { return avgProgressPercent; }
  public void setAvgProgressPercent(Double avgProgressPercent) { this.avgProgressPercent = avgProgressPercent; }
  public LocalDateTime getLastActiveAt() { return lastActiveAt; }
  public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
  public Integer getTotalAssignments() { return totalAssignments; }
  public void setTotalAssignments(Integer totalAssignments) { this.totalAssignments = totalAssignments; }
  public Integer getSubmittedAssignments() { return submittedAssignments; }
  public void setSubmittedAssignments(Integer submittedAssignments) { this.submittedAssignments = submittedAssignments; }
  public Double getHomeworkCompletionRate() { return homeworkCompletionRate; }
  public void setHomeworkCompletionRate(Double homeworkCompletionRate) { this.homeworkCompletionRate = homeworkCompletionRate; }
  public Integer getTotalExams() { return totalExams; }
  public void setTotalExams(Integer totalExams) { this.totalExams = totalExams; }
  public Integer getCompletedExams() { return completedExams; }
  public void setCompletedExams(Integer completedExams) { this.completedExams = completedExams; }
  public Double getExamCompletionRate() { return examCompletionRate; }
  public void setExamCompletionRate(Double examCompletionRate) { this.examCompletionRate = examCompletionRate; }
  public Integer getTotalCheckins() { return totalCheckins; }
  public void setTotalCheckins(Integer totalCheckins) { this.totalCheckins = totalCheckins; }
  public Integer getCheckedInCount() { return checkedInCount; }
  public void setCheckedInCount(Integer checkedInCount) { this.checkedInCount = checkedInCount; }
  public Double getAttendanceRate() { return attendanceRate; }
  public void setAttendanceRate(Double attendanceRate) { this.attendanceRate = attendanceRate; }
  public Double getFinalScore() { return finalScore; }
  public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
  public String getRiskLevel() { return riskLevel; }
  public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}

