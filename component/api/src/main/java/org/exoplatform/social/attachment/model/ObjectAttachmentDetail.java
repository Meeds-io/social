/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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
package org.exoplatform.social.attachment.model;

import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectAttachmentDetail implements Cloneable {

  private String id;

  private String name;

  private String mimetype;

  private long   size;

  private long   updated;

  private String updater;

  private String altText;

  private String format;

  public ObjectAttachmentDetail(String id, String name, String mimetype, long size, long updated, String updater) {
    this.id = id;
    this.name = name;
    this.mimetype = mimetype;
    this.size = size;
    this.updated = updated;
    this.updater = updater;
  }

  @Override
  public ObjectAttachmentDetail clone() { // NOSONAR
    return new ObjectAttachmentDetail(id, name, mimetype, size, updated, updater, altText, format);
  }

  @Override
  public String toString() {
    return toJsonString();
  }

  public String toJsonString() {
    try {
      return new JsonGeneratorImpl().createJsonObject(this).toString();
    } catch (JsonException e) {
      throw new IllegalStateException("Error parsing current object to string", e);
    }
  }

}
