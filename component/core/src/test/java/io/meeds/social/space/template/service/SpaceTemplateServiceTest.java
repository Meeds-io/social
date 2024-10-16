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
package io.meeds.social.space.template.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import io.meeds.social.space.constant.Registration;
import io.meeds.social.space.constant.Visibility;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.model.SpaceTemplateFilter;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;
import io.meeds.social.translation.service.TranslationService;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateServiceTest {

  private static final String           SPACE_FIELDS                  = "spaceFields";

  private static final String           SPACE_DELETE_PERMISSIONS      = "spaceDeletePermissions";

  private static final String           SPACE_LAYOUT_PERMISSIONS      = "spaceLayoutPermissions";

  private static final String           CREATE_AND_ACCESS_PERMISSIONS = "permissions";

  private static final String           TEST_USER                     = "testuser";

  @Mock
  protected TranslationService          translationService;

  @Mock
  protected AttachmentService           attachmentService;

  @Mock
  protected SpacesAdministrationService spacesAdministrationService;

  @Mock
  protected UserACL                     userAcl;

  @Mock
  private SpaceTemplateStorage          spaceTemplateStorage;

  @Mock
  private Identity                      userIdentity;

  private SpaceTemplateService          spaceTemplateService;

  @Before
  public void init() {
    spaceTemplateService = new SpaceTemplateService();
    spaceTemplateService.translationService = translationService;
    spaceTemplateService.attachmentService = attachmentService;
    spaceTemplateService.spacesAdministrationService = spacesAdministrationService;
    spaceTemplateService.userAcl = userAcl;
    spaceTemplateService.spaceTemplateStorage = spaceTemplateStorage;
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
    assertTrue(spaceTemplateService.canViewTemplate(2l, TEST_USER));
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
  public void testCreateSpaceTemplate() throws IllegalAccessException {
    assertThrows(IllegalAccessException.class, () -> spaceTemplateService.createSpaceTemplate(newSpaceTemplate(0l), TEST_USER));
    setCanManageTemplate(true);
    assertThrows(IllegalArgumentException.class, () -> spaceTemplateService.createSpaceTemplate(newSpaceTemplate(2l), TEST_USER));
    SpaceTemplate spaceTemplate = newSpaceTemplate(0l);
    spaceTemplateService.createSpaceTemplate(spaceTemplate, TEST_USER);
    verify(spaceTemplateStorage).createSpaceTemplate(spaceTemplate);
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

  private void setCanViewTemplate(boolean hasAccess) {
    when(userAcl.getUserIdentity(TEST_USER)).thenReturn(userIdentity);
    when(userIdentity.isMemberOf(new MembershipEntry(CREATE_AND_ACCESS_PERMISSIONS))).thenReturn(hasAccess);
  }

  private void setCanManageTemplate(boolean hasAccess) {
    when(spacesAdministrationService.isSuperManager(TEST_USER)).thenReturn(hasAccess);
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
                             5l,
                             Arrays.asList(CREATE_AND_ACCESS_PERMISSIONS),
                             Arrays.asList(SPACE_LAYOUT_PERMISSIONS),
                             Arrays.asList(SPACE_DELETE_PERMISSIONS),
                             Arrays.asList(SPACE_FIELDS),
                             Visibility.PRIVATE,
                             Registration.VALIDATION,
                             true);
  }

}
