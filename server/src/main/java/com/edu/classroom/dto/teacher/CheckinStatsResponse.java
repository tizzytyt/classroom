package com.edu.classroom.dto.teacher;

import java.time.LocalDateTime;
import java.util.List;

public class CheckinStatsResponse {
  public static class CheckedInStudent {
    private Long studentId;
    private String studentName;
    private LocalDateTime checkedInAt;

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

    public LocalDateTime getCheckedInAt() {
      return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
      this.checkedInAt = checkedInAt;
    }
  }

  public static class NotCheckedInStudent {
    private Long studentId;
    private String studentName;

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
  }

  private List<CheckedInStudent> checkedIn;
  private List<NotCheckedInStudent> notCheckedIn;

  public List<CheckedInStudent> getCheckedIn() {
    return checkedIn;
  }

  public void setCheckedIn(List<CheckedInStudent> checkedIn) {
    this.checkedIn = checkedIn;
  }

  public List<NotCheckedInStudent> getNotCheckedIn() {
    return notCheckedIn;
  }

  public void setNotCheckedIn(List<NotCheckedInStudent> notCheckedIn) {
    this.notCheckedIn = notCheckedIn;
  }
}

