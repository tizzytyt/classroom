package com.edu.classroom.entity;

public class ExamOption {
  private Long id;
  private Long questionId;
  private String optKey; // A/B/C/D...
  private String optText;
  private Integer sortNo;
  private Integer status; // 1有效 0删除

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getQuestionId() { return questionId; }
  public void setQuestionId(Long questionId) { this.questionId = questionId; }
  public String getOptKey() { return optKey; }
  public void setOptKey(String optKey) { this.optKey = optKey; }
  public String getOptText() { return optText; }
  public void setOptText(String optText) { this.optText = optText; }
  public Integer getSortNo() { return sortNo; }
  public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
}

