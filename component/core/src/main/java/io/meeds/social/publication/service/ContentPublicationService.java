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
package io.meeds.social.publication.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.publication.plugin.PublicationPlugin;

/**
 * Generic content publication Service, dispatching scheduled publication
 * operations to the {@link PublicationPlugin} registered for each content
 * type. It owns no storage: each content type keeps its scheduling state in
 * its own domain.
 */
@Service
public class ContentPublicationService {

  private static final Log        LOG        = ExoLogger.getLogger(ContentPublicationService.class);

  private static final int        BATCH_SIZE = 100;

  @Autowired(required = false)
  protected List<PublicationPlugin> publicationPlugins;

  /**
   * Publishes every content of every registered content type which scheduled
   * publication time is due
   */
  public void publishDueContents() {
    long dueTime = System.currentTimeMillis();
    getPublicationPlugins().forEach(plugin -> publishDueContents(plugin, dueTime));
  }

  /**
   * Publishes a scheduled content by dispatching to the {@link PublicationPlugin}
   * registered for its content type
   *
   * @param objectType the content object type, like 'activity'
   * @param objectId the content object id
   * @return the published object, or null when the content isn't a pending
   *         scheduled content (anymore)
   */
  public Object publish(String objectType, String objectId) {
    PublicationPlugin plugin = getPublicationPlugin(objectType);
    if (plugin == null) {
      throw new IllegalArgumentException("publication.unsupportedObjectType: " + objectType);
    }
    return plugin.publish(objectId);
  }

  /**
   * @param objectType the content object type, like 'activity'
   * @return the registered {@link PublicationPlugin} for the given content
   *         object type, or null when none
   */
  public PublicationPlugin getPublicationPlugin(String objectType) {
    return getPublicationPlugins().stream()
                                  .filter(plugin -> plugin.getObjectType().equals(objectType))
                                  .findFirst()
                                  .orElse(null);
  }

  private List<PublicationPlugin> getPublicationPlugins() {
    return CollectionUtils.isEmpty(publicationPlugins) ? Collections.emptyList() : publicationPlugins;
  }

  private void publishDueContents(PublicationPlugin plugin, long dueTime) {
    try {
      List<String> objectIds;
      int published;
      do {
        objectIds = plugin.getDueObjectIds(dueTime, 0, BATCH_SIZE);
        published = 0;
        for (String objectId : objectIds) {
          if (publishObject(plugin, objectId)) {
            published++;
          }
        }
      } while (!objectIds.isEmpty() && published > 0);
    } catch (Exception e) {
      LOG.warn("Error while publishing due contents of type {}, it will be retried on next run", plugin.getObjectType(), e);
    }
  }

  private boolean publishObject(PublicationPlugin plugin, String objectId) {
    try {
      return plugin.publish(objectId) != null;
    } catch (Exception e) {
      LOG.warn("Error while publishing content of type {} with id {}, it will be retried on next run",
               plugin.getObjectType(),
               objectId,
               e);
      return false;
    }
  }

}
