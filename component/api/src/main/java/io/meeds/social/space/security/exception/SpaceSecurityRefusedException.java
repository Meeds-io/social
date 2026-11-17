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
 * A refusal of a space security setting. Its message is the message code
 * naming the rule that refused, which a client turns into its label; it never
 * carries a document's name or path.
 */
public class SpaceSecurityRefusedException extends IllegalAccessException {

  private static final long serialVersionUID = -5148201657435468215L;

  public SpaceSecurityRefusedException(String messageCode) {
    super(messageCode);
  }

  public String getMessageCode() {
    return getMessage();
  }

}
