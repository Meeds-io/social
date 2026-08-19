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
package io.meeds.social.handler;

import static org.exoplatform.portal.application.PortalRequestHandler.REQUEST_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.ControllerContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProfileAccessHandlerTest {

  private static final String     USERNAME      = "john";

  private static final String     PROFILE_OWNER = "mary";

  @Mock
  private IdentityManager         identityManager;

  @Mock
  private UserPortalConfigService portalConfigService;

  @Mock
  private ControllerContext       controllerContext;

  @Mock
  private HttpServletRequest      request;

  @Mock
  private HttpServletResponse     response;

  @Mock
  private Identity                identity;

  @InjectMocks
  private ProfileAccessHandler    profileAccessHandler;

  @Before
  public void setUp() {
    when(controllerContext.getRequest()).thenReturn(request);
    when(controllerContext.getResponse()).thenReturn(response);
    when(request.getRemoteUser()).thenReturn(USERNAME);
    when(portalConfigService.getMetaPortal()).thenReturn("dw");
  }

  @Test
  public void testGetHandlerName() {
    assertEquals("profile-access", profileAccessHandler.getHandlerName());
  }

  @Test
  @SneakyThrows
  public void testLetsBlankPathThrough() {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn("");
    assertFalse(profileAccessHandler.execute(controllerContext));
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  @SneakyThrows
  public void testLetsOwnProfileThrough() {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn(USERNAME);
    assertFalse(profileAccessHandler.execute(controllerContext));
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  @SneakyThrows
  public void testLetsEnabledUserProfileThrough() {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn(PROFILE_OWNER + "/some/sub/path");
    when(identityManager.getOrCreateUserIdentity(PROFILE_OWNER)).thenReturn(identity);
    when(identity.isEnable()).thenReturn(true);
    when(identity.isDeleted()).thenReturn(false);
    assertFalse(profileAccessHandler.execute(controllerContext));
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  @SneakyThrows
  public void testRedirectsDisabledUserProfileToPageNotFound() {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn(PROFILE_OWNER);
    when(identityManager.getOrCreateUserIdentity(PROFILE_OWNER)).thenReturn(identity);
    when(identity.isEnable()).thenReturn(false);
    assertTrue(profileAccessHandler.execute(controllerContext));
    verify(response).sendRedirect("/portal/dw/page-not-found");
  }

  @Test
  @SneakyThrows
  public void testRedirectsDeletedUserProfileToPageNotFound() {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn(PROFILE_OWNER);
    when(identityManager.getOrCreateUserIdentity(PROFILE_OWNER)).thenReturn(identity);
    when(identity.isEnable()).thenReturn(true);
    when(identity.isDeleted()).thenReturn(true);
    assertTrue(profileAccessHandler.execute(controllerContext));
    verify(response).sendRedirect("/portal/dw/page-not-found");
  }

  @Test
  @SneakyThrows
  public void testRedirectsUnknownUserProfileToPageNotFound() {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn(PROFILE_OWNER);
    when(identityManager.getOrCreateUserIdentity(PROFILE_OWNER)).thenReturn(null);
    assertTrue(profileAccessHandler.execute(controllerContext));
    verify(response).sendRedirect("/portal/dw/page-not-found");
  }

  @Test
  @SneakyThrows
  public void testRedirectsAnonymousViewerToPublicPageNotFound() {
    when(request.getRemoteUser()).thenReturn(null);
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn(PROFILE_OWNER);
    when(identityManager.getOrCreateUserIdentity(PROFILE_OWNER)).thenReturn(identity);
    when(identity.isEnable()).thenReturn(false);
    assertTrue(profileAccessHandler.execute(controllerContext));
    verify(response).sendRedirect("/portal/public/page-not-found");
    verify(portalConfigService, never()).getMetaPortal();
  }

}
