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
package io.meeds.social.reaction.storage;

import static io.meeds.social.reaction.service.ReactionService.METADATA_TYPE_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;

@Component
public class ReactionStorage {

  private static final int CREATORS_QUERY_CHUNK_SIZE = 500;

  @Autowired
  private MetadataService  metadataService;

  public Map<String, Long> countReactionsByOption(MetadataObject object) {
    return metadataService.countMetadataItemsByMetadataTypeAndObjectGroupedByMetadataName(METADATA_TYPE_NAME, object);
  }

  public MetadataItem getUserReactionItem(MetadataObject object, long identityId) {
    return metadataService.getMetadataItemsByMetadataTypeAndObjectAndCreators(METADATA_TYPE_NAME,
                                                                              object,
                                                                              Collections.singletonList(identityId))
                          .stream()
                          .findFirst()
                          .orElse(null);
  }

  public List<MetadataItem> getReactionItemsByCreators(MetadataObject object, List<Long> creatorIds) {
    if (creatorIds.size() <= CREATORS_QUERY_CHUNK_SIZE) {
      return metadataService.getMetadataItemsByMetadataTypeAndObjectAndCreators(METADATA_TYPE_NAME, object, creatorIds);
    }
    // chunk the IN clause: some databases cap its expression count (Oracle:
    // 1000) and the Service API accepts unbounded pages (limit <= 0)
    List<MetadataItem> items = new ArrayList<>();
    for (int fromIndex = 0; fromIndex < creatorIds.size(); fromIndex += CREATORS_QUERY_CHUNK_SIZE) {
      int toIndex = Math.min(fromIndex + CREATORS_QUERY_CHUNK_SIZE, creatorIds.size());
      items.addAll(metadataService.getMetadataItemsByMetadataTypeAndObjectAndCreators(METADATA_TYPE_NAME,
                                                                                      object,
                                                                                      creatorIds.subList(fromIndex, toIndex)));
    }
    return items;
  }

  /**
   * @param object {@link MetadataObject} to retrieve the typed reactors of
   * @return every typed reaction item of the object — bounded by the typed
   *         reactions count, used only by the 'like' drawer filter
   */
  public List<MetadataItem> getTypedReactionItems(MetadataObject object) {
    return metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE_NAME, object);
  }

  public List<MetadataItem> getReactionItemsByOption(String reactionId, MetadataObject object, long offset, long limit) {
    return metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(reactionId,
                                                                          METADATA_TYPE_NAME,
                                                                          object.getType(),
                                                                          object.getId(),
                                                                          offset,
                                                                          limit);
  }

  public void createReaction(MetadataObject object, String reactionId, long identityId) throws ObjectAlreadyExistsException {
    metadataService.createMetadataItem(object, new MetadataKey(METADATA_TYPE_NAME, reactionId, 0), identityId);
  }

  public void deleteReaction(long itemId, long identityId) throws ObjectNotFoundException {
    metadataService.deleteMetadataItem(itemId, identityId);
  }

}
