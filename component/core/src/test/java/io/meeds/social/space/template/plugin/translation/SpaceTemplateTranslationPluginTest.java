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
package io.meeds.social.space.template.plugin.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.translation.service.TranslationService;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateTranslationPluginTest {

  private static final String            TEST_USER = "test";

  @Mock
  private SpaceTemplateService           spaceTemplateService;

  @Mock
  private TranslationService             translationService;

  @Mock
  private SpaceTemplate                  spaceTemplate;

  private SpaceTemplateTranslationPlugin translationPlugin;

  @Before
  public void init() {
    translationPlugin = new SpaceTemplateTranslationPlugin();
    translationPlugin.translationService = translationService;
    translationPlugin.spaceTemplateService = spaceTemplateService;
  }

  @Test
  @SneakyThrows
  public void testHasEditPermission() {
    assertFalse(translationPlugin.hasEditPermission(0, TEST_USER));
    when(spaceTemplateService.canManageTemplates(TEST_USER)).thenReturn(true);
    assertTrue(translationPlugin.hasEditPermission(0, TEST_USER));
  }

  @Test
  @SneakyThrows
  public void testHasAccessPermission() {
    assertThrows(ObjectNotFoundException.class, () -> translationPlugin.hasAccessPermission(2, TEST_USER));
    when(spaceTemplateService.canManageTemplates(TEST_USER)).thenReturn(true);
    assertTrue(translationPlugin.hasEditPermission(0, TEST_USER));

    when(spaceTemplateService.getSpaceTemplate(2)).thenReturn(spaceTemplate);
    assertFalse(translationPlugin.hasAccessPermission(2, TEST_USER));
    assertThrows(ObjectNotFoundException.class, () -> translationPlugin.hasAccessPermission(3, TEST_USER));

    when(spaceTemplateService.canViewTemplate(2, TEST_USER)).thenReturn(true);
    assertTrue(translationPlugin.hasAccessPermission(2, TEST_USER));
  }

  @Test
  public void getObjectType() {
    assertEquals(SpaceTemplateTranslationPlugin.OBJECT_TYPE, translationPlugin.getObjectType());
  }

  @Test
  public void getAudienceId() {
    assertEquals(0l, translationPlugin.getAudienceId(0));
  }

  @Test
  public void getSpaceId() {
    assertEquals(0l, translationPlugin.getSpaceId(0));
  }

}
