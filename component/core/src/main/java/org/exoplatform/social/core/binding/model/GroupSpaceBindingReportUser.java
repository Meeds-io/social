package org.exoplatform.social.core.binding.model;

import java.util.Date;

public class GroupSpaceBindingReportUser {

  /** The id */
  private long               id;

  private long               bindingReportActionId;

  /** The username */
  private String             username;

  /** The action */
  private String             action;

  /** true if the user was present in the space before the binding */
  private boolean            wasPresentBefore;

  /**
   * true if the user is still present in space after remove. false if the user is
   * no more in space after remove null for other actions
   */
  private boolean            stillInSpace;

  /** The action startDate */
  private Date               date                 = new Date();

  public static final String UPDATE_ADD_ACTION    = "ADD_USER";

  public static final String UPDATE_REMOVE_ACTION = "REMOVE_USER";

  public GroupSpaceBindingReportUser() {
  }

  public GroupSpaceBindingReportUser(long bindingReportActionId, String username, String action) {
    this.bindingReportActionId = bindingReportActionId;
    this.username = username;
    this.action = action;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getBindingReportActionId() {
    return bindingReportActionId;
  }

  public void setBindingReportActionId(long bindingReportActionId) {
    this.bindingReportActionId = bindingReportActionId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public boolean isWasPresentBefore() {
    return wasPresentBefore;
  }

  public void setWasPresentBefore(boolean wasPresentBefore) {
    this.wasPresentBefore = wasPresentBefore;
  }

  public boolean isStillInSpace() {
    return stillInSpace;
  }

  public void setStillInSpace(boolean stillInSpace) {
    this.stillInSpace = stillInSpace;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
