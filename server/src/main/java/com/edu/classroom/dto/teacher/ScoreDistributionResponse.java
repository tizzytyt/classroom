package com.edu.classroom.dto.teacher;

import java.util.List;

public class ScoreDistributionResponse {
  public static class Bucket {
    private String range;
    private Integer count;

    public String getRange() {
      return range;
    }

    public void setRange(String range) {
      this.range = range;
    }

    public Integer getCount() {
      return count;
    }

    public void setCount(Integer count) {
      this.count = count;
    }
  }

  private Long courseId;
  private Long homeworkId;
  private List<Bucket> buckets;

  public Long getCourseId() {
    return courseId;
  }

  public void setCourseId(Long courseId) {
    this.courseId = courseId;
  }

  public Long getHomeworkId() {
    return homeworkId;
  }

  public void setHomeworkId(Long homeworkId) {
    this.homeworkId = homeworkId;
  }

  public List<Bucket> getBuckets() {
    return buckets;
  }

  public void setBuckets(List<Bucket> buckets) {
    this.buckets = buckets;
  }
}

