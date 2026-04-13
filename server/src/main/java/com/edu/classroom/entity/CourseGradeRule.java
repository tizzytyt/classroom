package com.edu.classroom.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public class CourseGradeRule {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  private Long courseId;
  private Double assignmentWeight;
  private Double checkinWeight;
  private Double resourceWeight;
  /** 考试成绩在总评中的权重，0～1；可与前三项合计为 1 */
  private Double examWeight;
  private LocalDateTime updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getCourseId() { return courseId; }
  public void setCourseId(Long courseId) { this.courseId = courseId; }
  public Double getAssignmentWeight() { return assignmentWeight; }
  public void setAssignmentWeight(Double assignmentWeight) { this.assignmentWeight = assignmentWeight; }
  public Double getCheckinWeight() { return checkinWeight; }
  public void setCheckinWeight(Double checkinWeight) { this.checkinWeight = checkinWeight; }
  public Double getResourceWeight() { return resourceWeight; }
  public void setResourceWeight(Double resourceWeight) { this.resourceWeight = resourceWeight; }
  public Double getExamWeight() { return examWeight; }
  public void setExamWeight(Double examWeight) { this.examWeight = examWeight; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
