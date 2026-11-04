/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.reminder.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

@SpringBootTest(classes = {
  ChangesReminderService.class,
})
@RunWith(SpringRunner.class)
public class ChangesReminderServiceTest {

  private static final String    USERNAME = "john";

  private static final Context   CONTEXT  = Context.USER.id(USERNAME);

  private static final Scope     SCOPE    = Scope.APPLICATION.id("changesReminder");

  @MockitoBean
  private SettingService         settingService;

  @Autowired
  private ChangesReminderService changesReminderService;

  @Test
  public void acknowledgeStoresTrueForTheUser() {
    changesReminderService.acknowledge(USERNAME, "LayoutEditorChangesAnnounced");
    verify(settingService).set(eq(CONTEXT),
                               eq(SCOPE),
                               eq("LayoutEditorChangesAnnounced"),
                               argThat(value -> Boolean.TRUE.equals(value.getValue())));
  }

  @Test
  public void acknowledgeAcceptsTheNamesTheApplicationsUse() {
    changesReminderService.acknowledge(USERNAME, "notes-tree");
    changesReminderService.acknowledge(USERNAME, "termsAndConditions");
    changesReminderService.acknowledge(USERNAME, "a.b_c-1");
    verify(settingService).set(eq(CONTEXT), eq(SCOPE), eq("notes-tree"), any());
    verify(settingService).set(eq(CONTEXT), eq(SCOPE), eq("termsAndConditions"), any());
    verify(settingService).set(eq(CONTEXT), eq(SCOPE), eq("a.b_c-1"), any());
  }

  @Test
  public void acknowledgeRefusesAnInvalidName() {
    assertInvalidName(null);
    assertInvalidName("");
    assertInvalidName("a b");
    assertInvalidName("a/b");
    assertInvalidName("<script>");
    assertInvalidName("a".repeat(101));
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void acknowledgeAcceptsTheLongestName() {
    String name = "a".repeat(100);
    changesReminderService.acknowledge(USERNAME, name);
    verify(settingService).set(eq(CONTEXT), eq(SCOPE), eq(name), any());
  }

  @Test
  public void acknowledgeRefusesABlankUser() {
    assertThrows(IllegalArgumentException.class, () -> changesReminderService.acknowledge(" ", "notes-tree"));
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void isAcknowledgedReadsTheUserValue() {
    assertFalse(changesReminderService.isAcknowledged(USERNAME, "notes-tree"));

    // The generic settings REST stored the value as a string
    when(settingService.get(CONTEXT, SCOPE, "notes-tree")).thenAnswer(invocation -> SettingValue.create("true"));
    assertTrue(changesReminderService.isAcknowledged(USERNAME, "notes-tree"));

    when(settingService.get(CONTEXT, SCOPE, "notes-tree")).thenAnswer(invocation -> SettingValue.create(true));
    assertTrue(changesReminderService.isAcknowledged(USERNAME, "notes-tree"));

    when(settingService.get(CONTEXT, SCOPE, "notes-tree")).thenAnswer(invocation -> SettingValue.create("anything"));
    assertFalse(changesReminderService.isAcknowledged(USERNAME, "notes-tree"));
  }

  @Test
  public void isAcknowledgedRefusesAnInvalidName() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                      () -> changesReminderService.isAcknowledged(USERNAME, "a b"));
    assertEquals(ChangesReminderService.INVALID_NAME_MESSAGE, exception.getMessage());
    verify(settingService, never()).get(any(), any(), anyString());
  }

  private void assertInvalidName(String name) {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                      () -> changesReminderService.acknowledge(USERNAME, name));
    assertEquals(ChangesReminderService.INVALID_NAME_MESSAGE, exception.getMessage());
  }

}
