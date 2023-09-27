/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

package io.meeds.social.link.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class LinkWithIconAttachment extends Link implements Cloneable {

  private static final long serialVersionUID = -5052149626047868897L;

  private String            uploadId;

  public LinkWithIconAttachment(long id, // NOSONAR
                                Map<String, String> name,
                                Map<String, String> description,
                                String url,
                                boolean sameTab,
                                int order,
                                long iconFileId,
                                String uploadId) {
    super(id, name, description, url, sameTab, order, iconFileId);
    this.uploadId = uploadId;
  }

  @Override
  public LinkWithIconAttachment clone() { // NOSONAR
    return new LinkWithIconAttachment(getId(),
                                      getName(),
                                      getDescription(),
                                      getUrl(),
                                      isSameTab(),
                                      getOrder(),
                                      getIconFileId(),
                                      uploadId);
  }

  public Link toLink() {
    return new Link(getId(), getName(), getDescription(), getUrl(), isSameTab(), getOrder(), getIconFileId());
  }

}
