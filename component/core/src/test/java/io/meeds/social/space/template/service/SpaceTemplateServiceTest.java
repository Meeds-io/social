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
package io.meeds.social.space.template.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.attachment.AttachmentService;

import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.model.SpaceTemplateFilter;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;
import io.meeds.social.translation.service.TranslationService;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateServiceTest {

  private static final String       SPACE_FIELDS                  = "spaceFields";

  private static final String       SPACE_DELETE_PERMISSIONS      = "spaceDeletePermissions";

  private static final String       SPACE_LAYOUT_PERMISSIONS      = "spaceLayoutPermissions";

  private static final String       SPACE_PUBLIC_SITE_PERMISSIONS = "spacePublicSitePermissions";

  private static final String       CREATE_AND_ACCESS_PERMISSIONS = "permissions";

  private static final String       ADMIN_PERMISSIONS             = "adminPermissions";

  private static final String       TEST_USER                     = "testuser";

  private static final long         SPACE_CATEGORY_ID             = 2l;

  @Mock
  protected TranslationService      translationService;

  @Mock
  protected AttachmentService       attachmentService;

  @Mock
  protected UserPortalConfigService userPortalConfigService;

  @Mock
  protected LayoutService           layoutService;

  @Mock
  protected NavigationService       navigationService;

  @Mock
  protected ListenerService         listenerService;

  @Mock
  protected UserACL                 userAcl;

  @Mock
  private SpaceTemplateStorage      spaceTemplateStorage;

  @Mock
  private Identity                  userIdentity;

  @Mock
  private PortalConfig              portalConfig;

  private SpaceTemplateService      spaceTemplateService;

  @Before
  public void init() {
    spaceTemplateService = new SpaceTemplateService(translationService,
                                                    attachmentService,
                                                    userPortalConfigService,
                                                    layoutService,
                                                    navigationService,
                                                    listenerService,
                                                    userAcl,
                                                    spaceTemplateStorage);
  }

  @Test
  public void testGetSpaceTemplates() {
    Pageable pageable = Pageable.unpaged();
    List<SpaceTemplate> spaceTemplates = spaceTemplateService.getSpaceTemplates(new SpaceTemplateFilter(), pageable, true);
    assertNotNull(spaceTemplates);
    assertEquals(0, spaceTemplates.size());

    SpaceTemplate spaceTemplate = newSpaceTemplate(2l);
    when(spaceTemplateStorage.getSpaceTemplates(pageable)).then(invocation -> List.of(spaceTemplate));
    spaceTemplates = spaceTemplateService.getSpaceTemplates(null, pageable, true);
    assertNotNull(spaceTemplates);
    assertEquals(1, spaceTemplates.size());
    assertEquals(spaceTemplate, spaceTemplates.get(0));

    SpaceTemplateFilter spaceTemplateFilter = new SpaceTemplateFilter();
    spaceTemplateFilter.setUsername(TEST_USER);
    spaceTemplateFilter.setIncludeDisabled(true);
    spaceTemplates = spaceTemplateService.getSpaceTemplates(spaceTemplateFilter, pageable, true);
    assertNotNull(spaceTemplates);
    assertEquals(0, spaceTemplates.size());

    setCanViewTemplate(true);
    when(spaceTemplateStorage.getSpaceTemplate(2l)).thenReturn(spaceTemplate);

    spaceTemplates = spaceTemplateService.getSpaceTemplates(spaceTemplateFilter, pageable, false);
    assertNotNull(spaceTemplates);
    assertEquals(1, spaceTemplates.size());
    assertEquals(spaceTemplate, spaceTemplates.get(0));
  }

  @Test
  public void testGetSpaceTemplate() throws IllegalAccessException {
    assertNull(spaceTemplateService.getSpaceTemplate(2l));
    assertNull(spaceTemplateService.getSpaceTemplate(2l, TEST_USER, Locale.ENGLISH, false));

    SpaceTemplate spaceTemplate = newSpaceTemplate(2l);
    when(spaceTemplateStorage.getSpaceTemplate(2l)).thenReturn(spaceTemplate);
    assertEquals(spaceTemplate, spaceTemplateService.getSpaceTemplate(2l));

    assertThrows(IllegalAccessException.class, () -> spaceTemplateService.getSpaceTemplate(2l, TEST_USER, Locale.ENGLISH, false));
    setCanViewTemplate(true);
    assertEquals(spaceTemplate, spaceTemplateService.getSpaceTemplate(2l, TEST_USER, Locale.ENGLISH, false));
  }

  @Test
  public void testCanManageTemplates() {
    assertFalse(spaceTemplateService.canManageTemplates(TEST_USER));
    setCanManageTemplate(true);
    assertTrue(spaceTemplateService.canManageTemplates(TEST_USER));
  }

  @Test
  public void testCanViewTemplateWhenManager() {
    assertFalse(spaceTemplateService.canViewTemplate(2l, null));
    assertFalse(spaceTemplateService.canViewTemplate(2l, TEST_USER));
    setCanManageTemplate(true);
    assertFalse(spaceTemplateService.canViewTemplate(2l, TEST_USER));
    when(spaceTemplateStorage.getSpaceTemplate(2l)).thenReturn(newSpaceTemplate(2l));
    assertTrue(spaceTemplateService.canViewTemplate(2l, TEST_USER));
  }

  @Test
  public void testCanViewTemplateWhenAnonymous() {
    SpaceTemplate template = newSpaceTemplate(2l);
    template.setPermissions(List.of(UserACL.EVERYONE));
    lenient().when(userAcl.isAnonymousUser((String) null)).thenReturn(true);
    lenient().when(userAcl.isAnonymousUser((Identity) null)).thenReturn(true);
    when(spaceTemplateStorage.getSpaceTemplate(2l)).thenReturn(template);
    assertTrue(spaceTemplateService.canViewTemplate(2l, null));
  }

  @Test
  public void testCanViewTemplateWhenMemberOfPermissions() {
    assertFalse(spaceTemplateService.canViewTemplate(2l, TEST_USER));
    setCanViewTemplate(true);
    assertFalse(spaceTemplateService.canViewTemplate(2l, TEST_USER));
    when(spaceTemplateStorage.getSpaceTemplate(2l)).thenReturn(newSpaceTemplate(2l));
    assertTrue(spaceTemplateService.canViewTemplate(2l, TEST_USER));
  }

  @Test
  public void testCreateSpaceTemplate() throws IllegalAccessException, ObjectNotFoundException {
    assertThrows(IllegalAccessException.class, () -> spaceTemplateService.createSpaceTemplate(newSpaceTemplate(0l), TEST_USER));
    setCanManageTemplate(true);
    assertThrows(IllegalArgumentException.class, () -> spaceTemplateService.createSpaceTemplate(newSpaceTemplate(2l), TEST_USER));
    SpaceTemplate spaceTemplate = newSpaceTemplate(0l);
    assertThrows(ObjectNotFoundException.class, () -> spaceTemplateService.createSpaceTemplate(spaceTemplate, TEST_USER));

    doAnswer(invocation -> {
      SpaceTemplate spaceTemplateClone = spaceTemplate.clone();
      spaceTemplateClone.setId(2l);
      return spaceTemplateClone;
    }).when(spaceTemplateStorage).createSpaceTemplate(any());
    when(layoutService.getPortalConfig(SiteKey.groupTemplate(spaceTemplate.getLayout()))).thenReturn(portalConfig);
    when(layoutService.getPortalConfig(SiteKey.groupTemplate("2"))).thenReturn(portalConfig);
    spaceTemplateService.createSpaceTemplate(spaceTemplate, TEST_USER);

    SpaceTemplate spaceTemplateClone = spaceTemplate.clone();
    spaceTemplateClone.setLayout(null);
    spaceTemplateClone.setSystem(false);
    spaceTemplateClone.setDeleted(false);
    verify(spaceTemplateStorage).createSpaceTemplate(spaceTemplateClone);
    verify(spaceTemplateStorage).updateSpaceTemplate(argThat(template -> StringUtils.equals(template.getLayout(), "2")));
    verify(userPortalConfigService).createSiteFromTemplate(SiteKey.groupTemplate(spaceTemplate.getLayout()),
                                                           SiteKey.groupTemplate("2"));
  }

  @Test
  public void testUpdateSpaceTemplate() throws ObjectNotFoundException, IllegalAccessException {
    SpaceTemplate spaceTemplate = newSpaceTemplate(2l);
    assertThrows(IllegalAccessException.class, () -> spaceTemplateService.updateSpaceTemplate(spaceTemplate, TEST_USER));
    setCanManageTemplate(true);
    spaceTemplate.setDeleted(true);
    assertThrows(IllegalArgumentException.class, () -> spaceTemplateService.updateSpaceTemplate(spaceTemplate, TEST_USER));

    SpaceTemplate savedSpaceTemplate = newSpaceTemplate(2l);
    when(spaceTemplateStorage.getSpaceTemplate(2l)).thenReturn(savedSpaceTemplate);
    savedSpaceTemplate.setDeleted(true);
    spaceTemplate.setDeleted(false);

    assertThrows(ObjectNotFoundException.class, () -> spaceTemplateService.updateSpaceTemplate(spaceTemplate, TEST_USER));

    savedSpaceTemplate.setDeleted(false);
    spaceTemplateService.updateSpaceTemplate(spaceTemplate, TEST_USER);
    verify(spaceTemplateStorage).updateSpaceTemplate(spaceTemplate);
  }

  @Test
  public void testDeleteSpaceTemplate() throws IllegalAccessException, ObjectNotFoundException {
    assertThrows(IllegalAccessException.class, () -> spaceTemplateService.deleteSpaceTemplate(2l, TEST_USER));
    setCanManageTemplate(true);
    assertThrows(ObjectNotFoundException.class, () -> spaceTemplateService.deleteSpaceTemplate(2l, TEST_USER));

    SpaceTemplate savedSpaceTemplate = newSpaceTemplate(2l);
    when(spaceTemplateStorage.getSpaceTemplate(2l)).thenReturn(savedSpaceTemplate);
    savedSpaceTemplate.setSystem(false);
    savedSpaceTemplate.setDeleted(true);
    assertThrows(ObjectNotFoundException.class, () -> spaceTemplateService.deleteSpaceTemplate(2l, TEST_USER));

    savedSpaceTemplate.setDeleted(false);
    savedSpaceTemplate.setSystem(true);
    assertThrows(IllegalAccessException.class, () -> spaceTemplateService.deleteSpaceTemplate(2l, TEST_USER));
    savedSpaceTemplate.setSystem(false);

    spaceTemplateService.deleteSpaceTemplate(2l, TEST_USER);
    verify(spaceTemplateStorage).updateSpaceTemplate(savedSpaceTemplate);
    assertTrue(savedSpaceTemplate.isDeleted());
  }

  @Test
  public void testGetAllowedSubspaceTemplates() throws Exception {
    // parent template not found
    when(spaceTemplateStorage.getSpaceTemplate(1L)).thenReturn(null);
    
    assertThrows(ObjectNotFoundException.class,
            () -> spaceTemplateService.getAllowedSubspaceTemplates(1L, "user", null));
    // parent template deleted
    SpaceTemplate spaceTemplate = Mockito.mock(SpaceTemplate.class);
    when(spaceTemplate.isDeleted()).thenReturn(true);
    when(spaceTemplateStorage.getSpaceTemplate(anyLong())).thenReturn(null).thenReturn(spaceTemplate);

    assertThrows(ObjectNotFoundException.class,
            () -> spaceTemplateService.getAllowedSubspaceTemplates(1L, "user", null));

    // parent template has no allowed subspace template
    when(spaceTemplate.isDeleted()).thenReturn(false);
    when(spaceTemplate.getAllowedSubspaceTemplates()).thenReturn(Collections.emptyList());

    List<SpaceTemplate> empty = spaceTemplateService.getAllowedSubspaceTemplates(1L, "user", null);
    assertTrue(empty.isEmpty());
    // subspace template not accessible for user
    when(spaceTemplate.getAllowedSubspaceTemplates()).thenReturn(List.of("10:5"));
    when(userAcl.isAnonymousUser(anyString())).thenReturn(false);
    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(anyString())).thenReturn(identity);
    SpaceTemplate subspaceTemplate = mock(SpaceTemplate.class);
    when(subspaceTemplate.isEnabled()).thenReturn(true);
    when(spaceTemplateStorage.getSpaceTemplate(10L)).thenReturn(subspaceTemplate);
    try(MockedStatic<CommonsUtils> mockedUtils = mockStatic(CommonsUtils.class)) {
      when(subspaceTemplate.getPermissions()).thenReturn(List.of("*:/platform/users"));
      when(identity.isMemberOf(any(MembershipEntry.class))).thenReturn(true);
      List<SpaceTemplate> result = spaceTemplateService.getAllowedSubspaceTemplates(1L, "user", null);
      assertTrue(CollectionUtils.isNotEmpty(result));
    }
  }

  private void setCanViewTemplate(boolean hasAccess) {
    when(userAcl.getUserIdentity(TEST_USER)).thenReturn(userIdentity);
    when(userIdentity.isMemberOf(new MembershipEntry(CREATE_AND_ACCESS_PERMISSIONS))).thenReturn(hasAccess);
  }

  private void setCanManageTemplate(boolean hasAccess) {
    Identity identity = mock(Identity.class);
    when(userAcl.getUserIdentity(TEST_USER)).thenReturn(identity);
    when(userAcl.isAdministrator(identity)).thenReturn(hasAccess);
  }

  private SpaceTemplate newSpaceTemplate(long id) {
    return new SpaceTemplate(id,
                             "name",
                             "description",
                             6l,
                             "icon",
                             true,
                             false,
                             true,
                             "layout",
                             Arrays.asList(CREATE_AND_ACCESS_PERMISSIONS),
                             Arrays.asList(ADMIN_PERMISSIONS),
                             Arrays.asList(SPACE_LAYOUT_PERMISSIONS),
                             Arrays.asList(SPACE_DELETE_PERMISSIONS),
                             Arrays.asList(SPACE_PUBLIC_SITE_PERMISSIONS),
                             Arrays.asList(SPACE_FIELDS),
                             Arrays.asList(SPACE_CATEGORY_ID),
                             SpaceVisibility.PRIVATE,
                             SpaceRegistration.VALIDATION,
                             true,
                             null,
                             0);
  }

}
