package org.exoplatform.deprecation;

import java.lang.annotation.*;

/**
 * An annotation that could be used on Methods to enable logging about usage of
 * deprecated API or REST endpoints. This logging will be enabled only on
 * DEVELOPMENT MODE!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DeprecatedAPI {

  /**
   * @return Message to log in console
   */
  String value();

  /**
   * @return true if logging should be made each time the method is used, else
   *         logged once during the runtime
   */
  boolean insist() default false;

}
