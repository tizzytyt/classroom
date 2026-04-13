package com.edu.classroom.entity;

public class ExamAnswer {
  private Long id;
  private Long attemptId;
  private Long questionId;
  private String answer; // 学生答案（单选:A，多选:A,B；判断:T/F；文本:内容）
  private Double score;  // 该题得分（只自动判客观题）

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getAttemptId() { return attemptId; }
  public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
  public Long getQuestionId() { return questionId; }
  public void setQuestionId(Long questionId) { this.questionId = questionId; }
  public String getAnswer() { return answer; }
  public void setAnswer(String answer) { this.answer = answer; }
  public Double getScore() { return score; }
  public void setScore(Double score) { this.score = score; }
}

