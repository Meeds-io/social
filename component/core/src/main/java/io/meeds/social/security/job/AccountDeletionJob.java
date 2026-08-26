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
package io.meeds.social.security.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import io.meeds.social.security.service.AccountDeletionService;

import lombok.Synchronized;

/**
 * Job executing the account deletion requests whose grace delay elapsed: no
 * business logic, it delegates the whole processing to
 * {@link AccountDeletionService}. Deliberately not transactional: the service
 * opens one container lifecycle per processed account so a failure never
 * poisons the rest of the batch. There is no cluster lock either
 * ({@link Synchronized} only prevents same-node overlaps): the processing is
 * idempotent by construction — each account's request marker is removed in
 * its own committed step before the deletion happens.
 */
@Configuration
@EnableScheduling
public class AccountDeletionJob {

  @Autowired
  private AccountDeletionService accountDeletionService;

  @Scheduled(cron = "${social.AccountDeletionJob.expression:0 15 5 ? * *}")
  @Synchronized
  public void run() {
    accountDeletionService.processPendingDeletionRequests();
  }

}
