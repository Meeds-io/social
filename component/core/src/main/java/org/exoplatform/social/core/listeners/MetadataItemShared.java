/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataObject;

public class MetadataItemShared extends AbstractMetadataItemListener<MetadataObject, String> {

  public MetadataItemShared(MetadataService metadataService,
                            ActivityStorage activityStorage,
                            SpaceStorage spaceStorage,
                            IndexingService indexingService) {
    super(metadataService, activityStorage, spaceStorage, indexingService);
  }

  @Override
  public void onEvent(Event<MetadataObject, String> event) throws Exception {
    MetadataObject sourceObject = event.getSource();
    String targetObjectId = event.getData();
    handleMetadataModification(sourceObject.getType(), targetObjectId);
  }
}
