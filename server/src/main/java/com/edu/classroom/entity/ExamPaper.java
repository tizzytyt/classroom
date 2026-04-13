package com.edu.classroom.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public class ExamPaper {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long courseId;
  private String title;
  private Integer durationMinutes;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  /** 是否打乱题目顺序：0否 1是 */
  private Integer shuffleQuestions;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long creatorId;
  private Integer status; // 0=草稿 1=已发布 2=已下线
  private LocalDateTime createdAt;
  /** 学生列表查询时填充：已提交答卷中的最高得分，无则为 null */
  private Double myBestScore;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getCourseId() { return courseId; }
  public void setCourseId(Long courseId) { this.courseId = courseId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public Integer getDurationMinutes() { return durationMinutes; }
  public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
  public LocalDateTime getStartAt() { return startAt; }
  public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
  public LocalDateTime getEndAt() { return endAt; }
  public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
  public Integer getShuffleQuestions() { return shuffleQuestions; }
  public void setShuffleQuestions(Integer shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }
  public Long getCreatorId() { return creatorId; }
  public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public Double getMyBestScore() { return myBestScore; }
  public void setMyBestScore(Double myBestScore) { this.myBestScore = myBestScore; }
}

