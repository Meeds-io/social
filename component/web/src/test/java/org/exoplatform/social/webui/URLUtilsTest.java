/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.webui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.application.RequestContext;

/**
 * The profile-mode discriminator of the User Spaces List widget (EXO-89465):
 * {@link URLUtils#getStreamOwnerId()} must name the profile owner on BOTH
 * shapes of the profile URL — /profile/{username} (visited profile) and the
 * bare /profile the navigation uses for one's own profile — and stay null on
 * any other page, or the widget silently falls back to analytics mode.
 */
class URLUtilsTest {

  private static final String        VIEWER = "mary";

  private MockedStatic<RequestContext> requestContext;

  private PortalRequestContext        pcontext;

  private ControllerContext           controllerContext;

  @BeforeEach
  void setUp() {
    pcontext = mock(PortalRequestContext.class);
    controllerContext = mock(ControllerContext.class);
    when(pcontext.getControllerContext()).thenReturn(controllerContext);
    when(pcontext.getSiteType()).thenReturn(SiteType.PORTAL);
    requestContext = mockStatic(RequestContext.class);
    requestContext.when(RequestContext::getCurrentInstance).thenReturn(pcontext);
    // The same route the production config registers for the profile page
    ExoRouter.reset();
    ExoRouter.addRoute("/profile/{streamOwnerId}", "profile.owner.show");
  }

  @AfterEach
  void tearDown() {
    requestContext.close();
    ExoRouter.reset();
  }

  private void givenRequestPath(String path) {
    when(controllerContext.getParameter(RequestNavigationData.REQUEST_PATH)).thenReturn(path);
  }

  @Test
  void ownProfileWithoutUsernameSegmentReturnsTheViewer() {
    givenRequestPath("profile");
    when(pcontext.getRemoteUser()).thenReturn(VIEWER);
    assertEquals(VIEWER, URLUtils.getStreamOwnerId());
  }

  @Test
  void visitedProfileReturnsTheUserCarriedByTheUrl() {
    givenRequestPath("profile/john");
    Identity identity = mock(Identity.class);
    when(identity.getRemoteId()).thenReturn("john");
    IdentityManager identityManager = mock(IdentityManager.class);
    when(identityManager.getOrCreateUserIdentity("john")).thenReturn(identity);
    try (MockedStatic<ExoContainerContext> containerContext = mockStatic(ExoContainerContext.class)) {
      containerContext.when(() -> ExoContainerContext.getService(IdentityManager.class)).thenReturn(identityManager);
      assertEquals("john", URLUtils.getStreamOwnerId());
    }
  }

  @Test
  void nonProfilePageCarriesNoOwner() {
    givenRequestPath("home");
    assertNull(URLUtils.getStreamOwnerId());
  }

  @Test
  void bareProfileWithoutAuthenticatedViewerCarriesNoOwner() {
    givenRequestPath("profile");
    when(pcontext.getRemoteUser()).thenReturn(null);
    assertNull(URLUtils.getStreamOwnerId());
  }

  @Test
  void bareProfileWithTrailingSlashReturnsTheViewer() {
    givenRequestPath("profile/");
    when(pcontext.getRemoteUser()).thenReturn(VIEWER);
    assertEquals(VIEWER, URLUtils.getStreamOwnerId());
  }

  @Test
  void groupSitePageNamedProfileCarriesNoOwner() {
    // A space whose pretty name is "profile" navigates with the same
    // single-segment path on a GROUP site — it must not gain an implicit
    // owner. The viewer stub matters: without it a guard that wrongly fires
    // here still returns null and the pin passes against the very bug.
    givenRequestPath("profile");
    when(pcontext.getSiteType()).thenReturn(SiteType.GROUP);
    when(pcontext.getRemoteUser()).thenReturn(VIEWER);
    assertNull(URLUtils.getStreamOwnerId());
  }
}
