package com.edu.classroom.dto.teacher;

public class CourseStatsOverviewResponse {
  private Long courseId;
  private Integer studentCount;
  private Integer signInCount;
  private Double avgAttendanceRate;
  private Integer homeworkCount;
  private Double avgHomeworkCompletionRate;
  private Double avgScore;

  public Long getCourseId() {
    return courseId;
  }

  public void setCourseId(Long courseId) {
    this.courseId = courseId;
  }

  public Integer getStudentCount() {
    return studentCount;
  }

  public void setStudentCount(Integer studentCount) {
    this.studentCount = studentCount;
  }

  public Integer getSignInCount() {
    return signInCount;
  }

  public void setSignInCount(Integer signInCount) {
    this.signInCount = signInCount;
  }

  public Double getAvgAttendanceRate() {
    return avgAttendanceRate;
  }

  public void setAvgAttendanceRate(Double avgAttendanceRate) {
    this.avgAttendanceRate = avgAttendanceRate;
  }

  public Integer getHomeworkCount() {
    return homeworkCount;
  }

  public void setHomeworkCount(Integer homeworkCount) {
    this.homeworkCount = homeworkCount;
  }

  public Double getAvgHomeworkCompletionRate() {
    return avgHomeworkCompletionRate;
  }

  public void setAvgHomeworkCompletionRate(Double avgHomeworkCompletionRate) {
    this.avgHomeworkCompletionRate = avgHomeworkCompletionRate;
  }

  public Double getAvgScore() {
    return avgScore;
  }

  public void setAvgScore(Double avgScore) {
    this.avgScore = avgScore;
  }
}

