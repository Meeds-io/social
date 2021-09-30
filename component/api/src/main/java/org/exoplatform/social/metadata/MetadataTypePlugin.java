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
package org.exoplatform.social.metadata;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.metadata.model.MetadataType;

import lombok.Getter;

public class MetadataTypePlugin extends BaseComponentPlugin {

  private static final String METADATA_TYPE_PARAM_NAME        = "metadataType";

  private static final String SHAREABLE_PARAM_NAME            = "shareable";

  private static final String ALLOW_MULTIPLE_ITEMS_PARAM_NAME = "allowMultipleItemsPerObject";

  @Getter
  protected MetadataType      metadataType;

  @Getter
  protected boolean           shareable;

  @Getter
  protected boolean           allowMultipleItemsPerObject;

  public MetadataTypePlugin(InitParams params) {
    if (params != null) {
      if (params.containsKey(METADATA_TYPE_PARAM_NAME)) {
        this.metadataType = (MetadataType) params.getObjectParam(METADATA_TYPE_PARAM_NAME).getObject();
      }
      if (params.containsKey(SHAREABLE_PARAM_NAME)) {
        this.shareable = StringUtils.equals(params.getValueParam(SHAREABLE_PARAM_NAME).getValue(), "true");
      }
      if (params.containsKey(ALLOW_MULTIPLE_ITEMS_PARAM_NAME)) {
        this.allowMultipleItemsPerObject = StringUtils.equals(params.getValueParam(ALLOW_MULTIPLE_ITEMS_PARAM_NAME).getValue(),
                                                              "true");
      }
    }
    if (this.metadataType == null || this.metadataType.getId() == 0 || StringUtils.isBlank(this.metadataType.getName())) {
      throw new IllegalStateException("MetadataType is mandatory");
    }
  }

  @Override
  public String getName() {
    return this.metadataType.getName();
  }

  public long getId() {
    return this.metadataType.getId();
  }

}
