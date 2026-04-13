package com.edu.classroom.entity;

import java.time.LocalDateTime;

public class Course {
  private Long id;
  private String name;
  private String courseCode;
  private String intro;
  private String coverUrl;
  private String category;
  private Long teacherId;
  private Integer status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
  /** 以下四项来自 course_grade_rule，仅接口 JSON 使用，非 course 表字段 */
  private Double gradeAssignmentWeight;
  private Double gradeCheckinWeight;
  private Double gradeResourceWeight;
  private Double gradeExamWeight;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getCourseCode() { return courseCode; }
  public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
  public String getIntro() { return intro; }
  public void setIntro(String intro) { this.intro = intro; }
  public String getCoverUrl() { return coverUrl; }
  public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public Long getTeacherId() { return teacherId; }
  public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
  public LocalDateTime getDeletedAt() { return deletedAt; }
  public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
  public Double getGradeAssignmentWeight() { return gradeAssignmentWeight; }
  public void setGradeAssignmentWeight(Double gradeAssignmentWeight) { this.gradeAssignmentWeight = gradeAssignmentWeight; }
  public Double getGradeCheckinWeight() { return gradeCheckinWeight; }
  public void setGradeCheckinWeight(Double gradeCheckinWeight) { this.gradeCheckinWeight = gradeCheckinWeight; }
  public Double getGradeResourceWeight() { return gradeResourceWeight; }
  public void setGradeResourceWeight(Double gradeResourceWeight) { this.gradeResourceWeight = gradeResourceWeight; }
  public Double getGradeExamWeight() { return gradeExamWeight; }
  public void setGradeExamWeight(Double gradeExamWeight) { this.gradeExamWeight = gradeExamWeight; }
}
