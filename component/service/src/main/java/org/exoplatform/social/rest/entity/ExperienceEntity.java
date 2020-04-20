package org.exoplatform.social.rest.entity;

public class ExperienceEntity {

  private String  id;

  private String  company;

  private String  description;

  private String  position;

  private String  skills;

  private Boolean isCurrent;

  private String  startDate;

  private String  endDate;

  public ExperienceEntity() {
  }

  public ExperienceEntity(String id,
                          String company,
                          String description,
                          String position,
                          String skills,
                          Boolean isCurrent,
                          String startDate,
                          String endDate) {
    this.id = id;
    this.company = company;
    this.description = description;
    this.position = position;
    this.skills = skills;
    this.isCurrent = isCurrent;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

  public Boolean getIsCurrent() {
    return isCurrent;
  }

  public void setIsCurrent(Boolean isCurrent) {
    this.isCurrent = isCurrent;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

}
