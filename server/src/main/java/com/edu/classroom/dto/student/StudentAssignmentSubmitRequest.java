package com.edu.classroom.dto.student;

import java.util.List;

public class StudentAssignmentSubmitRequest {
  private String text;
  /** 学生端上传接口返回的 /api/files/... 路径列表 */
  private List<String> attachmentUrls;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public List<String> getAttachmentUrls() {
    return attachmentUrls;
  }

  public void setAttachmentUrls(List<String> attachmentUrls) {
    this.attachmentUrls = attachmentUrls;
  }
}
