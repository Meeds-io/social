/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.webui.profile;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;

/**
 * Component that manages title bar of profile management section.<br>
 *
 *
 */
@ComponentConfig(
  template =  "war:/groovy/social/webui/profile/UITitleBar.gtmpl"
)

public class UITitleBar extends UIComponent {

  /**
   * Overrides event method of UIComponent.<br>
   *
   * @return event of title bar component.
   */
  @Override
  public String event(String name, String beanId) throws Exception {
    UIProfileSection pf = getParent();
    return pf.event(name, beanId);
  }

  /**
   * Gets the name of parent component.<br>
   *
   * @return name of parent component.
   */
  public String getTranlationKey() {
    UIProfileSection pf = getParent();
    return pf.getName();
  }

  /**
   * Checks the current user is right edit permission.<br>
   *
   * @return true if current user has permission.
   */
  public boolean isEditable() {
    UIProfileSection pf = getParent();
    return pf.isEditable();
  }

  /**
   * Checks the current display of title bar is can be edit.<br>
   *
   * @return true if title bar is in edit mode.
   */
  public boolean isEditMode() {
    UIProfileSection pf = getParent();
    return pf.isEditMode();
  }
}
