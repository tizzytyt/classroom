package com.edu.classroom.dto.student;

public class StudentEmergencyContactDto {
  private String emergencyContactName;
  private String emergencyContactPhone;
  private String emergencyContactRelation;

  public String getEmergencyContactName() {
    return emergencyContactName;
  }

  public void setEmergencyContactName(String emergencyContactName) {
    this.emergencyContactName = emergencyContactName;
  }

  public String getEmergencyContactPhone() {
    return emergencyContactPhone;
  }

  public void setEmergencyContactPhone(String emergencyContactPhone) {
    this.emergencyContactPhone = emergencyContactPhone;
  }

  public String getEmergencyContactRelation() {
    return emergencyContactRelation;
  }

  public void setEmergencyContactRelation(String emergencyContactRelation) {
    this.emergencyContactRelation = emergencyContactRelation;
  }
}
