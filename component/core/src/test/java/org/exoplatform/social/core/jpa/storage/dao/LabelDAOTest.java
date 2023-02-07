/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import org.exoplatform.social.core.jpa.storage.dao.jpa.LabelDAO;
import org.exoplatform.social.core.jpa.storage.entity.LabelEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;

public class LabelDAOTest extends BaseCoreTest {
  private LabelDAO labelDAO;

  private final String   objectId   = "1";

  private final String   objectType = "testObject";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    labelDAO = getService(LabelDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    labelDAO.deleteAll();
    super.tearDown();
  }

  public void testCreateLabel() {
    LabelEntity labelEntity = createLabel("test label fr", "fr");

    labelDAO.create(labelEntity);

    restartTransaction();

    LabelEntity result = labelDAO.find(labelEntity.getId());
    assertNotNull(result);
    assertEquals(result.getId(), labelEntity.getId());
  }

  public void testFindLabelByObjectTypeAndObjectIdAndLang() {
    LabelEntity labelEntityFr = createLabel("test label fr", "fr");

    labelEntityFr = labelDAO.create(labelEntityFr);

    LabelEntity labelEntityEn = createLabel("test label en", "en");

    labelDAO.create(labelEntityEn);

    restartTransaction();

    LabelEntity result = labelDAO.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertNotNull(result);
    assertEquals(result.getId(), labelEntityFr.getId());

    List<LabelEntity> results = labelDAO.findLabelByObjectTypeAndObjectId(objectType, objectId);
    assertNotNull(results);
    assertEquals(3, results.size());
  }

  public void testFindLabelByObjectTypeAndObjectId() {
    LabelEntity labelEntityFr = createLabel("test label fr", "fr");

    labelDAO.create(labelEntityFr);

    LabelEntity labelEntityEn = createLabel("test label en", "en");

    labelDAO.create(labelEntityEn);

    LabelEntity labelEntityDe = createLabel("test label de", "de");

    labelDAO.create(labelEntityDe);

    restartTransaction();

    List<LabelEntity> results = labelDAO.findLabelByObjectTypeAndObjectId(objectType, objectId);
    assertNotNull(results);
    assertEquals(2, results.size());
  }

  private LabelEntity createLabel(String label, String lang) {
    LabelEntity labelEntity = new LabelEntity();
    labelEntity.setLanguage(lang);
    labelEntity.setObjectType(objectType);
    labelEntity.setObjectId(objectId);
    labelEntity.setLabel(label);
    return labelEntity;
  }

}
