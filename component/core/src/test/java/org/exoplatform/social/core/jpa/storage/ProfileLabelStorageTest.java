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
package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfileLabelDAO;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.model.ProfileLabel;
import org.exoplatform.social.core.profilelabel.storage.ProfileLabelStorage;

/**
 * Unit Tests for {@link ProfileLabelStorage}
 */
public class ProfileLabelStorageTest extends AbstractCoreTest {

  private final String       objectId   = "1";

  private final String       objectType = "testObject";

  private ProfileLabelStorage profileLabelStorage;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    profileLabelStorage = getContainer().getComponentInstanceOfType(ProfileLabelStorage.class);
    assertNotNull(profileLabelStorage);
  }

  public void testSaveLabel() {
    ProfileLabel profileLabel = createLabel("test profileLabel fr", "fr");
    profileLabelStorage.saveLabel(profileLabel, true);
    profileLabel = profileLabelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertNotNull(profileLabel.getId());
  }

  public void testDeleteLabel() {
    ProfileLabel profileLabel = createLabel("test profileLabel fr", "fr");
    profileLabelStorage.saveLabel(profileLabel, true);
    profileLabel = profileLabelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertNotNull(profileLabel.getId());
    profileLabelStorage.deleteLabel(profileLabel.getId());
    profileLabel = profileLabelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertNull(profileLabel);
  }

  public void testupdateLabel() {
    ProfileLabel profileLabel = createLabel("test profileLabel fr", "fr");
    profileLabelStorage.saveLabel(profileLabel, true);
    profileLabel = profileLabelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertEquals("test profileLabel fr", profileLabel.getLabel());
    profileLabel.setLabel("test profileLabel fr 2");
    profileLabelStorage.saveLabel(profileLabel, false);
    profileLabel = profileLabelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertEquals("test profileLabel fr 2", profileLabel.getLabel());
  }

  public void testGetLabels() {
    assertEquals(0, profileLabelStorage.findLabelByObjectTypeAndObjectId(objectType, objectId).size());
    ProfileLabel profileLabel1 = createLabel("test label fr", "fr");
    ProfileLabel profileLabel2 = createLabel("test label en", "en");
    ProfileLabel profileLabel3 = createLabel("test label it", "it");
    ProfileLabel profileLabel4 = createLabel("test label de", "de");
    ProfileLabel profileLabel5 = createLabel("test label ar", "ar");
    profileLabelStorage.saveLabel(profileLabel1, true);
    profileLabelStorage.saveLabel(profileLabel2, true);
    profileLabelStorage.saveLabel(profileLabel3, true);
    profileLabelStorage.saveLabel(profileLabel4, true);
    profileLabelStorage.saveLabel(profileLabel5, true);
    assertEquals(5, profileLabelStorage.findLabelByObjectTypeAndObjectId(objectType, objectId).size());

  }
  @Override
  protected void tearDown() throws Exception {
    deleteAllLabels();
    super.tearDown();
  }
  private ProfileLabel createLabel(String labelValue, String lang) {
    ProfileLabel profileLabel = new ProfileLabel();
    profileLabel.setLanguage(lang);
    profileLabel.setObjectType(objectType);
    profileLabel.setObjectId(objectId);
    profileLabel.setLabel(labelValue);
    return profileLabel;
  }

  protected void deleteAllLabels() throws Exception {
    ProfileLabelDAO labelDAO = getService(ProfileLabelDAO.class);
    labelDAO.deleteAll();
  }

}
