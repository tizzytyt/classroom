package com.edu.classroom.dto.exam;

import java.util.List;

public class StudentExamSubmitRequest {
  public static class AnswerItem {
    private Long questionId;
    private String answer;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
  }

  private List<AnswerItem> answers;

  public List<AnswerItem> getAnswers() { return answers; }
  public void setAnswers(List<AnswerItem> answers) { this.answers = answers; }
}

