/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import java.util.List;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.storage.PageStorage;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import io.meeds.common.ContainerTransactional;

public class ManagerPropertySettingUpdatedListener extends Listener<ProfilePropertyService, ProfilePropertySetting> {

  public static final List<String> orgChartPages = List.of("portal::mycraft::myteam", "portal::global::organizationalchart");
  private static final String ALL_USERS_PERMISSION = "*:/platform/users";
  private static final String ALL_ADMINISTRATORS_PERMISSION = "*:/platform/administrators";
  private final PageStorage pageStorage;
  private final UserACL userACL;

  public ManagerPropertySettingUpdatedListener(PageStorage pageStorage, UserACL userACL) {
    this.pageStorage = pageStorage;
    this.userACL = userACL;
  }

  @Override
  @ContainerTransactional
  public void onEvent(Event<ProfilePropertyService, ProfilePropertySetting> event) throws Exception {
    ProfilePropertySetting propertySetting = event.getData();
    if ("manager".equalsIgnoreCase(event.getData().getPropertyName())) {
      for (String pageRefKey : orgChartPages) {
        PageKey pageKey = PageKey.parse(pageRefKey);
        PageContext pageContext = pageStorage.loadPage(pageKey);
        if (pageContext != null) {
          PageState page = pageContext.getState();
          PageState pageState = new PageState(page.getDisplayName(),
                                              page.getDescription(),
                                              page.getShowMaxWindow(),
                                              page.getFactoryId(),
                                              propertySetting.isActive() ? List.of(ALL_USERS_PERMISSION) :
                                                                         List.of(userACL.getSuperUser()),
                                              propertySetting.isActive() ? ALL_ADMINISTRATORS_PERMISSION :
                                                                         userACL.getSuperUser());
          pageStorage.savePage(new PageContext(pageKey, pageState));
        }
      }
    }
  }
}
