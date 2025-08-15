/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
