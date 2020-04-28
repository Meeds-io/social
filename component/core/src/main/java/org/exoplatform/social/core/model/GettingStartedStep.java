/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
