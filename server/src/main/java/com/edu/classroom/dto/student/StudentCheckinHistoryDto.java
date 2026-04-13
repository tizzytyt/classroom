package com.edu.classroom.dto.student;

import java.time.LocalDateTime;

public class StudentCheckinHistoryDto {
  private Long id;
  private Long courseId;
  private String title;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  /** 1进行中/0已结束（与 checkin.status 一致） */
  private Integer status;
  /** 学生是否已签到 */
  private Boolean checkedIn;
  private LocalDateTime checkedInAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getCourseId() {
    return courseId;
  }

  public void setCourseId(Long courseId) {
    this.courseId = courseId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDateTime getStartAt() {
    return startAt;
  }

  public void setStartAt(LocalDateTime startAt) {
    this.startAt = startAt;
  }

  public LocalDateTime getEndAt() {
    return endAt;
  }

  public void setEndAt(LocalDateTime endAt) {
    this.endAt = endAt;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Boolean getCheckedIn() {
    return checkedIn;
  }

  public void setCheckedIn(Boolean checkedIn) {
    this.checkedIn = checkedIn;
  }

  public LocalDateTime getCheckedInAt() {
    return checkedInAt;
  }

  public void setCheckedInAt(LocalDateTime checkedInAt) {
    this.checkedInAt = checkedInAt;
  }
}

