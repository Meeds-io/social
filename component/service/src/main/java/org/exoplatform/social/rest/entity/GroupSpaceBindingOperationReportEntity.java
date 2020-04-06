package org.exoplatform.social.rest.entity;

public class GroupSpaceBindingOperationReportEntity extends BaseEntity {

  private static final long serialVersionUID = -8049916455960643317L;

  public GroupSpaceBindingOperationReportEntity() {
  }

  public GroupSpaceBindingOperationReportEntity setSpaceId(String spaceId) {
    setProperty("spaceId", spaceId);
    return this;
  }

  public String getSpaceId() {
    return getString("spaceId");
  }

  public GroupSpaceBindingOperationReportEntity setGroup(String group) {
    setProperty("group", group);
    return this;
  }

  public String getGroup() {
    return getString("group");
  }

  public GroupSpaceBindingOperationReportEntity setAction(String action) {
    setProperty("action", action);
    return this;
  }

  public String getAction() {
    return getString("action");
  }

  public GroupSpaceBindingOperationReportEntity setBindingId(String bindingId) {
    setProperty("bindingId", bindingId);
    return this;
  }

  public String getBindingId() {
    return getString("bindingId");
  }

  public GroupSpaceBindingOperationReportEntity setAddedUsersCount(String addedUsers) {
    setProperty("addedUsers", addedUsers);
    return this;
  }

  public String getAddedUsersCount() {
    return getString("addedUsers");
  }

  public GroupSpaceBindingOperationReportEntity setRemovedUsersCount(String removedUsers) {
    setProperty("removedUsers", removedUsers);
    return this;
  }

  public String getRemovedUsersCount() {
    return getString("removedUsers");
  }

  public GroupSpaceBindingOperationReportEntity setStartDate(String startDate) {
    setProperty("startDate", startDate);
    return this;
  }

  public String getStartDate() {
    return getString("startDate");
  }

  public GroupSpaceBindingOperationReportEntity setEndDate(String endDate) {
    setProperty("endDate", endDate);
    return this;
  }

  public String getEndDate() {
    return getString("endDate");
  }
}
