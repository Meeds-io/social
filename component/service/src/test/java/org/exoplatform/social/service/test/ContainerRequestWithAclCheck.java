/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package org.exoplatform.social.service.test;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.services.rest.impl.ContainerRequest;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

public class ContainerRequestWithAclCheck extends ContainerRequest {

  public ContainerRequestWithAclCheck(String method,
                                      URI requestUri,
                                      URI baseUri,
                                      InputStream entityStream,
                                      MultivaluedMap<String, String> httpHeaders) {
    super(method, requestUri, baseUri, entityStream, httpHeaders);
  }

  @Override
  public boolean isUserInRole(String role) {
    ConversationState conversationState = ConversationState.getCurrent();
    Identity identity = conversationState == null ? null : conversationState.getIdentity();
    return identity != null && ("root".equals(identity.getUserId())
                                || identity.getRoles().contains(role)
                                || identity.isMemberOf("/platform/" + role));
  }

}
