/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.oauth.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth exception
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthException extends RuntimeException {

  private static final long         serialVersionUID = -7034897190745766989L;

  // Specify error code
  private final OAuthExceptionCode  exceptionCode;

  // Context with additional attributes about error
  private final Map<String, Object> exceptionAttributes;

  public OAuthException() {
    super();
    this.exceptionCode = OAuthExceptionCode.UNKNOWN_ERROR;
    this.exceptionAttributes = new HashMap<String, Object>();
  }

  public OAuthException(OAuthExceptionCode exceptionCode, Map<String, Object> exceptionAttributes, String message) {
    super(message);
    this.exceptionCode = exceptionCode;
    this.exceptionAttributes = exceptionAttributes == null ? new HashMap<String, Object>() : exceptionAttributes;
  }

  public OAuthException(OAuthExceptionCode exceptionCode,
                        Map<String, Object> exceptionAttributes,
                        String message,
                        Throwable cause) {
    super(message, cause);
    this.exceptionCode = exceptionCode;
    this.exceptionAttributes = exceptionAttributes == null ? new HashMap<String, Object>() : exceptionAttributes;
  }

  public OAuthException(OAuthExceptionCode exceptionCode, Map<String, Object> exceptionAttributes, Throwable cause) {
    super(cause);
    this.exceptionCode = exceptionCode;
    this.exceptionAttributes = exceptionAttributes == null ? new HashMap<String, Object>() : exceptionAttributes;
  }

  public OAuthException(OAuthExceptionCode exceptionCode, String message) {
    this(exceptionCode, null, message);
  }

  public OAuthException(OAuthExceptionCode exceptionCode, Throwable cause) {
    this(exceptionCode, (Map<String, Object>) null, cause);
  }

  public OAuthException(OAuthExceptionCode exceptionCode, String message, Throwable cause) {
    this(exceptionCode, null, message, cause);
  }

  public OAuthExceptionCode getExceptionCode() {
    return exceptionCode;
  }

  public Map<String, Object> getExceptionAttributes() {
    return Collections.unmodifiableMap(exceptionAttributes);
  }

  public Object getExceptionAttribute(String attrName) {
    return exceptionAttributes.get(attrName);
  }

  @Override
  public String getMessage() {
    return exceptionCode + ": " + super.getMessage();
  }

}
