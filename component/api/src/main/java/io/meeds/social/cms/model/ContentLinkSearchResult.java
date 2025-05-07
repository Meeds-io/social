/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.cms.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ContentLinkSearchResult extends ContentLink {

  public ContentLinkSearchResult(ContentLink link) {
    super(link.getObjectType(), link.getObjectId(), link.getLocale(), link.getTitle(), link.getUri());
  }

  public ContentLinkSearchResult(ContentObjectIdentifier link) {
    super(link.getObjectType(), link.getObjectId());
  }

  public ContentLinkSearchResult(String objectType, String objectId, String title) {
    super(objectType, objectId, null, title, null);
  }

}
