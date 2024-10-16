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
package io.meeds.social.space.template.plugin.attachment;

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
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentService;

import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateBannerAttachmentPluginTest {

  @Mock
  private SpaceTemplateService                spaceTemplateService;

  @Mock
  private AttachmentService                   attachmentService;

  private SpaceTemplateBannerAttachmentPlugin attachmentPlugin;

  @Mock
  private Identity                            userIdentity;

  @Mock
  private SpaceTemplate                       spaceTemplate;

  @Before
  public void init() {
    attachmentPlugin = new SpaceTemplateBannerAttachmentPlugin();
    attachmentPlugin.attachmentService = attachmentService;
    attachmentPlugin.spaceTemplateService = spaceTemplateService;
  }

  @Test
  public void getObjectType() {
    assertEquals(SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE, attachmentPlugin.getObjectType());
  }

  @Test
  @SneakyThrows
  public void testHasEditPermission() {
    assertFalse(attachmentPlugin.hasEditPermission(null, null));
    assertFalse(attachmentPlugin.hasEditPermission(userIdentity, null));

    when(userIdentity.getUserId()).thenReturn("test");
    assertFalse(attachmentPlugin.hasEditPermission(userIdentity, null));

    when(spaceTemplateService.canManageTemplates("test")).thenReturn(true);
    assertTrue(attachmentPlugin.hasEditPermission(userIdentity, "2"));
  }

  @Test
  @SneakyThrows
  public void testHasAccessPermission() {
    assertFalse(attachmentPlugin.hasAccessPermission(null, null));
    assertFalse(attachmentPlugin.hasAccessPermission(userIdentity, null));
    assertFalse(attachmentPlugin.hasAccessPermission(null, "2"));

    when(userIdentity.getUserId()).thenReturn("test");
    assertThrows(ObjectNotFoundException.class, () -> attachmentPlugin.hasAccessPermission(userIdentity, "2"));

    when(spaceTemplateService.getSpaceTemplate(2)).thenReturn(spaceTemplate);
    assertFalse(attachmentPlugin.hasAccessPermission(userIdentity, "2"));

    when(spaceTemplateService.canViewTemplate(2, "test")).thenReturn(true);
    assertTrue(attachmentPlugin.hasAccessPermission(userIdentity, "2"));
  }

  @Test
  @SneakyThrows
  public void getAudienceId() {
    assertEquals(0l, attachmentPlugin.getAudienceId(null));
  }

  @Test
  @SneakyThrows
  public void getSpaceId() {
    assertEquals(0l, attachmentPlugin.getSpaceId(""));
  }

}
