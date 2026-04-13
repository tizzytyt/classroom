package com.edu.classroom.dto.exam;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/** 教师批改：单题及学生作答、当前得分 */
public class TeacherExamGradingQuestionDto {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long questionId;
  private Integer qType;
  private String stem;
  private Double fullScore;
  private String correctAnswer;
  private String studentAnswer;
  /** 当前得分（可为空表示未记分） */
  private Double earnedScore;
  /** 是否允许教师改分（简答题或教师纠错时均可调，由前端高亮） */
  private boolean gradable;

  public Long getQuestionId() { return questionId; }
  public void setQuestionId(Long questionId) { this.questionId = questionId; }
  public Integer getQType() { return qType; }
  public void setQType(Integer qType) { this.qType = qType; }
  public String getStem() { return stem; }
  public void setStem(String stem) { this.stem = stem; }
  public Double getFullScore() { return fullScore; }
  public void setFullScore(Double fullScore) { this.fullScore = fullScore; }
  public String getCorrectAnswer() { return correctAnswer; }
  public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
  public String getStudentAnswer() { return studentAnswer; }
  public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
  public Double getEarnedScore() { return earnedScore; }
  public void setEarnedScore(Double earnedScore) { this.earnedScore = earnedScore; }
  public boolean isGradable() { return gradable; }
  public void setGradable(boolean gradable) { this.gradable = gradable; }
}
