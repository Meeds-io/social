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
package io.meeds.social.space.administration.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;
import io.meeds.social.space.model.SpacePermissions;
import io.meeds.social.space.model.SpaceTemplatePatch;
import io.meeds.social.space.service.SpaceAdministrationServiceImpl;
import io.meeds.social.space.service.SpaceLayoutService;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

@RunWith(MockitoJUnitRunner.class)
public class SpaceAdministrationServiceTest {

  private static final String    TESTUSER_REDACTOR = "testuserRedactor";

  private static final String    TESTUSER_MANAGER  = "testuserManager";

  long                           spaceId           = 2l;

  long                           templateId        = 3l;

  @Mock
  SpaceService                   spaceService;

  @Mock
  SpaceLayoutService             spaceLayoutService;

  @Mock
  SpaceTemplateService           spaceTemplateService;

  @Mock
  Space                          space;

  @Mock
  SpaceTemplate                  spaceTemplate;

  @Mock
  SpaceTemplatePatch             templatePatch;

  SpaceAdministrationServiceImpl spaceAdministrationService;

  @Before
  public void setup() {
    spaceAdministrationService = new SpaceAdministrationServiceImpl();
    spaceAdministrationService.setSpaceService(spaceService);
    spaceAdministrationService.setSpaceLayoutService(spaceLayoutService);
    spaceAdministrationService.setSpaceTemplateService(spaceTemplateService);
  }

  @Test
  public void testGetSpacePermissions() throws ObjectNotFoundException {
    assertThrows(ObjectNotFoundException.class, () -> spaceAdministrationService.getSpacePermissions(spaceId));
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    when(space.getLayoutPermissions()).thenReturn(Collections.singletonList("layoutPermissions"));
    when(space.getDeletePermissions()).thenReturn(Collections.singletonList("deletePermissions"));
    when(space.getPublicSitePermissions()).thenReturn(Collections.singletonList("publicSitePermissionsPermissions"));
    SpacePermissions spacePermissions = spaceAdministrationService.getSpacePermissions(spaceId);
    assertNotNull(spacePermissions);
    assertEquals(space.getDeletePermissions(), spacePermissions.getDeletePermissions());
    assertEquals(space.getLayoutPermissions(), spacePermissions.getLayoutPermissions());
    assertEquals(space.getPublicSitePermissions(), spacePermissions.getPublicSitePermissions());
  }

  @Test
  public void testGetSpaceExtendedProperties() throws ObjectNotFoundException {
    assertThrows(ObjectNotFoundException.class, () -> spaceAdministrationService.getSpaceExtendedProperties(spaceId));

    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    Map<String, String> extendedProperties = Collections.singletonMap("key", "value");
    when(space.getExtendedProperties()).thenReturn(extendedProperties);

    assertEquals(extendedProperties, spaceAdministrationService.getSpaceExtendedProperties(spaceId));
  }

  @Test
  public void testUpdateSpacePermissions() throws ObjectNotFoundException {
    SpacePermissions spacePermissions = mock(SpacePermissions.class);
    when(spacePermissions.getLayoutPermissions()).thenReturn(Collections.singletonList("layoutPermissions"));
    when(spacePermissions.getDeletePermissions()).thenReturn(Collections.singletonList("deletePermissions"));
    when(spacePermissions.getPublicSitePermissions()).thenReturn(Collections.singletonList("publicSitePermissionsPermissions"));

    assertThrows(ObjectNotFoundException.class,
                 () -> spaceAdministrationService.updateSpacePermissions(spaceId, spacePermissions));
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    spaceAdministrationService.updateSpacePermissions(spaceId, spacePermissions);
    verify(space).setDeletePermissions(spacePermissions.getDeletePermissions());
    verify(space).setLayoutPermissions(spacePermissions.getLayoutPermissions());
    verify(space).setPublicSitePermissions(spacePermissions.getPublicSitePermissions());
    verify(spaceService).updateSpace(space);
  }

  @Test
  public void testUpdateSpaceExtendedPropertyWhenSpaceNotFound() {
    assertThrows(ObjectNotFoundException.class,
                 () -> spaceAdministrationService.updateSpaceExtendedProperty(spaceId, "key", "value"));
  }

  @Test
  public void testUpdateSpaceExtendedPropertyWhenValueBlankRemovesProperty() throws ObjectNotFoundException {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    Map<String, String> existingProperties = new HashMap<>();
    existingProperties.put("key", "value");
    when(space.getExtendedProperties()).thenReturn(existingProperties);

    spaceAdministrationService.updateSpaceExtendedProperty(spaceId, "key", " ");

    verify(space).setExtendedProperties(Collections.emptyMap());
    verify(spaceService).updateSpace(space);
  }

  @Test
  public void testUpdateSpaceExtendedPropertyWhenValueNotBlankAddsProperty() throws ObjectNotFoundException {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    when(space.getExtendedProperties()).thenReturn(null);

    spaceAdministrationService.updateSpaceExtendedProperty(spaceId, "key", "value");

    verify(space).setExtendedProperties(Collections.singletonMap("key", "value"));
    verify(spaceService).updateSpace(space);
  }

  @Test
  public void testUpdateSpaceExtendedPropertiesWhenSpaceNotFound() {
    assertThrows(ObjectNotFoundException.class,
                 () -> spaceAdministrationService.updateSpaceExtendedProperties(spaceId, Collections.singletonMap("key", "value")));
  }

  @Test
  public void testUpdateSpaceExtendedPropertiesWhenEmptyDoesNothing() throws ObjectNotFoundException {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);

    spaceAdministrationService.updateSpaceExtendedProperties(spaceId, null);
    spaceAdministrationService.updateSpaceExtendedProperties(spaceId, Collections.emptyMap());

    verify(space, never()).setExtendedProperties(any());
    verify(spaceService, never()).updateSpace(any());
  }

  @Test
  public void testUpdateSpaceExtendedPropertiesMergesWithExisting() throws ObjectNotFoundException {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    Map<String, String> existingProperties = new HashMap<>();
    existingProperties.put("existingKey", "existingValue");
    when(space.getExtendedProperties()).thenReturn(existingProperties);

    Map<String, String> newProperties = Collections.singletonMap("newKey", "newValue");
    spaceAdministrationService.updateSpaceExtendedProperties(spaceId, newProperties);

    Map<String, String> expected = new HashMap<>();
    expected.put("existingKey", "existingValue");
    expected.put("newKey", "newValue");
    verify(space).setExtendedProperties(expected);
    verify(spaceService).updateSpace(space);
  }

  @Test
  public void testApplySpaceTemplate() throws ObjectNotFoundException {
    when(spaceTemplateService.getSpaceTemplate(anyLong())).thenReturn(spaceTemplate);
    when(spaceTemplate.isDeleted()).thenReturn(false);
    when(spaceTemplate.isEnabled()).thenReturn(true);
    when(spaceTemplate.isSpaceAllowContentCreation()).thenReturn(true);
    when(spaceTemplate.getSpaceDefaultRegistration()).thenReturn(SpaceRegistration.OPEN);
    when(spaceTemplate.getSpaceDefaultVisibility()).thenReturn(SpaceVisibility.HIDDEN);

    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    when(space.getManagers()).thenReturn(new String[] { TESTUSER_MANAGER });

    when(templatePatch.getTemplateId()).thenReturn(templateId);
    when(templatePatch.isAccessRules()).thenReturn(true);
    when(templatePatch.isDeletePermissions()).thenReturn(true);
    when(templatePatch.isLayoutPermissions()).thenReturn(true);
    when(templatePatch.isPublicSitePermissions()).thenReturn(true);
    when(templatePatch.isEditorialMode()).thenReturn(true);
    when(templatePatch.isUpdateSite()).thenReturn(true);

    spaceAdministrationService.applySpaceTemplate(spaceId, templatePatch);

    verify(spaceService).updateSpace(space);
    verify(spaceLayoutService).updateSpaceSite(space);
    verify(spaceService).addRedactor(any(), any());
    verify(space).setRegistration(spaceTemplate.getSpaceDefaultRegistration().name().toLowerCase());
    verify(space).setVisibility(spaceTemplate.getSpaceDefaultVisibility().name().toLowerCase());
  }

  @Test
  public void testApplySpaceTemplateWhenSpaceNotFound() {
    when(spaceService.getSpaceById(spaceId)).thenReturn(null);
    assertThrows(ObjectNotFoundException.class, () -> spaceAdministrationService.applySpaceTemplate(spaceId, templatePatch));
  }

  @Test
  public void testApplySpaceTemplateWhenSpaceTemplateNotFound() {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    when(templatePatch.getTemplateId()).thenReturn(templateId);
    when(spaceTemplateService.getSpaceTemplate(templateId)).thenReturn(null);

    assertThrows(ObjectNotFoundException.class, () -> spaceAdministrationService.applySpaceTemplate(spaceId, templatePatch));
  }

  @Test
  public void testApplySpaceTemplateWhenSpaceTemplateIsDeleted() {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    when(templatePatch.getTemplateId()).thenReturn(templateId);
    when(spaceTemplateService.getSpaceTemplate(templateId)).thenReturn(spaceTemplate);
    when(spaceTemplate.isDeleted()).thenReturn(true);

    assertThrows(ObjectNotFoundException.class, () -> spaceAdministrationService.applySpaceTemplate(spaceId, templatePatch));
  }

  @Test
  public void testApplySpaceTemplateWhenSpaceTemplateIsDisabled() {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    when(templatePatch.getTemplateId()).thenReturn(templateId);
    when(spaceTemplateService.getSpaceTemplate(templateId)).thenReturn(spaceTemplate);
    when(spaceTemplate.isEnabled()).thenReturn(false);

    assertThrows(ObjectNotFoundException.class, () -> spaceAdministrationService.applySpaceTemplate(spaceId, templatePatch));
  }

  @Test
  public void testApplySpaceTemplateWhenEditorialModeAndAllowContentCreation() throws ObjectNotFoundException {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    when(space.getRedactors()).thenReturn(new String[0]);
    when(space.getManagers()).thenReturn(new String[] { TESTUSER_MANAGER });

    when(spaceTemplateService.getSpaceTemplate(templateId)).thenReturn(spaceTemplate);
    when(spaceTemplate.isSpaceAllowContentCreation()).thenReturn(true);
    when(spaceTemplate.isDeleted()).thenReturn(false);
    when(spaceTemplate.isEnabled()).thenReturn(true);

    when(templatePatch.isEditorialMode()).thenReturn(true);
    when(templatePatch.getTemplateId()).thenReturn(templateId);

    spaceAdministrationService.applySpaceTemplate(spaceId, templatePatch);

    verify(spaceService).addRedactor(any(), any());
  }

  @Test
  public void testApplySpaceTemplateWhenEditorialModeAndNotAllowContentCreation() throws ObjectNotFoundException {
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    when(space.getRedactors()).thenReturn(new String[] { TESTUSER_REDACTOR });

    when(spaceTemplateService.getSpaceTemplate(anyLong())).thenReturn(spaceTemplate);
    when(spaceTemplate.isSpaceAllowContentCreation()).thenReturn(false);
    when(spaceTemplate.isDeleted()).thenReturn(false);
    when(spaceTemplate.isEnabled()).thenReturn(true);

    when(templatePatch.isEditorialMode()).thenReturn(true);
    when(templatePatch.getTemplateId()).thenReturn(templateId);

    spaceAdministrationService.applySpaceTemplate(spaceId, templatePatch);

    verify(spaceService).removeRedactor(any(), any());
  }

}
