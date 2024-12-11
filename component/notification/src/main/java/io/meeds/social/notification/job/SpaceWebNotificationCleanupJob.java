/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.notification.job;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;

import lombok.Synchronized;

@Configuration
@EnableScheduling
public class SpaceWebNotificationCleanupJob {

  private static final Log            LOG       = ExoLogger.getLogger(SpaceWebNotificationCleanupJob.class);

  private static final int            DAY_IN_MS = 24 * 3600 * 1000;

  @Autowired
  private SpaceWebNotificationService spaceWebNotificationService;

  @Value("${social.SpaceWebNotificationCleanupJob.keepAlive.days:30}")
  private int                         keepAliveDays;

  @Scheduled(cron = "${social.SpaceWebNotificationCleanupJob.expression:0 5 23 ? * *}")
  @Synchronized
  public void run() {
    long start = System.currentTimeMillis();
    long untilDate = start - keepAliveDays * DAY_IN_MS;
    String untilFormatedDate = DateFormat.getDateTimeInstance().format(new Date(untilDate));
    LOG.info("Delete unread activities created before {}", untilFormatedDate);

    int deletedUnreadNotifications = spaceWebNotificationService.markAllAsReadUntil(untilDate);
    if (deletedUnreadNotifications > 0) {
      LOG.info("{} unread activities has been marked as read unti {}. Operation suceeded within {}ms",
               deletedUnreadNotifications,
               untilFormatedDate,
               NumberFormat.getIntegerInstance().format(System.currentTimeMillis() - start));
    }
  }

}
