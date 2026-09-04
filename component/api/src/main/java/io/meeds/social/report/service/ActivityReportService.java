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
package io.meeds.social.report.service;

import java.util.List;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.Identity;

import io.meeds.social.report.model.ActivityReport;

public interface ActivityReportService {

  String       METADATA_TYPE_NAME      = "reports";

  String       REASON_PROPERTY         = "reason";

  String       STATUS_PROPERTY         = "status";

  String       STATUS_ACTIVE           = "active";

  String       STATUS_STALE            = "stale";

  String       EVENT_ACTIVITY_REPORTED = "social.activity.reported";

  List<String> DEFAULT_REASONS         = List.of("spam",
                                                 "fakeAccount",
                                                 "harassmentOrBullying",
                                                 "hateSpeechOrDiscrimination",
                                                 "violenceOrThreats",
                                                 "falseInformation",
                                                 "intellectualPropertyViolation");

  /**
   * Records a report made by the authenticated user on a space-feed activity
   * or comment, then broadcasts {@link #EVENT_ACTIVITY_REPORTED}. One active
   * report per (reporter, object): a stale report left by a previous author
   * edit is reactivated instead of duplicated.
   *
   * @param activityId activity id, or comment id with its "comment" prefix
   * @param reason one of {@link #DEFAULT_REASONS}
   * @param viewerAclIdentity authenticated user ACL identity, resolved
   *          server-side from the session
   * @return the recorded {@link ActivityReport}
   * @throws ObjectNotFoundException when no activity/comment exists with this
   *           id
   * @throws IllegalAccessException when the user can't view the target or is
   *           its author
   * @throws IllegalArgumentException when the reason isn't allowed, or the
   *           target isn't space-feed content
   * @throws ObjectAlreadyExistsException when the user already has an active
   *           report on this target
   */
  ActivityReport reportActivity(String activityId,
                                String reason,
                                Identity viewerAclIdentity) throws ObjectNotFoundException,
                                                            IllegalAccessException,
                                                            ObjectAlreadyExistsException;

  /**
   * Flips every active report on the given activity/comment to the stale
   * state, across all reporters — called when the author edits the target, so
   * that everyone who had reported it may report the edited content again.
   * Keeps each report's reason and timestamp for moderation audit, and raises
   * neither a notification nor the {@link #EVENT_ACTIVITY_REPORTED} event.
   *
   * @param activity the edited activity or comment
   */
  void markReportsStale(org.exoplatform.social.core.activity.model.ExoSocialActivity activity);

}
