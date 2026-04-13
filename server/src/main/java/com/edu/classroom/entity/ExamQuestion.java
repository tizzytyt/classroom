package com.edu.classroom.entity;

public class ExamQuestion {
  private Long id;
  private Long paperId;
  private Integer qType; // 1单选 2多选 3判断 4填空(简化为文本匹配) 5简答(不自动判分)
  private String stem;
  private Double score;
  private Integer sortNo;
  private String correctAnswer; // 客观题标准答案（单选:A，多选:A,B；判断:T/F；填空:文本）
  private Integer status; // 1有效 0删除

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getPaperId() { return paperId; }
  public void setPaperId(Long paperId) { this.paperId = paperId; }
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
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
}

