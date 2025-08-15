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
package org.exoplatform.social.core.plugin;

import io.meeds.social.translation.plugin.TranslationPlugin;

public class OrganizationalChartHeaderTranslation extends TranslationPlugin {

  public static final String ORGANIZATIONAL_CHART_OBJECT_TYPE = "organizationalChart";

  @Override
  public String getObjectType() {
    return ORGANIZATIONAL_CHART_OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(long objectId, String username) {
    return true;
  }

  @Override
  public boolean hasEditPermission(long objectId, String username) {
    return true;
  }

  @Override
  public long getAudienceId(long objectId) {
    return 0;
  }

  @Override
  public long getSpaceId(long objectId) {
    return 0;
  }
}
