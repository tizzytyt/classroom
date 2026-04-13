package com.edu.classroom.entity;

import java.time.LocalDateTime;

public class AssignmentSubmission {
  private Long id;
  private Long assignmentId;
  private Long studentId;
  private String submitText;
  /** JSON 数组字符串，如 ["/api/files/xxx.pdf"] */
  private String attachmentUrls;
  private LocalDateTime submittedAt;
  private String submitStatus;
  private Integer isLate;
  private Integer version;
  private LocalDateTime updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getAssignmentId() { return assignmentId; }
  public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
  public Long getStudentId() { return studentId; }
  public void setStudentId(Long studentId) { this.studentId = studentId; }
  public String getSubmitText() { return submitText; }
  public void setSubmitText(String submitText) { this.submitText = submitText; }
  public String getAttachmentUrls() { return attachmentUrls; }
  public void setAttachmentUrls(String attachmentUrls) { this.attachmentUrls = attachmentUrls; }
  public LocalDateTime getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
  public String getSubmitStatus() { return submitStatus; }
  public void setSubmitStatus(String submitStatus) { this.submitStatus = submitStatus; }
  public Integer getIsLate() { return isLate; }
  public void setIsLate(Integer isLate) { this.isLate = isLate; }
  public Integer getVersion() { return version; }
  public void setVersion(Integer version) { this.version = version; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
