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

  @Autowired
  private MetadataService metadataService;

  public Map<String, Long> countReactionsByOption(MetadataObject object) {
    return metadataService.countMetadataItemsByMetadataTypeAndObjectGroupedByMetadataName(METADATA_TYPE_NAME, object);
  }

  public List<MetadataItem> getReactionItems(MetadataObject object) {
    return metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE_NAME, object);
  }

  public MetadataItem getUserReactionItem(MetadataObject object, long identityId) {
    return getReactionItems(object).stream()
                                   .filter(item -> item.getCreatorId() == identityId)
                                   .findFirst()
                                   .orElse(null);
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
