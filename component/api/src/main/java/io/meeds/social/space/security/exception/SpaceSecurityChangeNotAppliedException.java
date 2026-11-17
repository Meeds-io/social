/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.space.security.exception;

/**
 * A space security setting change that a contributor of
 * {@link io.meeds.social.space.security.plugin.SpaceSecuritySettingsPlugin}
 * failed to prepare: nothing was written.
 */
public class SpaceSecurityChangeNotAppliedException extends RuntimeException {

  public static final String MESSAGE_CODE     = "space.security.changeNotApplied";

  private static final long  serialVersionUID = 3407829153652127730L;

  public SpaceSecurityChangeNotAppliedException(Throwable cause) {
    super(MESSAGE_CODE, cause);
  }

}
