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
package io.meeds.social.space.template.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;
import io.meeds.social.space.template.dao.SpaceTemplateDAO;
import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.model.SpaceTemplate;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateStorageTest {

  private static final String  SPACE_FIELDS                  = "spaceFields";

  private static final String  SPACE_CREATE_PERMISSIONS      = "permissions";

  private static final String  SPACE_ADMIN_PERMISSIONS       = "adminPermissions";

  private static final String  SPACE_LAYOUT_PERMISSIONS      = "spaceLayoutPermissions";

  private static final String  SPACE_DELETE_PERMISSIONS      = "spaceDeletePermissions";

  private static final String  SPACE_PUBLIC_SITE_PERMISSIONS = "spacePublicSitePermissions";

  private static final long    SPACE_CATEGORY_ID             = 2l;

  @Mock
  private SpaceTemplateDAO     spaceTemplateDAO;

  private SpaceTemplateStorage spaceTemplateStorage;

  @Before
  public void init() {
    spaceTemplateStorage = new SpaceTemplateStorage(spaceTemplateDAO);
  }

  @Test
  public void testGetSpaceTemplates() {
    SpaceTemplateEntity spaceTemplateEntity = newSpaceTemplateEntity();
    when(spaceTemplateDAO.findByDeletedFalse(any())).thenAnswer(invocation -> List.of(spaceTemplateEntity));
    List<SpaceTemplate> spaceTemplates = spaceTemplateStorage.getSpaceTemplates(Pageable.unpaged());
    assertNotNull(spaceTemplates);
    assertEquals(1l, spaceTemplates.size());

    spaceTemplates = spaceTemplateStorage.getSpaceTemplates(Pageable.unpaged());
    SpaceTemplate spaceTemplate = spaceTemplates.get(0);
    checkEntityEqualsModel(spaceTemplateEntity, spaceTemplate);
  }

  @Test
  public void testGetEnabledSpaceTemplates() {
    SpaceTemplateEntity spaceTemplateEntity = newSpaceTemplateEntity();
    when(spaceTemplateDAO.findByDeletedFalseAndEnabledTrue(any())).thenAnswer(invocation -> List.of(spaceTemplateEntity));
    List<SpaceTemplate> spaceTemplates = spaceTemplateStorage.getEnabledSpaceTemplates(Pageable.unpaged());
    assertNotNull(spaceTemplates);
    assertEquals(1l, spaceTemplates.size());

    spaceTemplates = spaceTemplateStorage.getEnabledSpaceTemplates(Pageable.unpaged());
    SpaceTemplate spaceTemplate = spaceTemplates.get(0);
    checkEntityEqualsModel(spaceTemplateEntity, spaceTemplate);
  }

  @Test
  public void testGetSpaceTemplate() {
    assertNull(spaceTemplateStorage.getSpaceTemplate(3l));

    SpaceTemplateEntity spaceTemplateEntity = newSpaceTemplateEntity();
    when(spaceTemplateDAO.findById(3l)).thenAnswer(invocation -> Optional.of(spaceTemplateEntity));
    SpaceTemplate spaceTemplate = spaceTemplateStorage.getSpaceTemplate(3l);
    assertNotNull(spaceTemplate);
    checkEntityEqualsModel(spaceTemplateEntity, spaceTemplate);
  }

  @Test
  public void testCreateSpaceTemplate() {
    SpaceTemplate spaceTemplate = newSpaceTemplate();
    when(spaceTemplateDAO.save(any())).thenReturn(newSpaceTemplateEntity());
    SpaceTemplate createdSpaceTemplate = spaceTemplateStorage.createSpaceTemplate(spaceTemplate);
    assertNotNull(createdSpaceTemplate);
    spaceTemplate.setName(null);
    spaceTemplate.setDescription(null);
    spaceTemplate.setBannerFileId(0);
    assertEquals(spaceTemplate, createdSpaceTemplate);
  }

  @Test
  public void testUpdateSpaceTemplate() throws ObjectNotFoundException {
    SpaceTemplate spaceTemplate = newSpaceTemplate();
    assertThrows(ObjectNotFoundException.class, () -> spaceTemplateStorage.updateSpaceTemplate(spaceTemplate));
    when(spaceTemplateDAO.existsById(spaceTemplate.getId())).thenReturn(true);
    when(spaceTemplateDAO.save(any())).thenReturn(newSpaceTemplateEntity());
    SpaceTemplate updatedSpaceTemplate = spaceTemplateStorage.updateSpaceTemplate(spaceTemplate);
    assertNotNull(updatedSpaceTemplate);
    spaceTemplate.setName(null);
    spaceTemplate.setDescription(null);
    spaceTemplate.setBannerFileId(0);
    assertEquals(spaceTemplate, updatedSpaceTemplate);
  }

  @Test
  public void testDeleteSpaceTemplate() {
    spaceTemplateStorage.deleteSpaceTemplate(2l);
    verify(spaceTemplateDAO).deleteById(2l);
  }

  private void checkEntityEqualsModel(SpaceTemplateEntity spaceTemplateEntity, SpaceTemplate spaceTemplate) {
    assertNotNull(spaceTemplate);
    assertEquals(spaceTemplateEntity.getId().longValue(), spaceTemplate.getId());
    assertEquals(spaceTemplateEntity.getIcon(), spaceTemplate.getIcon());
    assertEquals(spaceTemplateEntity.getIcon(), spaceTemplate.getIcon());
    assertEquals(spaceTemplateEntity.isEnabled(), spaceTemplate.isEnabled());
    assertEquals(spaceTemplateEntity.isDeleted(), spaceTemplate.isDeleted());
    assertEquals(spaceTemplateEntity.isSystem(), spaceTemplate.isSystem());
    assertEquals(spaceTemplateEntity.getPermissions(), spaceTemplate.getPermissions());
    assertEquals(SPACE_CREATE_PERMISSIONS, spaceTemplate.getPermissions().get(0));
    assertEquals(spaceTemplateEntity.getAdminPermissions(), spaceTemplate.getAdminPermissions());
    assertEquals(SPACE_ADMIN_PERMISSIONS, spaceTemplate.getAdminPermissions().get(0));
    assertEquals(spaceTemplateEntity.getSpaceDeletePermissions(), spaceTemplate.getSpaceDeletePermissions());
    assertEquals(SPACE_DELETE_PERMISSIONS, spaceTemplate.getSpaceDeletePermissions().get(0));
    assertEquals(spaceTemplateEntity.getSpaceLayoutPermissions(), spaceTemplate.getSpaceLayoutPermissions());
    assertEquals(SPACE_LAYOUT_PERMISSIONS, spaceTemplate.getSpaceLayoutPermissions().get(0));
    assertEquals(spaceTemplateEntity.getSpacePublicSitePermissions(), spaceTemplate.getSpacePublicSitePermissions());
    assertEquals(SPACE_PUBLIC_SITE_PERMISSIONS, spaceTemplate.getSpacePublicSitePermissions().get(0));
    assertEquals(spaceTemplateEntity.getSpaceFields(), spaceTemplate.getSpaceFields());
    assertEquals(spaceTemplateEntity.getSpaceDefaultVisibility(), spaceTemplate.getSpaceDefaultVisibility());
    assertEquals(spaceTemplateEntity.getSpaceDefaultRegistration(), spaceTemplate.getSpaceDefaultRegistration());
    assertEquals(spaceTemplateEntity.isSpaceAllowContentCreation(), spaceTemplate.isSpaceAllowContentCreation());
    if (spaceTemplateEntity.getSpaceDefaultCategoryIds() == null) {
      assertTrue(CollectionUtils.isEmpty(spaceTemplate.getSpaceDefaultCategoryIds()));
    } else if (spaceTemplate.getSpaceDefaultCategoryIds() == null) {
      assertTrue(CollectionUtils.isEmpty(spaceTemplateEntity.getSpaceDefaultCategoryIds()));
    } else {
      assertEquals(spaceTemplateEntity.getSpaceDefaultCategoryIds(),
                   spaceTemplate.getSpaceDefaultCategoryIds().stream().map(String::valueOf).toList());
    }
  }

  private SpaceTemplateEntity newSpaceTemplateEntity() {
    return new SpaceTemplateEntity(2l,
                                   "icon",
                                   true,
                                   false,
                                   true,
                                   "layout",
                                   Arrays.asList(SPACE_CREATE_PERMISSIONS),
                                   Arrays.asList(SPACE_ADMIN_PERMISSIONS),
                                   Arrays.asList(SPACE_LAYOUT_PERMISSIONS),
                                   Arrays.asList(SPACE_DELETE_PERMISSIONS),
                                   Arrays.asList(SPACE_PUBLIC_SITE_PERMISSIONS),
                                   Arrays.asList(SPACE_FIELDS),
                                   Arrays.asList(String.valueOf(SPACE_CATEGORY_ID)),
                                   SpaceVisibility.PRIVATE,
                                   SpaceRegistration.VALIDATION,
                                   true,
                                   null,
                                   0);
  }

  private SpaceTemplate newSpaceTemplate() {
    return new SpaceTemplate(2l,
                             "name",
                             "description",
                             6l,
                             "icon",
                             true,
                             false,
                             true,
                             "layout",
                             Arrays.asList(SPACE_CREATE_PERMISSIONS),
                             Arrays.asList(SPACE_ADMIN_PERMISSIONS),
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
