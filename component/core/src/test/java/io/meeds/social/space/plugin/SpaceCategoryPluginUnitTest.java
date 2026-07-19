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
package io.meeds.social.space.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.category.model.CategoryObject;

@SpringBootTest(classes = {
  SpaceCategoryPlugin.class,
})
@RunWith(SpringRunner.class)
public class SpaceCategoryPluginUnitTest {

  private static final long   SPACE_TECHNICAL_ID = 42l;

  private static final long   SPACE_IDENTITY_ID  = 74456l;

  private static final String SPACE_PRETTY_NAME  = "prettyName";

  @MockBean
  private SpaceService        spaceService;

  @MockBean
  private IdentityManager     identityManager;

  @Autowired
  private SpaceCategoryPlugin  spaceCategoryPlugin;

  @Test
  public void testGetObjectNormalizesTechnicalId() {
    Space space = newSpace();
    when(spaceService.getSpaceById(String.valueOf(SPACE_TECHNICAL_ID))).thenReturn(space);

    CategoryObject object = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, String.valueOf(SPACE_TECHNICAL_ID), 0l);
    CategoryObject normalized = spaceCategoryPlugin.getObject(object);

    assertEquals(String.valueOf(SPACE_TECHNICAL_ID), normalized.getId());
    assertEquals(SpaceCategoryPlugin.OBJECT_TYPE, normalized.getType());
  }

  @Test
  public void testGetObjectNormalizesIdentityIdToTechnicalId() {
    Space space = newSpace();
    // The identity id is not a space technical id
    when(spaceService.getSpaceById(String.valueOf(SPACE_IDENTITY_ID))).thenReturn(null);
    Identity identity = mock(Identity.class);
    when(identity.getProviderId()).thenReturn(SpaceIdentityProvider.NAME);
    when(identity.getRemoteId()).thenReturn(SPACE_PRETTY_NAME);
    when(identityManager.getIdentity(String.valueOf(SPACE_IDENTITY_ID))).thenReturn(identity);
    when(spaceService.getSpaceByPrettyName(SPACE_PRETTY_NAME)).thenReturn(space);

    CategoryObject object = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, String.valueOf(SPACE_IDENTITY_ID), 0l);
    CategoryObject normalized = spaceCategoryPlugin.getObject(object);

    assertEquals(String.valueOf(SPACE_TECHNICAL_ID), normalized.getId());
  }

  @Test
  public void testGetObjectNormalizesPrettyNameToTechnicalId() {
    Space space = newSpace();
    when(spaceService.getSpaceByPrettyName(SPACE_PRETTY_NAME)).thenReturn(space);

    CategoryObject object = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, SPACE_PRETTY_NAME, 0l);
    CategoryObject normalized = spaceCategoryPlugin.getObject(object);

    assertEquals(String.valueOf(SPACE_TECHNICAL_ID), normalized.getId());
  }

  @Test
  public void testGetObjectReturnsSameObjectWhenUnresolvable() {
    when(spaceService.getSpaceById("unknown")).thenReturn(null);
    when(spaceService.getSpaceByPrettyName("unknown")).thenReturn(null);

    CategoryObject object = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, "unknown", 0l);
    CategoryObject normalized = spaceCategoryPlugin.getObject(object);

    assertSame(object, normalized);
  }

  private Space newSpace() {
    Space space = new Space();
    space.setId(String.valueOf(SPACE_TECHNICAL_ID));
    space.setPrettyName(SPACE_PRETTY_NAME);
    return space;
  }

}
