package com.edu.classroom.dto.exam;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class TeacherExamQuestionAnalysisDto {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long questionId;
  private Integer qType;
  private String stem;
  private Double fullScore;
  private Integer participantCount;
  private Double avgScore;
  private Integer correctCount;

  public Long getQuestionId() { return questionId; }
  public void setQuestionId(Long questionId) { this.questionId = questionId; }
  public Integer getQType() { return qType; }
  public void setQType(Integer qType) { this.qType = qType; }
  public String getStem() { return stem; }
  public void setStem(String stem) { this.stem = stem; }
  public Double getFullScore() { return fullScore; }
  public void setFullScore(Double fullScore) { this.fullScore = fullScore; }
  public Integer getParticipantCount() { return participantCount; }
  public void setParticipantCount(Integer participantCount) { this.participantCount = participantCount; }
  public Double getAvgScore() { return avgScore; }
  public void setAvgScore(Double avgScore) { this.avgScore = avgScore; }
  public Integer getCorrectCount() { return correctCount; }
  public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
}

