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

import static io.meeds.social.handler.SpacePermanentLinkHandler.REQUEST_PATH;
import static io.meeds.social.handler.SpacePermanentLinkHandler.REQUEST_SPACE_ID;
import static io.meeds.social.space.plugin.SpacePermanentLinkPlugin.APPLICATION_URI;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.ControllerContext;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.service.PermanentLinkService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Opening a space through /portal/s/{spaceId} lands on the space home. When
 * the page of the space home node is a link the user can access, the handler
 * redirects to that link, as the menus listing the node do; otherwise, and for
 * any sub-path, it redirects to the space page as before.
 */
@ExtendWith(MockitoExtension.class)
class SpacePermanentLinkHandlerTest {

  private static final String       USERNAME       = "john";

  private static final String       SPACE_ID       = "12";

  private static final String       GROUP_ID       = "/spaces/spacex";

  private static final String       SPACE_URL      = "home";

  private static final String       SPACE_PAGE_URI = "/portal/g/:spaces:spacex/home/";

  private static final String       HOME_LINK      = "https://www.example.org/target";

  private static final PageKey      HOME_PAGE_KEY  = new PageKey(SiteKey.group(GROUP_ID), "home");

  @Mock
  private SpaceService              spaceService;

  @Mock
  private UserPortalConfigService   portalConfigService;

  @Mock
  private PermanentLinkService      permanentLinkService;

  @Mock
  private NavigationService         navigationService;

  @Mock
  private ControllerContext         controllerContext;

  @Mock
  private HttpServletRequest        request;

  @Mock
  private HttpServletResponse       response;

  @Mock
  private Space                     space;

  @InjectMocks
  private SpacePermanentLinkHandler handler;

  @BeforeEach
  void setUp() throws Exception {
    lenient().when(controllerContext.getRequest()).thenReturn(request);
    lenient().when(controllerContext.getResponse()).thenReturn(response);
    lenient().when(controllerContext.getParameter(REQUEST_SPACE_ID)).thenReturn(SPACE_ID);
    lenient().when(request.getRemoteUser()).thenReturn(USERNAME);
    lenient().when(spaceService.getSpaceById(SPACE_ID)).thenReturn(space);
    lenient().when(space.getGroupId()).thenReturn(GROUP_ID);
    lenient().when(space.getUrl()).thenReturn(SPACE_URL);
    lenient().when(space.getPublicSiteId()).thenReturn(0L);
    lenient().when(space.getVisibility()).thenReturn(Space.PRIVATE);
    lenient().when(space.getRegistration()).thenReturn(Space.OPEN);
    lenient().when(spaceService.canViewSpace(space, USERNAME)).thenReturn(true);
    lenient().when(permanentLinkService.getLink(any())).thenReturn(SPACE_PAGE_URI);
  }

  @Test
  void testRedirectsSpaceHomeToItsLink() throws Exception {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn("");
    mockHomePage(PageType.LINK, HOME_LINK);

    assertTrue(handler.execute(controllerContext));

    verify(response).sendRedirect(HOME_LINK);
    verify(permanentLinkService, never()).getLink(any());
  }

  @Test
  void testRedirectsSpaceHomeWithoutPathToItsLink() throws Exception {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn(null);
    mockHomePage(PageType.LINK, HOME_LINK);

    assertTrue(handler.execute(controllerContext));

    verify(response).sendRedirect(HOME_LINK);
  }

  @Test
  void testRedirectsSpaceHomeToSpacePageWhenItsPageIsNotALink() throws Exception {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn("");
    mockHomePage(PageType.PAGE, HOME_LINK);

    assertTrue(handler.execute(controllerContext));

    verify(response).sendRedirect(SPACE_PAGE_URI);
    verify(response, never()).sendRedirect(HOME_LINK);
  }

  @Test
  void testRedirectsSpaceHomeToSpacePageWhenTheLinkIsBlank() throws Exception {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn("");
    mockHomePage(PageType.LINK, " ");

    assertTrue(handler.execute(controllerContext));

    verify(response).sendRedirect(SPACE_PAGE_URI);
  }

  @Test
  void testRedirectsSpaceHomeToSpacePageWhenTheUserCannotAccessTheLinkPage() throws Exception {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn("");
    mockHomeNode();
    when(portalConfigService.getPage(HOME_PAGE_KEY, USERNAME)).thenReturn(null);

    assertTrue(handler.execute(controllerContext));

    verify(response).sendRedirect(SPACE_PAGE_URI);
    verify(response, never()).sendRedirect(HOME_LINK);
  }

  @Test
  void testRedirectsSpaceHomeToSpacePageWhenTheHomeNodeIsMissing() throws Exception {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn("");
    when(navigationService.loadNode(SiteKey.group(GROUP_ID), SPACE_URL)).thenReturn(null);

    assertTrue(handler.execute(controllerContext));

    verify(response).sendRedirect(SPACE_PAGE_URI);
    verify(portalConfigService, never()).getPage(any(PageKey.class), anyString());
  }

  @Test
  void testKeepsTheSpacePageForASubPathEvenWhenTheHomeIsALink() throws Exception {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn("members");
    mockHomePage(PageType.LINK, HOME_LINK);

    assertTrue(handler.execute(controllerContext));

    verify(response).sendRedirect(SPACE_PAGE_URI);
    verify(response, never()).sendRedirect(HOME_LINK);
    verify(permanentLinkService).getLink(argThat((PermanentLinkObject object) -> "members".equals(object.getParameters()
                                                                                                         .get(APPLICATION_URI))));
    verify(navigationService, never()).loadNode(any(SiteKey.class), anyString());
  }

  @Test
  void testKeepsTheSpacePageForAQueryStringEvenWhenTheHomeIsALink() throws Exception {
    when(controllerContext.getParameter(REQUEST_PATH)).thenReturn("");
    when(request.getQueryString()).thenReturn("tab=1");
    mockHomePage(PageType.LINK, HOME_LINK);

    assertTrue(handler.execute(controllerContext));

    verify(response).sendRedirect(SPACE_PAGE_URI);
    verify(response, never()).sendRedirect(HOME_LINK);
    verify(navigationService, never()).loadNode(any(SiteKey.class), anyString());
  }

  /**
   * Lenient: the tests of a sub-path or a query string stub a link home too,
   * so that a handler consulting the home for them redirects to the link and
   * fails on the redirect, not only on an interaction.
   */
  private void mockHomePage(PageType pageType, String link) {
    mockHomeNode();
    PageState pageState = mock(PageState.class);
    lenient().when(pageState.getType()).thenReturn(pageType.name());
    lenient().when(pageState.getLink()).thenReturn(link);
    lenient().when(portalConfigService.getPage(HOME_PAGE_KEY, USERNAME)).thenReturn(new PageContext(HOME_PAGE_KEY, pageState));
  }

  @SuppressWarnings("unchecked")
  private void mockHomeNode() {
    NodeContext<NodeContext<Object>> homeNode = mock(NodeContext.class);
    lenient().when(homeNode.getState()).thenReturn(new NodeState.Builder().pageRef(HOME_PAGE_KEY).build());
    lenient().when(navigationService.loadNode(SiteKey.group(GROUP_ID), SPACE_URL)).thenReturn(homeNode);
  }

}
