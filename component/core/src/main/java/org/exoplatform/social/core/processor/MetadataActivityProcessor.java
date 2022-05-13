/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

/**
 * Retrieves the list of Metadatas to attach to activity. This will allow to
 * cache metadata items for a given activity without having to retrieve it again
 * each time someone accesses the activity
 */
public class MetadataActivityProcessor extends BaseActivityProcessorPlugin {

  private MetadataService metadataService;

  public MetadataActivityProcessor(MetadataService metadataService, InitParams params) {
    super(params);
    this.metadataService = metadataService;
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null) {
      MetadataObject metadataObject = activity.getMetadataObject();
      List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
      Map<String, List<MetadataItem>> metadatas = new HashMap<>();
      metadataItems.forEach(metadataItem -> {
        String type = metadataItem.getMetadata().getType().getName();
        if (metadatas.get(type) == null) {
          metadatas.put(type, new ArrayList<>());
        }
        metadatas.get(type).add(metadataItem);
      });
      activity.setMetadatas(metadatas);
    }
  }

}
