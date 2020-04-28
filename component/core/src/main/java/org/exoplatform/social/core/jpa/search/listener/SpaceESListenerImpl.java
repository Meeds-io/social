/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.search.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.search.SpaceIndexingServiceConnector;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

public class SpaceESListenerImpl extends SpaceListenerPlugin {

  private static final Log LOG = ExoLogger.getExoLogger(SpaceESListenerImpl.class);

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getSpace().getId();

    LOG.info("Notifying indexing service for space creation id={}", id);

    indexingService.index(SpaceIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
    reindex(event, "space description");
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getSpace().getId();

    LOG.debug("Notifying indexing service for space removal id={}", id);

    indexingService.unindex(SpaceIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    reindex(event, "space renaming");
  }

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
    reindex(event, "space access edited");
  }

  @Override
  public void spaceRegistrationEdited(SpaceLifeCycleEvent event) {
    reindex(event, "space registration edited");
  }

  @Override
  public void spaceBannerEdited(SpaceLifeCycleEvent event) {
    // Banner not indexed
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    reindex(event, "space application activated");
  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    reindex(event, "space application added");
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    reindex(event, "space application disabled");
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    reindex(event, "space application removed");
  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    reindex(event, "space member granted as manager");
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    reindex(event, "space member joined");
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    reindex(event, "space member left");
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    reindex(event, "space member revoked as manager");
  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {
    reindex(event, "space avatar updated");
  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    reindex(event, "user invited to space");
  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    reindex(event, "user requested access to space");
  }

  private void reindex(SpaceLifeCycleEvent event, String cause) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getSpace().getId();

    LOG.debug("Notifying indexing service for {} id={}", cause, id);

    indexingService.reindex(SpaceIndexingServiceConnector.TYPE, id);
  }

}
