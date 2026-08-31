/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.report.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import io.meeds.social.report.service.ActivityReportService;

@RunWith(MockitoJUnitRunner.class)
public class ActivityReportRestTest {

  @Mock
  private ActivityReportService activityReportService;

  private ActivityReportRest    activityReportRest;

  @Before
  public void setUp() throws Exception {
    activityReportRest = new ActivityReportRest();
    Field field = ActivityReportRest.class.getDeclaredField("activityReportService");
    field.setAccessible(true); // NOSONAR
    field.set(activityReportRest, activityReportService);
    ConversationState.setCurrent(new ConversationState(new Identity("john")));
  }

  @After
  public void tearDown() {
    ConversationState.setCurrent(null);
  }

  @Test
  public void testReportDelegatesToTheService() throws Exception {
    activityReportRest.reportActivity("55", "spam");
    verify(activityReportService).reportActivity(eq("55"), eq("spam"), any(Identity.class));
  }

  @Test
  public void testExceptionToStatusContract() throws Exception {
    doThrow(new ObjectNotFoundException("Activity 55 not found")).when(activityReportService)
                                                                 .reportActivity(eq("55"), eq("spam"), any());
    assertStatus(HttpStatus.NOT_FOUND);

    doThrow(new IllegalAccessException("not allowed")).when(activityReportService)
                                                      .reportActivity(eq("55"), eq("spam"), any());
    assertStatus(HttpStatus.FORBIDDEN);

    doThrow(new IllegalArgumentException("report.invalidReason")).when(activityReportService)
                                                                 .reportActivity(eq("55"), eq("spam"), any());
    assertStatus(HttpStatus.BAD_REQUEST);

    doThrow(new ObjectAlreadyExistsException(new Object(), "report.alreadyReported")).when(activityReportService)
                                                                                     .reportActivity(eq("55"),
                                                                                                     eq("spam"),
                                                                                                     any());
    assertStatus(HttpStatus.CONFLICT);
  }

  private void assertStatus(HttpStatus expectedStatus) {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                                                     () -> activityReportRest.reportActivity("55", "spam"));
    assertEquals(expectedStatus, exception.getStatusCode());
  }

}
