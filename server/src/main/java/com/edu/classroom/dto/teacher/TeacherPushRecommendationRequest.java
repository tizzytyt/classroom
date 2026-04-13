package com.edu.classroom.dto.teacher;

import java.util.List;

public class TeacherPushRecommendationRequest {
  private List<Long> resourceIds;
  private String reason;

  public List<Long> getResourceIds() { return resourceIds; }
  public void setResourceIds(List<Long> resourceIds) { this.resourceIds = resourceIds; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
}

