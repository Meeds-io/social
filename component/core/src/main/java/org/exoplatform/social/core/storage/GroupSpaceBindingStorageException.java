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

package org.exoplatform.social.core.storage;

import org.exoplatform.social.common.ExoSocialException;

/**
 * Class to handle group/space binding exception
 */
public class GroupSpaceBindingStorageException extends ExoSocialException {

  private static final String MESSAGE_BUNDLE_DELIMITER = ".";

  public static enum Type {
    ILLEGAL_ARGUMENTS("Illegal_Arguments");
    ;

    private final String msgKey;

    private Type(String msgKey) {
      this.msgKey = msgKey;
    }

    @Override
    public String toString() {
      return this.getClass() + MESSAGE_BUNDLE_DELIMITER + msgKey;
    }
  }

  public GroupSpaceBindingStorageException(Type type) {
    super(type.toString());
  }

  public GroupSpaceBindingStorageException(Type type, String[] messageArguments) {
    super(type.toString(), messageArguments);
  }

  public GroupSpaceBindingStorageException(Type type, String message) {
    super(type.toString(), message);
  }

  public GroupSpaceBindingStorageException(Type type, String message, Throwable cause) {
    super(type.toString(), message, cause);
  }

  public GroupSpaceBindingStorageException(Type type, String[] messageArguments, String message, Throwable cause) {
    super(type.toString(), messageArguments, message, cause);
  }

}
