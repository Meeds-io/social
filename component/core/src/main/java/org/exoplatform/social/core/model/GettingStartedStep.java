package org.exoplatform.social.core.model;

import java.io.Serializable;

public class GettingStartedStep implements Serializable {

  private String  name;

  private Boolean status;

  public GettingStartedStep() {
  }

  public GettingStartedStep(String name, Boolean status) {
    this.name = name;
    this.status = status;
  }

  /*
   * @return the step name
   */
  public String getName() {
    return name;
  }

  /*
   * @param name the step name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /*
   * @return the step status
   */
  public Boolean getStatus() {
    return status;
  }

  /*
   * @param status the status to set
   */
  public void setStatus(Boolean status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Step [name=" + name + ", status=" + status + "]";
  }

}
