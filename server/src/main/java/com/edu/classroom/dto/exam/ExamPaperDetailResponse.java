package com.edu.classroom.dto.exam;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.List;

public class ExamPaperDetailResponse {
  public static class Option {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;
    private String key;
    private String text;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
  }

  public static class Question {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Integer qType;
    private String stem;
    private Double score;
    private Integer sortNo;
    private String correctAnswer; // 教师端可返回；学生端不填充
    private List<Option> options;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getQType() { return qType; }
    public void setQType(Integer qType) { this.qType = qType; }
    public String getStem() { return stem; }
    public void setStem(String stem) { this.stem = stem; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }
  }

  @JsonSerialize(using = ToStringSerializer.class)
  private Long paperId;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long courseId;
  private String title;
  private Integer durationMinutes;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private Integer status;
  /** 是否打乱题目顺序：0否 1是 */
  private Integer shuffleQuestions;
  /** 学生端：是否已提交过该试卷 */
  private Boolean submitted;
  /** 学生端：已提交答卷中的最高分 */
  private Double myBestScore;
  private List<Question> questions;

  public Long getPaperId() { return paperId; }
  public void setPaperId(Long paperId) { this.paperId = paperId; }
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
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
  public Integer getShuffleQuestions() { return shuffleQuestions; }
  public void setShuffleQuestions(Integer shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }
  public Boolean getSubmitted() { return submitted; }
  public void setSubmitted(Boolean submitted) { this.submitted = submitted; }
  public Double getMyBestScore() { return myBestScore; }
  public void setMyBestScore(Double myBestScore) { this.myBestScore = myBestScore; }
  public List<Question> getQuestions() { return questions; }
  public void setQuestions(List<Question> questions) { this.questions = questions; }
}

