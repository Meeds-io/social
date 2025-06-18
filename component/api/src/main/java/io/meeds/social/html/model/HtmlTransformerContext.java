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
package io.meeds.social.html.model;

import java.util.Locale;

import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HtmlTransformerContext {

  private Identity userIdentity;

  private boolean  system;

  private Locale   locale;

  private boolean  isTextOnly;

  public HtmlTransformerContext(Locale locale) {
    this.locale = locale;
  }

  public HtmlTransformerContext(Identity userIdentity, Locale locale) {
    this.userIdentity = userIdentity;
    this.locale = locale;
  }

  public HtmlTransformerContext(boolean system, Locale locale) {
    this.system = system;
    this.locale = locale;
  }

  public HtmlTransformerContext(Identity userIdentity, Locale locale,  boolean isTextOnly) {
    this.userIdentity = userIdentity;
    this.locale = locale;
    this.isTextOnly = isTextOnly;
  }

  public String getUsername() {
    return userIdentity == null || IdentityConstants.ANONIM.equals(userIdentity.getUserId()) ? null : userIdentity.getUserId();
  }

}
