package com.edu.classroom.dto.teacher;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public class AssignmentSubmissionGradeDto {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long submissionId;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long assignmentId;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long studentId;
  private String studentName;

  private String submitText;
  /** 学生提交的附件 URL 列表（JSON 字符串） */
  private String attachmentUrls;
  private LocalDateTime submittedAt;
  private String submitStatus;
  private Integer isLate;

  private Double gradeScore;
  private String gradeComment;
  private LocalDateTime gradedAt;

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
  }

  public Long getStudentId() {
    return studentId;
  }

  public void setStudentId(Long studentId) {
    this.studentId = studentId;
  }

  public String getStudentName() {
    return studentName;
  }

  public void setStudentName(String studentName) {
    this.studentName = studentName;
  }

  public String getSubmitText() {
    return submitText;
  }

  public void setSubmitText(String submitText) {
    this.submitText = submitText;
  }

  public String getAttachmentUrls() {
    return attachmentUrls;
  }

  public void setAttachmentUrls(String attachmentUrls) {
    this.attachmentUrls = attachmentUrls;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(LocalDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  public String getSubmitStatus() {
    return submitStatus;
  }

  public void setSubmitStatus(String submitStatus) {
    this.submitStatus = submitStatus;
  }

  public Integer getIsLate() {
    return isLate;
  }

  public void setIsLate(Integer isLate) {
    this.isLate = isLate;
  }

  public Double getGradeScore() {
    return gradeScore;
  }

  public void setGradeScore(Double gradeScore) {
    this.gradeScore = gradeScore;
  }

  public String getGradeComment() {
    return gradeComment;
  }

  public void setGradeComment(String gradeComment) {
    this.gradeComment = gradeComment;
  }

  public LocalDateTime getGradedAt() {
    return gradedAt;
  }

  public void setGradedAt(LocalDateTime gradedAt) {
    this.gradedAt = gradedAt;
  }
}

