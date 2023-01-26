/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.model.Label;
import org.exoplatform.social.core.storage.api.LabelStorage;

/**
 * Unit Tests for {@link LabelStorage}
 */
public class LabelStorageTest extends AbstractCoreTest {

  private final Log             LOG = ExoLogger.getLogger(LabelStorageTest.class);

  private String objectId = "1";
  private String objectType = "testObject";
  private LabelStorage labelStorage;
  
  

  @Override
  public void setUp() throws Exception {
    super.setUp();
    labelStorage = getContainer().getComponentInstanceOfType(LabelStorage.class);
    assertNotNull(labelStorage);
  }

  public void testSaveLabel() {
    try {
      Label label = createLabel("test label fr", "fr");
      label = labelStorage.saveLabel(label, true);
      label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType,objectId,"fr");
      assertNotNull(label.getId());
    } catch (Exception e) {
      LOG.error(e.getMessage());
      fail(e.getMessage());
    }
  }

  public void testDeleteLabel() {
    try {
      Label label = createLabel("test label fr", "fr");
      label = labelStorage.saveLabel(label, true);
      label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType,objectId,"fr");
      assertNotNull(label.getId());
      labelStorage.deleteLabel(label.getId());
      label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType,objectId,"fr");
      assertNull(label);
    } catch (Exception e) {
      LOG.error(e.getMessage());
      fail(e.getMessage());
    }
  }

  public void testupdateLabel() {
    try {
      Label label = createLabel("test label fr", "fr");
      labelStorage.saveLabel(label, true);
      label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType,objectId,"fr");
      assertEquals("test label fr",label.getLabel());
      label.setLabel("test label fr 2");
      labelStorage.saveLabel(label, false);
      label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType,objectId,"fr");
      assertEquals("test label fr 2",label.getLabel());
    } catch (Exception e) {
      LOG.error(e.getMessage());
      fail(e.getMessage());
    }
  }

  public void testGetLabels() {
    try {
      assertEquals(0, labelStorage.findLabelByObjectTypeAndObjectId(objectType,objectId).size());
      Label label1 = createLabel("test label fr", "fr");
      Label label2 = createLabel("test label en", "en");
      Label label3 = createLabel("test label it", "it");
      Label label4 = createLabel("test label de", "de");
      Label label5 = createLabel("test label ar", "ar");
      labelStorage.saveLabel(label1, true);
      labelStorage.saveLabel(label2, true);
      labelStorage.saveLabel(label3, true);
      labelStorage.saveLabel(label4, true);
      labelStorage.saveLabel(label5, true);
      assertEquals(5, labelStorage.findLabelByObjectTypeAndObjectId(objectType,objectId).size());
    } catch (Exception e) {
      LOG.error(e.getMessage());
      fail(e.getMessage());
    }
  }


  private Label createLabel(String labelValue, String lang) {
    Label label = new Label();
    label.setLanguage(lang);
    label.setObjectType(objectType);
    label.setObjectId(objectId);
    label.setLabel(labelValue);
    return label;
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

}
