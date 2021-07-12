package org.exoplatform.deprecation;

public class DeprecatedAPIException extends RuntimeException {

  private static final long       serialVersionUID = -3775758055269420020L;

  private final StackTraceElement stackTraceElement;

  public DeprecatedAPIException(String message, Class<?> deprecatedClass, String deprecatedMethod) {
    super(message);
    stackTraceElement = new StackTraceElement(deprecatedClass.getName(), deprecatedMethod, deprecatedClass.getSimpleName(), -1);
  }

  @Override
  public StackTraceElement[] getStackTrace() {
    return new StackTraceElement[] { stackTraceElement };
  }
}
