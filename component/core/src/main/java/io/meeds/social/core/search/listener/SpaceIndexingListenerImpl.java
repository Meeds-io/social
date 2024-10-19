/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.social.core.search.listener;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.SpaceIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;

public class SpaceIndexingListenerImpl extends SpaceListenerPlugin {

  public static final String METADATA_OBJECT_TYPE = "space";

  private static final Log   LOG                  = ExoLogger.getExoLogger(SpaceIndexingListenerImpl.class);

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    updateSpaceTags(space);

    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = space.getId();
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
    Space space = event.getSpace();
    updateSpaceTags(space);

    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = space.getId();

    LOG.debug("Notifying indexing service for {} id={}", cause, id);

    indexingService.reindex(SpaceIndexingServiceConnector.TYPE, id);
  }

  private void updateSpaceTags(Space space) {
    TagService tagService = ExoContainerContext.getService(TagService.class);
    Set<TagName> tagNames = tagService.detectTagNames(space.getDescription());
    IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    String editor = StringUtils.firstNonBlank(space.getEditor(), space.getManagers() == null || space.getManagers().length == 0 ? null : space.getManagers()[0]);
    if (editor == null) {
      LOG.warn("Can't update Space Tags due to missing editor username");
    } else {
      Identity creatorIdentity = identityManager.getOrCreateUserIdentity(editor);
      tagService.saveTags(new TagObject(METADATA_OBJECT_TYPE, space.getId()),
                          tagNames,
                          Long.parseLong(spaceIdentity.getId()),
                          Long.parseLong(creatorIdentity.getId()));
    }
  }

}
