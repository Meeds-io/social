/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.processor;

import java.util.*;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;

/**
 * Retrieves the list of Metadatas to attach to activity. This will allow to
 * cache metadata items for a given activity without having to retrieve it again
 * each time someone accesses the activity
 */
public class MetadataActivityProcessor extends BaseActivityProcessorPlugin {

  public static final String ACTIVITY_METADATA_OBJECT_TYPE = "activity";

  public static final String COMMENT_METADATA_OBJECT_TYPE  = "comment";

  private MetadataService    metadataService;

  public MetadataActivityProcessor(MetadataService metadataService, InitParams params) {
    super(params);
    this.metadataService = metadataService;
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null) {
      List<MetadataItem> metadataItems =
                                       metadataService.getMetadataItemsByObject(ACTIVITY_METADATA_OBJECT_TYPE, activity.getId());
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
