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
package io.meeds.oauth.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * General class, which encapsulates all important information about OAuth
 * access token. Various implementation of OAuth providers should override this
 * class and add their own data related to their access tokens
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@EqualsAndHashCode
@ToString
public abstract class AccessTokenContext {

  public static final String DELIMITER = " ";

  private final Set<String>  scopes    = new HashSet<>();

  protected AccessTokenContext(String... scopes) {
    if (scopes != null && scopes.length > 0) {
      for (String scope : scopes) {
        this.scopes.add(scope);
      }
    }
  }

  protected AccessTokenContext(String scopesAsString) {
    if (scopesAsString == null) {
      scopesAsString = "";
    }
    String[] scopesArray = scopesAsString.split(DELIMITER);
    for (String scope : scopesArray) {
      this.scopes.add(scope);
    }
  }

  protected AccessTokenContext(Collection<String> scopes) {
    if (scopes != null) {
      this.scopes.addAll(scopes);
    }
  }

  public boolean isScopeAvailable(String scope) {
    return scopes.contains(scope);
  }

  public String getScopesAsString() {
    Iterator<String> iterator = scopes.iterator();
    StringBuilder result;

    if (iterator.hasNext()) {
      result = new StringBuilder(iterator.next());
    } else {
      return "";
    }

    while (iterator.hasNext()) {
      result.append(DELIMITER + iterator.next());
    }
    return result.toString();
  }

  public boolean addScope(String scope) {
    return scopes.add(scope);
  }

  /**
   * @return String representation of access token
   */
  public abstract String getAccessToken();

}
