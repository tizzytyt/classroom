package com.edu.classroom.dto.exam;

import java.util.List;

public class TeacherExamGradeSaveRequest {
  private List<Item> items;

  public List<Item> getItems() { return items; }
  public void setItems(List<Item> items) { this.items = items; }

  public static class Item {
    private Long questionId;
    private Double score;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
  }
}
