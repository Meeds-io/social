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
import org.exoplatform.social.core.label.storage.LabelStorage;

/**
 * Unit Tests for {@link LabelStorage}
 */
public class LabelStorageTest extends AbstractCoreTest {

  private final String       objectId   = "1";

  private final String       objectType = "testObject";

  private LabelStorage labelStorage;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    labelStorage = getContainer().getComponentInstanceOfType(LabelStorage.class);
    assertNotNull(labelStorage);
  }

  public void testSaveLabel() {
    Label label = createLabel("test label fr", "fr");
    labelStorage.saveLabel(label, true);
    label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertNotNull(label.getId());
  }

  public void testDeleteLabel() {
    Label label = createLabel("test label fr", "fr");
    labelStorage.saveLabel(label, true);
    label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertNotNull(label.getId());
    labelStorage.deleteLabel(label.getId());
    label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertNull(label);
  }

  public void testupdateLabel() {
    Label label = createLabel("test label fr", "fr");
    labelStorage.saveLabel(label, true);
    label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertEquals("test label fr", label.getLabel());
    label.setLabel("test label fr 2");
    labelStorage.saveLabel(label, false);
    label = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
    assertEquals("test label fr 2", label.getLabel());
  }

  public void testGetLabels() {
    assertEquals(0, labelStorage.findLabelByObjectTypeAndObjectId(objectType, objectId).size());
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
    assertEquals(5, labelStorage.findLabelByObjectTypeAndObjectId(objectType, objectId).size());

  }
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  private Label createLabel(String labelValue, String lang) {
    Label label = new Label();
    label.setLanguage(lang);
    label.setObjectType(objectType);
    label.setObjectId(objectId);
    label.setLabel(labelValue);
    return label;
  }
}
