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
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Link implements Serializable, Cloneable {

  private static final long   serialVersionUID = -5104279089111946609L;

  private long                id;

  private Map<String, String> name;

  private Map<String, String> description;

  private String              url;

  private boolean             sameTab;

  private int                 order;

  private long                iconFileId;

  public Link(Link link) {
    id = link.id;
    name = new HashMap<>(link.name);
    description = new HashMap<>(link.description);
    url = link.url;
    sameTab = link.sameTab;
    order = link.order;
    iconFileId = link.iconFileId;
  }

  @Override
  public Link clone() { // NOSONAR
    return new Link(this);
  }

}
