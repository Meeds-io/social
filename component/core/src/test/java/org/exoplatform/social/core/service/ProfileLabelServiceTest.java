/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.core.service;

import static org.junit.Assert.assertThrows;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfileLabelDAO;
import org.exoplatform.social.core.model.ProfileLabel;
import org.exoplatform.social.core.profilelabel.ProfileLabelService;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class ProfileLabelServiceTest extends AbstractCoreTest {

  private ProfileLabelService profileLabelService;

  private ProfileLabelDAO     labelDAO;

  private final String        objectId   = "1";

  private final String        objectType = "testObject";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    profileLabelService = getContainer().getComponentInstanceOfType(ProfileLabelService.class);
    labelDAO = getContainer().getComponentInstanceOfType(ProfileLabelDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    restartTransaction();
    labelDAO.deleteAll();
    super.tearDown();
  }

  public void testCreateLabel() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> this.profileLabelService.createLabel(null));
    assertEquals("ProfileLabel Item Object is mandatory", exception.getMessage());
    exception = assertThrows(IllegalArgumentException.class, () -> this.profileLabelService.createLabel(new ProfileLabel()));
    assertEquals("Object type is mandatory", exception.getMessage());
    ProfileLabel profileLabel = new ProfileLabel();
    profileLabel.setObjectType(objectType);
    ProfileLabel finalProfileLabel = profileLabel;
    exception = assertThrows(IllegalArgumentException.class, () -> this.profileLabelService.createLabel(finalProfileLabel));
    assertEquals("Object Id is mandatory", exception.getMessage());
    exception = assertThrows(IllegalArgumentException.class,
                             () -> this.profileLabelService.createLabel(createLabel("profileLabel", "")));
    assertEquals("language is mandatory", exception.getMessage());
    exception = assertThrows(IllegalArgumentException.class, () -> this.profileLabelService.createLabel(createLabel("", "fr")));
    assertEquals("profileLabel value is mandatory", exception.getMessage());
    try {
      profileLabel = createLabel("test profileLabel fr", "fr");
      profileLabelService.createLabel(profileLabel);
      profileLabel = profileLabelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertNotNull(profileLabel.getId());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreateDuplicatedLabel() {

    try {
      profileLabelService.createLabel(createLabel("test label fr", "fr"));
      Throwable exception1 = assertThrows(ObjectAlreadyExistsException.class,
                                          () -> this.profileLabelService.createLabel(createLabel("test label fr", "fr")));
      assertEquals("ProfileLabel already exist", exception1.getMessage());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testDeleteLabel() {
    Throwable exception1 = assertThrows(IllegalArgumentException.class, () -> this.profileLabelService.deleteLabel(0L));
    assertEquals("ProfileLabel Technical Identifier is mandatory", exception1.getMessage());
    try {
      profileLabelService.createLabel(createLabel("test profileLabel fr", "fr"));
      ProfileLabel profileLabel = profileLabelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertNotNull(profileLabel.getId());
      profileLabelService.deleteLabel(profileLabel.getId());
      profileLabel = profileLabelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertNull(profileLabel);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testUpdateLabel() {
    try {
      profileLabelService.createLabel(createLabel("test profileLabel fr", "fr"));
      ProfileLabel profileLabel = profileLabelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertEquals("test profileLabel fr", profileLabel.getLabel());
      profileLabel.setLabel("test profileLabel fr 2");
      profileLabelService.updateLabel(profileLabel);
      profileLabel = profileLabelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertEquals("test profileLabel fr 2", profileLabel.getLabel());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testFindLabelByObjectTypeAndObjectId() {
    try {
      assertEquals(0, profileLabelService.findLabelByObjectTypeAndObjectId(objectType, objectId).size());
      profileLabelService.createLabel(createLabel("test label fr", "fr"));
      profileLabelService.createLabel(createLabel("test label en", "en"));
      profileLabelService.createLabel(createLabel("test label it", "it"));
      profileLabelService.createLabel(createLabel("test label de", "de"));
      assertEquals(4, profileLabelService.findLabelByObjectTypeAndObjectId(objectType, objectId).size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private ProfileLabel createLabel(String labelValue, String lang) {
    ProfileLabel profileLabel = new ProfileLabel();
    profileLabel.setLanguage(lang);
    profileLabel.setObjectType(objectType);
    profileLabel.setObjectId(objectId);
    profileLabel.setLabel(labelValue);
    return profileLabel;
  }

}
