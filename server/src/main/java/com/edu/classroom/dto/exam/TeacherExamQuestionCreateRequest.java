package com.edu.classroom.dto.exam;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TeacherExamQuestionCreateRequest {
  public static class Option {
    private String key;   // A/B/C/D...
    private String text;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
  }

  @JsonProperty("qType")
  @JsonAlias({"q_type", "type", "qtype"})
  private Integer qType;

  @JsonProperty("stem")
  @JsonAlias({"question", "title"})
  private String stem;

  @JsonProperty("score")
  @JsonAlias({"points"})
  private Double score;

  @JsonProperty("sortNo")
  @JsonAlias({"sort_no", "sort"})
  private Integer sortNo;

  @JsonProperty("correctAnswer")
  @JsonAlias({"correct_answer", "answer"})
  private String correctAnswer;

  @JsonProperty("options")
  @JsonAlias({"opts", "choices"})
  private List<Option> options;

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

