package com.edu.classroom.dto.exam;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.List;

/** 教师批改：单次答卷详情 */
public class TeacherExamAttemptGradingResponse {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long paperId;
  private String paperTitle;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long attemptId;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long studentId;
  private String studentName;
  private LocalDateTime submittedAt;
  private Double totalScore;
  private Double objectiveScore;
  private List<TeacherExamGradingQuestionDto> questions;

  public Long getPaperId() { return paperId; }
  public void setPaperId(Long paperId) { this.paperId = paperId; }
  public String getPaperTitle() { return paperTitle; }
  public void setPaperTitle(String paperTitle) { this.paperTitle = paperTitle; }
  public Long getAttemptId() { return attemptId; }
  public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
  public Long getStudentId() { return studentId; }
  public void setStudentId(Long studentId) { this.studentId = studentId; }
  public String getStudentName() { return studentName; }
  public void setStudentName(String studentName) { this.studentName = studentName; }
  public LocalDateTime getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
  public Double getTotalScore() { return totalScore; }
  public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
  public Double getObjectiveScore() { return objectiveScore; }
  public void setObjectiveScore(Double objectiveScore) { this.objectiveScore = objectiveScore; }
  public List<TeacherExamGradingQuestionDto> getQuestions() { return questions; }
  public void setQuestions(List<TeacherExamGradingQuestionDto> questions) { this.questions = questions; }
}
