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
package io.meeds.social.space.template.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.core.space.constant.Registration;
import io.meeds.social.core.space.constant.Visibility;
import io.meeds.social.space.template.dao.SpaceTemplateDAO;
import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.model.SpaceTemplate;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateStorageTest {

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
    assertEquals(spaceTemplateEntity.getPermissions(), spaceTemplate.getPermissions());
    assertEquals(spaceTemplateEntity.getIcon(), spaceTemplate.getIcon());
    assertEquals(spaceTemplateEntity.isEnabled(), spaceTemplate.isEnabled());
    assertEquals(spaceTemplateEntity.isDeleted(), spaceTemplate.isDeleted());
    assertEquals(spaceTemplateEntity.isSystem(), spaceTemplate.isSystem());
    assertEquals(spaceTemplateEntity.getSpaceDeletePermissions(), spaceTemplate.getSpaceDeletePermissions());
    assertEquals(spaceTemplateEntity.getSpaceLayoutPermissions(), spaceTemplate.getSpaceLayoutPermissions());
    assertEquals(spaceTemplateEntity.getSpaceFields(), spaceTemplate.getSpaceFields());
    assertEquals(spaceTemplateEntity.getSpaceDefaultVisibility(), spaceTemplate.getSpaceDefaultVisibility());
    assertEquals(spaceTemplateEntity.getSpaceDefaultRegistration(), spaceTemplate.getSpaceDefaultRegistration());
    assertEquals(spaceTemplateEntity.isSpaceAllowContentCreation(), spaceTemplate.isSpaceAllowContentCreation());
  }

  private SpaceTemplateEntity newSpaceTemplateEntity() {
    return new SpaceTemplateEntity(2l,
                                   "icon",
                                   true,
                                   false,
                                   true,
                                   "layout",
                                   Arrays.asList("permissions"),
                                   Arrays.asList("spaceLayoutPermissions"),
                                   Arrays.asList("spaceDeletePermissions"),
                                   Arrays.asList("spaceFields"),
                                   Visibility.PRIVATE,
                                   Registration.VALIDATION,
                                   true);
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
                             Arrays.asList("permissions"),
                             Arrays.asList("spaceLayoutPermissions"),
                             Arrays.asList("spaceDeletePermissions"),
                             Arrays.asList("spaceFields"),
                             Visibility.PRIVATE,
                             Registration.VALIDATION,
                             true);
  }

}
