package com.edu.classroom.dto.teacher;

public class TeacherGradeAssignmentRequest {
  private Double score;
  private String comment;

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}

