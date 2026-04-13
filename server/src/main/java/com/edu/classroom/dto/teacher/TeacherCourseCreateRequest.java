package com.edu.classroom.dto.teacher;

public class TeacherCourseCreateRequest {
  private String name;
  private String intro;
  private String category;
  private String coverUrl;
  /** 总评占比 0～1，四项之和须为 1；均可选，创建时默认 0.7 / 0.2 / 0.1 / 0 */
  private Double assignmentWeight;
  private Double checkinWeight;
  private Double resourceWeight;
  private Double examWeight;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIntro() {
    return intro;
  }

  public void setIntro(String intro) {
    this.intro = intro;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  public void setCoverUrl(String coverUrl) {
    this.coverUrl = coverUrl;
  }

  public Double getAssignmentWeight() {
    return assignmentWeight;
  }

  public void setAssignmentWeight(Double assignmentWeight) {
    this.assignmentWeight = assignmentWeight;
  }

  public Double getCheckinWeight() {
    return checkinWeight;
  }

  public void setCheckinWeight(Double checkinWeight) {
    this.checkinWeight = checkinWeight;
  }

  public Double getResourceWeight() {
    return resourceWeight;
  }

  public void setResourceWeight(Double resourceWeight) {
    this.resourceWeight = resourceWeight;
  }

  public Double getExamWeight() {
    return examWeight;
  }

  public void setExamWeight(Double examWeight) {
    this.examWeight = examWeight;
  }
}
