package org.exoplatform.social.common;

import lombok.Getter;

public class ObjectAlreadyExistsException extends Exception {

  private static final long serialVersionUID = -9018382456987071070L;

  @Getter
  private final Object      existingObject;                          // NOSONAR

  public ObjectAlreadyExistsException(Object existingObject) {
    this.existingObject = existingObject;
  }

  public ObjectAlreadyExistsException(Object existingObject, String message) {
    super(message);
    this.existingObject = existingObject;
  }

  public ObjectAlreadyExistsException(Object existingObject, String message, Throwable e) {
    super(message, e);
    this.existingObject = existingObject;
  }

}
