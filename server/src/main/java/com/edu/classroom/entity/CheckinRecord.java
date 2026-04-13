package com.edu.classroom.entity;

import java.time.LocalDateTime;

public class CheckinRecord {
  private Long id;
  private Long checkinId;
  private Long studentId;
  private LocalDateTime checkedInAt;
  private String source;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getCheckinId() { return checkinId; }
  public void setCheckinId(Long checkinId) { this.checkinId = checkinId; }
  public Long getStudentId() { return studentId; }
  public void setStudentId(Long studentId) { this.studentId = studentId; }
  public LocalDateTime getCheckedInAt() { return checkedInAt; }
  public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }
  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
}
