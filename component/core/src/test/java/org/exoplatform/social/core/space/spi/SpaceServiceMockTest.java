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
package org.exoplatform.social.core.space.spi;

import io.meeds.social.search.SpaceSearchConnector;
import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;
import io.meeds.social.space.service.SpaceServiceImpl;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceLifecycle;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;
import org.exoplatform.web.security.security.CookieTokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpaceServiceMockTest {

  @Mock
  private SpaceStorage             spaceStorage;

  @Mock
  private SpaceSearchConnector     spaceSearchConnector;

  @Mock
  private GroupSpaceBindingStorage groupSpaceBindingStorage;

  @Mock
  private IdentityManager          identityManager;

  @Mock
  private UserACL                  userAcl;

  @Mock
  private ResourceBundleService    resourceBundleService;

  @Mock
  private LocaleConfigService      localeConfigService;

  @Mock
  private OrganizationService      organizationService;

  @Mock
  private SpaceTemplateService     spaceTemplateService;

  @Mock
  private FileService              fileService;

  @Mock
  private CookieTokenService       cookieTokenService;

  @Mock
  private SpaceLifecycle           spaceLifeCycle;

  @Mock
  ExoContainer                     container;

  @InjectMocks
  private SpaceServiceImpl         spaceService;

  @Test
  public void testCreateSubspace() throws Exception {

    SpaceTemplate parentTemplate = mock(SpaceTemplate.class);
    long parentTemplateId = 1L;
    long notAllowedTemplateId = 4L;
    when(parentTemplate.getId()).thenReturn(parentTemplateId);
    List<String> allowedSubspaceTemplates = new ArrayList<>();
    allowedSubspaceTemplates.add("2:1");
    allowedSubspaceTemplates.add("3:2");
    int globalLimit = 2;
    when(parentTemplate.getAllowedSubspaceTemplates()).thenReturn(allowedSubspaceTemplates);
    when(parentTemplate.getSubspacesMaxLimit()).thenReturn(globalLimit);

    Space parentSpace = mock(Space.class);
    when(parentSpace.getSpaceId()).thenReturn(1L);
    when(parentSpace.getDisplayName()).thenReturn("parentSpace");
    when(parentSpace.getTemplateId()).thenReturn(parentTemplateId);

    Space subSpace = mock(Space.class);
    when(subSpace.getId()).thenReturn("2");
    when(subSpace.getDisplayName()).thenReturn("subSpace");
    when(subSpace.getTemplateId()).thenReturn(notAllowedTemplateId);
    String notAllowedUserName = "notAllowedUserName";
    String userName = "userName";
    try (var mockedCtx = Mockito.mockStatic(ExoContainerContext.class); var mockedUtils = Mockito.mockStatic(SpaceUtils.class)) {
      mockedCtx.when(() -> ExoContainerContext.getService(SpaceTemplateService.class)).thenReturn(spaceTemplateService);
      // user not allowed
      when(spaceTemplateService.canCreateSpace(subSpace.getTemplateId(), notAllowedUserName)).thenReturn(false);
      Exception exception = assertThrows(SpaceException.class,
                                         () -> spaceService.createSpace(subSpace,
                                                                        notAllowedUserName,
                                                                        null,
                                                                        parentSpace.getSpaceId()));

      assertEquals(String.format("User %s isn't allowed to create space with template %s",
                                 notAllowedUserName,
                                 subSpace.getTemplateId()),
                   exception.getMessage());

      // not allowed space template
      when(spaceTemplateService.canCreateSpace(anyLong(), any())).thenReturn(true);
      when(spaceStorage.getSpaceById(parentSpace.getSpaceId())).thenReturn(parentSpace);
      when(parentSpace.getMembers()).thenReturn(new String[] { userName });
      when(spaceTemplateService.getSpaceTemplate(parentSpace.getTemplateId())).thenReturn(parentTemplate);
      SpaceFilter filter = new SpaceFilter();
      filter.setParentSpaceId(parentSpace.getSpaceId());
      when(spaceStorage.getAllSpacesByFilterCount(filter)).thenReturn(0);
      exception = assertThrows(SpaceException.class,
                               () -> spaceService.createSpace(subSpace, userName, null, parentSpace.getSpaceId()));
      assertEquals("Subspace template '%s' is not allowed under parent template '%s'".formatted(subSpace.getTemplateId(),
                                                                                                parentTemplate.getId()),
                   exception.getMessage());

      // specific space template limit reached
      long allowedSubspaceTemplateId = 2;
      filter.setTemplateIds(List.of(allowedSubspaceTemplateId));
      when(spaceStorage.getAllSpacesByFilterCount(filter)).thenReturn(1);
      when(subSpace.getTemplateId()).thenReturn(allowedSubspaceTemplateId);
      exception = assertThrows(SpaceException.class,
                               () -> spaceService.createSpace(subSpace, userName, null, parentSpace.getSpaceId()));
      assertEquals("Cannot create more subspaces of template '%s' (max %d reached under '%s')".formatted(subSpace.getTemplateId(),
                                                                                                         1,
                                                                                                         parentSpace.getDisplayName()),
                   exception.getMessage());

      // global limit reached
      filter.setTemplateIds(null);
      when(spaceStorage.getAllSpacesByFilterCount(filter)).thenReturn(globalLimit).thenReturn(1);
      exception = assertThrows(SpaceException.class,
                               () -> spaceService.createSpace(subSpace, userName, null, parentSpace.getSpaceId()));
      assertEquals("Cannot create more subspaces under '%s' (max %d reached)".formatted(parentSpace.getDisplayName(),
                                                                                        globalLimit),
                   exception.getMessage());

      // success
      filter.setTemplateIds(List.of(allowedSubspaceTemplateId));
      when(spaceStorage.getAllSpacesByFilterCount(filter)).thenReturn(0);
      SpaceTemplate spaceTemplate = mock(SpaceTemplate.class);
      when(spaceTemplate.isEnabled()).thenReturn(true);
      when(spaceTemplate.isDeleted()).thenReturn(false);
      when(spaceTemplate.getSpaceDefaultRegistration()).thenReturn(SpaceRegistration.OPEN);
      when(spaceTemplate.getSpaceDefaultVisibility()).thenReturn(SpaceVisibility.PUBLIC);
      mockedUtils.when(() -> SpaceUtils.createGroup(anyString(), anyString(), anyString())).thenReturn("/spaces/subspace");
      when(spaceTemplateService.getSpaceTemplate(subSpace.getTemplateId())).thenReturn(spaceTemplate);
      when(spaceStorage.saveSpace(any(Space.class), anyBoolean(), anyLong())).thenReturn(subSpace);
      spaceService.createSpace(subSpace, userName, null, parentSpace.getSpaceId());

      verify(spaceTemplateService, times(1)).getSpaceTemplate(subSpace.getTemplateId());
      verify(spaceStorage, times(1)).saveSpace(any(Space.class), anyBoolean(), anyLong());
    }
  }

  @Test
  public void testGetSpacesByParentSpaceId() throws Exception {
    long parentSpaceId = 1L;
    int count = 2;
    Space space1 = new Space();
    space1.setId(10L);
    space1.setParentSpaceId(parentSpaceId);
    Space space2 = new Space();
    space2.setId(11L);
    space2.setParentSpaceId(parentSpaceId);
    List<Space> spaces = List.of(space1, space2);
    when(spaceStorage.getAllSpacesByFilterCount(any(SpaceFilter.class))).thenReturn(spaces.size());
    // When
    SpaceFilter filter = new SpaceFilter();
    filter.setParentSpaceId(parentSpaceId);
    ListAccess<Space> result = spaceService.getAllSpacesByFilter(filter);
    // Then
    assertNotNull(result);
    assertEquals(count, result.getSize());
    verify(spaceStorage, times(1)).getAllSpacesByFilterCount(any(SpaceFilter.class));
  }

}
