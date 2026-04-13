package com.edu.classroom.dto.exam;

public class TeacherExamPaperCreateRequest {
  private String title;
  private Integer durationMinutes;
  private String startAt; // yyyy-MM-dd HH:mm:ss 可为空
  private String endAt;   // yyyy-MM-dd HH:mm:ss 可为空
  /** 是否打乱题目顺序：true=打乱 false=不打乱 */
  private Boolean shuffleQuestions;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public Integer getDurationMinutes() { return durationMinutes; }
  public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
  public String getStartAt() { return startAt; }
  public void setStartAt(String startAt) { this.startAt = startAt; }
  public String getEndAt() { return endAt; }
  public void setEndAt(String endAt) { this.endAt = endAt; }
  public Boolean getShuffleQuestions() { return shuffleQuestions; }
  public void setShuffleQuestions(Boolean shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }
}

