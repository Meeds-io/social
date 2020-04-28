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
package org.exoplatform.social.portlet;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.social.webui.UISocialNavigation;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * {@link UISocialNavigationPortlet} used to manage social navigation link.
 */
@ComponentConfigs({
  @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
  ),
  @ComponentConfig(
    type = UISocialNavigation.class,
    id = "UIHorizontalNavigation",
    events = @EventConfig(listeners = UISocialNavigation.SelectNodeActionListener.class)
  )
})

public class UISocialNavigationPortlet extends UIPortletApplication {
  /**
   * constructor
   *
   * @throws Exception
   */
  public UISocialNavigationPortlet() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletRequest prequest = context.getRequest();
    PortletPreferences prefers = prequest.getPreferences();
    UISocialNavigation portalNavigation = addChild(UISocialNavigation.class, "UIHorizontalNavigation", null);
    portalNavigation.setUseAjax(Boolean.valueOf(prefers.getValue("useAJAX", "true")));
  }
}