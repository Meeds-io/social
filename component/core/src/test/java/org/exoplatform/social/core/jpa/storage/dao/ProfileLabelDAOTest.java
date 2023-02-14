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
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfileLabelDAO;
import org.exoplatform.social.core.jpa.storage.entity.ProfileLabelEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;

public class ProfileLabelDAOTest extends BaseCoreTest {
  private ProfileLabelDAO labelDAO;

  private final String   objectId   = "1";

  private final String   objectType = "testObject";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    labelDAO = getService(ProfileLabelDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    labelDAO.deleteAll();
    super.tearDown();
  }

  public void testCreateLabel() {
    ProfileLabelEntity profileLabelEntity = createLabel("test label fr", "fr");

    labelDAO.create(profileLabelEntity);

    restartTransaction();

    ProfileLabelEntity result = labelDAO.find(profileLabelEntity.getId());
    assertNotNull(result);
    assertEquals(result.getId(), profileLabelEntity.getId());
  }

  public void testFindLabelByObjectTypeAndObjectIdAndLang() {
    ProfileLabelEntity profileLabelEntityFr = createLabel("test label fr", "fr");

    profileLabelEntityFr = labelDAO.create(profileLabelEntityFr);

    ProfileLabelEntity profileLabelEntityEn = createLabel("test label en", "en");

    labelDAO.create(profileLabelEntityEn);

    restartTransaction();

    ProfileLabelEntity result = labelDAO.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertNotNull(result);
    assertEquals(result.getId(), profileLabelEntityFr.getId());

    List<ProfileLabelEntity> results = labelDAO.findLabelByObjectTypeAndObjectId(objectType, objectId);
    assertNotNull(results);
    assertEquals(3, results.size());
  }

  public void testFindLabelByObjectTypeAndObjectId() {
    ProfileLabelEntity profileLabelEntityFr = createLabel("test label fr", "fr");

    labelDAO.create(profileLabelEntityFr);

    ProfileLabelEntity profileLabelEntityEn = createLabel("test label en", "en");

    labelDAO.create(profileLabelEntityEn);

    ProfileLabelEntity profileLabelEntityDe = createLabel("test label de", "de");

    labelDAO.create(profileLabelEntityDe);

    restartTransaction();

    List<ProfileLabelEntity> results = labelDAO.findLabelByObjectTypeAndObjectId(objectType, objectId);
    assertNotNull(results);
    assertEquals(2, results.size());
  }

  private ProfileLabelEntity createLabel(String label, String lang) {
    ProfileLabelEntity profileLabelEntity = new ProfileLabelEntity();
    profileLabelEntity.setLanguage(lang);
    profileLabelEntity.setObjectType(objectType);
    profileLabelEntity.setObjectId(objectId);
    profileLabelEntity.setLabel(label);
    return profileLabelEntity;
  }

}
