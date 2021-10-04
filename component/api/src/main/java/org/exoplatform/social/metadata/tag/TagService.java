/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.metadata.tag;

import java.util.List;
import java.util.Set;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.metadata.tag.model.*;

public interface TagService {

  public static final MetadataType METADATA_TYPE = new MetadataType(3, "tags");

  /**
   * Updates the list of tags associated to an object.
   * 
   * @param object {@link TagObject} that defines objectType objectId and
   *          parentObjectId
   * @param tagNames {@link Set} of {@link TagName}
   * @param audienceId {@link Metadata} audienceId
   * @param creatorId tag creator {@link Identity} identifier
   * @return {@link Set} or associated {@link TagName} after update
   */
  Set<TagName> saveTags(TagObject object,
                        Set<TagName> tagNames,
                        long audienceId,
                        long creatorId);

  /**
   * @param tagObject {@link TagObject}
   * @return {@link Set} of associated {@link TagName}
   */
  Set<TagName> getTagNames(TagObject tagObject);

  /**
   * Detects the list of Tag names found in content that are added inside an
   * HTML link using a specific class name 'metadata-tag'.
   * 
   * @param content content used to detected associated tags
   * @return {@link Set} of detected {@link TagName}
   */
  Set<TagName> detectTagNames(String content);

  /**
   * Searches in Tags switch name and audienceId
   * 
   * @param tagFilter Search filter of tag
   * @param userIdentityId user {@link Identity} identifier
   * @return {@link List} of {@link TagName}
   * @throws IllegalAccessException when user wasn't found
   */
  List<TagName> findTags(TagFilter tagFilter, long userIdentityId) throws IllegalAccessException;

}
