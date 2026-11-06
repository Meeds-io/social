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
package io.meeds.social.lifecycle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalApplication;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.Application;

import jakarta.servlet.http.HttpServletRequest;

/**
 * A site template page previewed with a space puts that space in the request
 * context only when the preview space id is a valid id, the same check the
 * portal head applies to the value it prints.
 */
class SpaceSiteTemplateLifecycleTest {

  private static final SiteKey       TEMPLATE_SITE = SiteKey.groupTemplate("template");

  private SpaceService               spaceService;

  private PortalApplication          portalApplication;

  private PortalRequestContext       context;

  private HttpServletRequest         request;

  private SpaceSiteTemplateLifecycle lifecycle;

  @BeforeEach
  void setUp() throws Exception {
    spaceService = mock(SpaceService.class);
    portalApplication = mock(PortalApplication.class);
    context = mock(PortalRequestContext.class);
    request = mock(HttpServletRequest.class);
    when(context.getRequest()).thenReturn(request);
    lifecycle = new SpaceSiteTemplateLifecycle();
    try (MockedStatic<ExoContainerContext> containerContext = mockStatic(ExoContainerContext.class)) {
      containerContext.when(() -> ExoContainerContext.getService(SpaceService.class)).thenReturn(spaceService);
      lifecycle.onInit(portalApplication);
    }
  }

  @Test
  void validPreviewSpaceIdOnTemplateSiteSetsTheSpace() {
    Space space = new Space();
    when(spaceService.getSpaceById(42L)).thenReturn(space);
    givenRequest(TEMPLATE_SITE, "42");

    lifecycle.onStartRequest(portalApplication, context);

    verify(context).setAttribute(SpaceUtils.CURRENT_SPACE, space);
  }

  @Test
  void unknownSpaceSetsNothing() {
    givenRequest(TEMPLATE_SITE, "42");

    lifecycle.onStartRequest(portalApplication, context);

    verify(spaceService).getSpaceById(42L);
    verify(context, never()).setAttribute(eq(SpaceUtils.CURRENT_SPACE), any());
  }

  @Test
  void invalidPreviewSpaceIdIsNeverLookedUp() {
    for (String value : new String[] { "42abc", "+42", "-42", " 42", "" }) {
      givenRequest(TEMPLATE_SITE, value);

      lifecycle.onStartRequest(portalApplication, context);
    }

    verifyNoLookup();
  }

  @Test
  void previewSpaceIdOutsideTemplateSitesIsIgnored() {
    givenRequest(SiteKey.group("/spaces/space"), "42");
    lifecycle.onStartRequest(portalApplication, context);

    givenRequest(null, "42");
    lifecycle.onStartRequest(portalApplication, context);

    givenRequest(TEMPLATE_SITE, "42");
    lifecycle.onStartRequest(mock(Application.class), context);

    verifyNoLookup();
  }

  private void givenRequest(SiteKey siteKey, String previewSpaceId) {
    when(context.getSiteKey()).thenReturn(siteKey);
    when(request.getParameter(Utils.PREVIEW_SPACE_ID_PARAM)).thenReturn(previewSpaceId);
  }

  private void verifyNoLookup() {
    verify(spaceService, never()).getSpaceById(anyLong());
    verify(spaceService, never()).getSpaceById(anyString());
    verify(context, never()).setAttribute(eq(SpaceUtils.CURRENT_SPACE), any());
  }
}
