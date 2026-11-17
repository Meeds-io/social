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
package io.meeds.social.space.security;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Pins the audit line format: one INFO line per event on the
 * io.meeds.space.security logger, key=value pairs in a fixed order, "-" for a
 * missing value, and no whitespace in a value, so that one event never spans
 * two lines nor shifts its fields.
 */
public class SpaceSecurityAuditTest {

  private Logger                      logger;

  private Level                       level;

  private ListAppender<ILoggingEvent> appender;

  @Before
  public void setUp() {
    logger = (Logger) LoggerFactory.getLogger(SpaceSecurityAudit.LOGGER_NAME);
    level = logger.getLevel();
    logger.setLevel(Level.INFO);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @After
  public void tearDown() {
    logger.detachAppender(appender);
    logger.setLevel(level);
  }

  @Test
  public void testEachEventIsOneInfoLineOfItsFields() {
    SpaceSecurityAudit.settingChanged(3, "meeds.documents.limitAccessManagement", null, "true", "mary", "spaceSettings");
    SpaceSecurityAudit.publicLinkRemoved(3, "doc1", "john", "mary");
    SpaceSecurityAudit.shareRevoked(3, "doc1", "john", "mary", "bulk");
    SpaceSecurityAudit.actionRefused(3, "doc1", "share", "mary", "rest");
    SpaceSecurityAudit.grantSkipped(3, "doc1", "*:/spaces/sales", "activity");
    SpaceSecurityAudit.externalRefused(3, "guest", "invitation");

    assertEquals(List.of("event=SETTING_CHANGED space=3 key=meeds.documents.limitAccessManagement old=- new=true actor=mary source=spaceSettings",
                         "event=PUBLIC_LINK_REMOVED space=3 document=doc1 owner=john actor=mary",
                         "event=SHARE_REVOKED space=3 document=doc1 recipient=john actor=mary source=bulk",
                         "event=ACTION_REFUSED space=3 document=doc1 action=share actor=mary channel=rest",
                         "event=GRANT_SKIPPED space=3 document=doc1 target=*:/spaces/sales source=activity",
                         "event=EXTERNAL_REFUSED space=3 user=guest route=invitation"),
                 lines());
    assertEquals(List.of(Level.INFO), appender.list.stream().map(ILoggingEvent::getLevel).distinct().toList());
  }

  @Test
  public void testAValueWithWhitespaceStaysInItsField() {
    SpaceSecurityAudit.actionRefused(3, "doc 1", "share", "mary\nevent=FORGED", null);

    assertEquals(List.of("event=ACTION_REFUSED space=3 document=doc_1 action=share actor=mary_event=FORGED channel=-"), lines());
  }

  private List<String> lines() {
    return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
  }

}
