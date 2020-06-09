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
package org.exoplatform.social.rest.api;

import java.util.*;

public class UserImportResultEntity implements Cloneable {

  private long                      count;

  private long                      processedCount;

  private Map<String, String>       errorMessages;

  private Map<String, List<String>> warnMessages;

  public UserImportResultEntity() {
  }

  public UserImportResultEntity(long count,
                                long processedCount,
                                Map<String, String> errorMessages,
                                Map<String, List<String>> warnMessages) {
    super();
    this.count = count;
    this.processedCount = processedCount;
    this.errorMessages = errorMessages;
    this.warnMessages = warnMessages;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public long getProcessedCount() {
    return processedCount;
  }

  public void setProcessedCount(long processedCount) {
    this.processedCount = processedCount;
  }

  public Map<String, String> getErrorMessages() {
    return errorMessages;
  }

  public void setErrorMessages(Map<String, String> errorMessages) {
    this.errorMessages = errorMessages;
  }

  public Map<String, List<String>> getWarnMessages() {
    return warnMessages;
  }

  public void setWarnMessages(Map<String, List<String>> warnMessages) {
    this.warnMessages = warnMessages;
  }

  public void incrementProcessed() {
    this.processedCount++;
  }

  public void addErrorMessage(String userName, String errorMessage) {
    if (errorMessages == null) {
      errorMessages = new HashMap<>();
    }
    errorMessages.put(userName, errorMessage);
  }

  public void addWarnMessage(String userName, String warnMessage) {
    if (warnMessages == null) {
      warnMessages = new HashMap<>();
    }
    warnMessages.computeIfAbsent(userName, key -> new ArrayList<>()).add(warnMessage);
  }

  @Override
  public UserImportResultEntity clone() { // NOSONAR
    Map<String, String> errorMessagesCopy = errorMessages == null ? null : Collections.unmodifiableMap(errorMessages);
    Map<String, List<String>> warnMessagesCopy = warnMessages == null ? null : Collections.unmodifiableMap(warnMessages);
    return new UserImportResultEntity(count, processedCount, errorMessagesCopy, warnMessagesCopy);
  }
}
