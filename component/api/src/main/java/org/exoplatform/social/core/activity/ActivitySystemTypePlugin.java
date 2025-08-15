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
package org.exoplatform.social.core.activity;

import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/*
 * A class to be able to inject system title ids and system activity/comment types
 * This will allow to determine if an activity/comment is editable
 */
public class ActivitySystemTypePlugin extends BaseComponentPlugin {

  public static final String SYSTEM_TITLE_IDS_PARAM = "system.titleIds";

  public static final String SYSTEM_TYPES_PARAM     = "system.types";

  private List<String>       systemActivityTypes    = null;

  private List<String>       systemActivityTitleIds = null;

  public ActivitySystemTypePlugin(InitParams initParams) {
    if (initParams != null) {
      if (initParams.containsKey(SYSTEM_TYPES_PARAM)) {
        systemActivityTypes = initParams.getValuesParam(SYSTEM_TYPES_PARAM).getValues();
      }
      if (initParams.containsKey(SYSTEM_TITLE_IDS_PARAM)) {
        systemActivityTitleIds = initParams.getValuesParam(SYSTEM_TITLE_IDS_PARAM).getValues();
      }
    }
  }

  public List<String> getSystemActivityTitleIds() {
    return systemActivityTitleIds;
  }

  public List<String> getSystemActivityTypes() {
    return systemActivityTypes;
  }
}
