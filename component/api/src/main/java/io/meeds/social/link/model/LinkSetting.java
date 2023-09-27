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

import java.io.Serializable;
import java.util.Map;

import io.meeds.social.link.constant.LinkDisplayType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LinkSetting implements Serializable, Cloneable {

  private static final long   serialVersionUID = 179594883916426378L;

  private long                id;

  private String              name;

  private String              pageReference;

  private long                spaceId;

  private Map<String, String> header;

  private LinkDisplayType     type;

  private boolean             largeIcon;

  private boolean             showName;

  private boolean             showDescription;

  private String              seeMore;

  private long                lastModified;

  @Override
  public LinkSetting clone() { // NOSONAR
    return new LinkSetting(id,
                           name,
                           pageReference,
                           spaceId,
                           header,
                           type,
                           largeIcon,
                           showName,
                           showDescription,
                           seeMore,
                           lastModified);
  }

}
