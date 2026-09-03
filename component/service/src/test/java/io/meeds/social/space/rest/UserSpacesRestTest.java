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
package io.meeds.social.space.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.constant.UserSpacesScope;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

/**
 * The REST contract of the profile spaces listing: parameter validation, the
 * status mapping, and the role the annotation actually requires.
 * <p>
 * The controller decides nothing about visibility — the scope travels to the
 * Service untouched, and these tests assert exactly that, because a controller
 * that "helpfully" defaulted or rejected a scope would move an access-control
 * decision out of the one place that owns it.
 */
@SpringBootTest(classes = { UserSpacesRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class UserSpacesRestTest {

  private static final String   OWNER_SPACES_PATH = "/users/john/spaces";

  private static final String   VIEWER            = "mary";

  private static final String   OWNER             = "john";

  private static final String   TEST_PASSWORD     = "testPassword";

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockBean
  private SpaceService          spaceService;

  private MockMvc               mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void testAnonymousIsRefused() throws Exception {
    // 403, not 401: on a @Secured method an anonymous request is an access
    // denial for this filter chain, as the sibling controllers' tests show
    // (CategoryLinkRestTest, ContentLinkRestTest). The 401 of CategoryRestTest
    // belongs to endpoints carrying no @Secured at all.
    mockMvc.perform(get(OWNER_SPACES_PATH)).andExpect(status().isForbidden());
    verify(spaceService, never()).getUserSpaces(anyString(), anyString(), any(), anyLong(), anyLong());
  }

  @Test
  public void testExternalViewerIsGrantedAccessByTheUsersRole() throws Exception {
    when(spaceService.getUserSpaces(anyString(), anyString(), any(), anyLong(), anyLong())).thenReturn(List.of());

    // An external identity carries both roles: the roles extractor adds users to
    // any identity holding externals. The annotation therefore lets them in, and
    // the Service — not this layer — narrows what they see.
    mockMvc.perform(get(OWNER_SPACES_PATH).with(externalViewer())).andExpect(status().isOk());
    verify(spaceService).getUserSpaces("external", OWNER, null, 0L, 20L);
  }

  @Test
  public void testViewerWithoutTheUsersRoleIsForbidden() throws Exception {
    mockMvc.perform(get(OWNER_SPACES_PATH).with(user("guest").password(TEST_PASSWORD)
                                                             .authorities(new SimpleGrantedAuthority("anything"))))
           .andExpect(status().isForbidden());
    verify(spaceService, never()).getUserSpaces(anyString(), anyString(), any(), anyLong(), anyLong());
  }

  @Test
  public void testScopeTravelsToTheServiceUntouched() throws Exception {
    when(spaceService.getUserSpaces(anyString(), anyString(), any(), anyLong(), anyLong())).thenReturn(List.of());

    mockMvc.perform(get(OWNER_SPACES_PATH + "?scope=ALL").with(viewer())).andExpect(status().isOk());
    verify(spaceService).getUserSpaces(VIEWER, OWNER, UserSpacesScope.ALL, 0L, 20L);

    mockMvc.perform(get(OWNER_SPACES_PATH + "?scope=COMMON").with(viewer())).andExpect(status().isOk());
    verify(spaceService).getUserSpaces(VIEWER, OWNER, UserSpacesScope.COMMON, 0L, 20L);
  }

  @Test
  public void testListingCarriesWhatTheUiNeeds() throws Exception {
    Space space = new Space();
    space.setId("42");
    space.setDisplayName("Alpha space");
    space.setPrettyName("alpha_space");
    space.setUrl("alpha_space");
    space.setVisibility(Space.PRIVATE);
    space.setMembers(new String[] { OWNER, "someoneelse" });
    when(spaceService.getUserSpaces(eq(VIEWER), eq(OWNER), any(), anyLong(), anyLong())).thenReturn(List.of(space));
    when(spaceService.isMember(space, VIEWER)).thenReturn(false);

    mockMvc.perform(get(OWNER_SPACES_PATH).with(viewer()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.spaces[0].id").value(42))
           .andExpect(jsonPath("$.spaces[0].displayName").value("Alpha space"))
           .andExpect(jsonPath("$.spaces[0].visibility").value(Space.PRIVATE))
           .andExpect(jsonPath("$.spaces[0].membersCount").value(2))
           // A private space the viewer is not in is listed; the flag is what
           // lets the UI render it without pretending the viewer belongs to it
           .andExpect(jsonPath("$.spaces[0].member").value(false))
           // doesNotExist() also passes for "size": null, so pin the raw shape
           .andExpect(content().string(not(containsString("\"size\""))));
  }

  @Test
  public void testTotalSizeIsOnlyComputedWhenAsked() throws Exception {
    when(spaceService.getUserSpaces(anyString(), anyString(), any(), anyLong(), anyLong())).thenReturn(List.of());

    mockMvc.perform(get(OWNER_SPACES_PATH).with(viewer())).andExpect(status().isOk());
    verify(spaceService, never()).countUserSpaces(anyString(), anyString(), any());

    when(spaceService.countUserSpaces(VIEWER, OWNER, null)).thenReturn(7);
    mockMvc.perform(get(OWNER_SPACES_PATH + "?returnSize=true").with(viewer()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.size").value(7));
  }

  @Test
  public void testUnknownProfileOwnerIsNotFound() throws Exception {
    when(spaceService.getUserSpaces(anyString(), anyString(), any(), anyLong(), anyLong()))
                                                                                          .thenThrow(ObjectNotFoundException.class);

    mockMvc.perform(get(OWNER_SPACES_PATH).with(viewer())).andExpect(status().isNotFound());
  }

  @Test
  public void testInvalidPagingIsRejected() throws Exception {
    mockMvc.perform(get(OWNER_SPACES_PATH + "?offset=-1").with(viewer())).andExpect(status().isBadRequest());
    mockMvc.perform(get(OWNER_SPACES_PATH + "?limit=0").with(viewer())).andExpect(status().isBadRequest());
    // The page size is bounded: it is a cache key dimension as well as a query
    // bound, so an unbounded value is refused rather than clamped silently
    mockMvc.perform(get(OWNER_SPACES_PATH + "?limit=1000").with(viewer())).andExpect(status().isBadRequest());
    verify(spaceService, never()).getUserSpaces(anyString(), anyString(), any(), anyLong(), anyLong());
  }

  @Test
  public void testUnknownScopeIsRejected() throws Exception {
    mockMvc.perform(get(OWNER_SPACES_PATH + "?scope=EVERYTHING").with(viewer())).andExpect(status().isBadRequest());
    verify(spaceService, never()).getUserSpaces(anyString(), anyString(), any(), anyLong(), anyLong());
  }

  private RequestPostProcessor viewer() {
    return user(VIEWER).password(TEST_PASSWORD).authorities(new SimpleGrantedAuthority("users"));
  }

  private RequestPostProcessor externalViewer() {
    return user("external").password(TEST_PASSWORD)
                           .authorities(new SimpleGrantedAuthority("users"), new SimpleGrantedAuthority("externals"));
  }

}
