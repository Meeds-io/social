/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.service;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.jpa.storage.dao.jpa.LabelDAO;
import org.exoplatform.social.core.model.Label;
import org.exoplatform.social.core.test.AbstractCoreTest;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertThrows;

public class LabelServiceTest extends AbstractCoreTest {

  private LabelService labelService;

  private LabelDAO labelDAO;

  private String objectId = "1";
  private String objectType = "testObject";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    labelService = getContainer().getComponentInstanceOfType(LabelService.class);
    labelDAO = getContainer().getComponentInstanceOfType(LabelDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    labelDAO.deleteAll();
    super.tearDown();
  }

  public void testCreateLabel() {
    Throwable exception =
            assertThrows(IllegalArgumentException.class,
                    () -> this.labelService.createLabel(null));
    assertEquals("Label Item Object is mandatory", exception.getMessage());
    exception =
            assertThrows(IllegalArgumentException.class,
                    () -> this.labelService.createLabel(new Label()));
    assertEquals("Object type is mandatory", exception.getMessage());
    Label label = new Label();
    label.setObjectType(objectType);
    Label finalLabel = label;
    exception =
            assertThrows(IllegalArgumentException.class,
                    () -> this.labelService.createLabel(finalLabel));
    assertEquals("Object Id is mandatory", exception.getMessage());
    exception =
            assertThrows(IllegalArgumentException.class,
                    () -> this.labelService.createLabel(createLabel("label", "")));
    assertEquals("language is mandatory", exception.getMessage());
    exception =
            assertThrows(IllegalArgumentException.class,
                    () -> this.labelService.createLabel(createLabel("", "fr")));
    assertEquals("label value is mandatory", exception.getMessage());
    try {
      label = createLabel("test label fr", "fr");
      labelService.createLabel(label);
      label = labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertNotNull(label.getId());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreateDuplicatedLabel() {

    try {
      labelService.createLabel(createLabel("test label fr", "fr"));
      Throwable exception1 =
              assertThrows(ObjectAlreadyExistsException.class,
                      () -> this.labelService.createLabel(createLabel("test label fr", "fr")));
      assertEquals("Label already exist", exception1.getMessage());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testDeleteLabel() {
    Throwable exception1 = assertThrows(IllegalArgumentException.class,
            () -> this.labelService.deleteLabel(0L));
    assertEquals("Label Technical Identifier is mandatory", exception1.getMessage());
    try {
      labelService.createLabel(createLabel("test label fr", "fr"));
      ;
      Label label = labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertNotNull(label.getId());
      labelService.deleteLabel(label.getId());
      label = labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertNull(label);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testUpdateLabel() {
    try {
      labelService.createLabel(createLabel("test label fr", "fr"));
      Label label = labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertEquals("test label fr", label.getLabel());
      label.setLabel("test label fr 2");
      labelService.updateLabel(label);
      label = labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr");
      assertEquals("test label fr 2", label.getLabel());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testFindLabelByObjectTypeAndObjectId() {
    try {
      assertEquals(0, labelService.findLabelByObjectTypeAndObjectId(objectType, objectId).size());
      labelService.createLabel(createLabel("test label fr", "fr"));
      labelService.createLabel(createLabel("test label en", "en"));
      labelService.createLabel(createLabel("test label it", "it"));
      labelService.createLabel(createLabel("test label de", "de"));
      assertEquals(4, labelService.findLabelByObjectTypeAndObjectId(objectType, objectId).size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testMergeLabels() {
    try {
      assertEquals(0, labelService.findLabelByObjectTypeAndObjectId(objectType, objectId).size());
      Label label1 = createLabel("test label fr", "fr");
      Label label2 = createLabel("test label en", "en");
      Label label3 = new Label();
      label3.setLabel("test label de");
      label3.setLanguage("de");
      List<Label> labels = new ArrayList<>();
      labels.add(label1);
      labels.add(label2);
      labels.add(label3);
      labels.add(label3);
      labelService.createLabels(labels, objectType, objectId);
      assertEquals(3, labelService.findLabelByObjectTypeAndObjectId(objectType, objectId).size());
      assertNotNull(labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "de"));
      Label label4 = new Label();
      label4.setLabel("test label it");
      label4.setLanguage("it");
      labels = new ArrayList<>();
      labels.add(labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "fr"));
      label2 = labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "en");
      label2.setLabel("test label en 2");
      labels.add(label2);
      labels.add(label4);
      labels.add(label4);
      labelService.mergeLabels(labels, objectType, objectId);
      labels = labelService.findLabelByObjectTypeAndObjectId(objectType, objectId);
      assertEquals(3, labels.size());
      assertEquals("test label en 2", labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "en").getLabel());
      assertNull(labelService.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, "de"));
      labels = new ArrayList<>();
      labelService.mergeLabels(labels, objectType, objectId);
      labels = labelService.findLabelByObjectTypeAndObjectId(objectType, objectId);
      assertEquals(0, labels.size());
    } catch (Exception e) {
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

}
