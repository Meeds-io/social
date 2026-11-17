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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Records the space security events, one structured INFO line per event on the
 * logger {@value #LOGGER_NAME}. The date is the line's timestamp. A document
 * is named by its id, never by its name.
 */
public class SpaceSecurityAudit {

  public static final String LOGGER_NAME = "io.meeds.space.security";

  private static final Log   LOG         = ExoLogger.getLogger(LOGGER_NAME);

  private SpaceSecurityAudit() {
    // Static methods only
  }

  public static void settingChanged(long spaceId, String key, String oldValue, String newValue, String actor, String source) {
    log("SETTING_CHANGED", "space", spaceId, "key", key, "old", oldValue, "new", newValue, "actor", actor, "source", source);
  }

  public static void publicLinkRemoved(long spaceId, String documentId, String documentOwner, String actor) {
    log("PUBLIC_LINK_REMOVED", "space", spaceId, "document", documentId, "owner", documentOwner, "actor", actor);
  }

  public static void shareRevoked(long spaceId, String documentId, String formerRecipient, String actor, String source) {
    log("SHARE_REVOKED",
        "space",
        spaceId,
        "document",
        documentId,
        "recipient",
        formerRecipient,
        "actor",
        actor,
        "source",
        source);
  }

  public static void actionRefused(long spaceId, String documentId, String action, String actor, String channel) {
    log("ACTION_REFUSED", "space", spaceId, "document", documentId, "action", action, "actor", actor, "channel", channel);
  }

  public static void grantSkipped(long spaceId, String documentId, String targetIdentity, String source) {
    log("GRANT_SKIPPED", "space", spaceId, "document", documentId, "target", targetIdentity, "source", source);
  }

  public static void externalRefused(long spaceId, String username, String route) {
    log("EXTERNAL_REFUSED", "space", spaceId, "user", username, "route", route);
  }

  private static void log(String event, Object... fields) {
    if (!LOG.isInfoEnabled()) {
      return;
    }
    StringBuilder line = new StringBuilder("event=").append(event);
    for (int i = 0; i + 1 < fields.length; i += 2) {
      line.append(' ').append(fields[i]).append('=').append(sanitize(fields[i + 1]));
    }
    LOG.info(line.toString());
  }

  private static String sanitize(Object value) {
    if (value == null) {
      return "-";
    }
    return String.valueOf(value).replaceAll("\\s", "_");
  }

}
