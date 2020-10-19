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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.navigation.help.HelpService;
import org.exoplatform.social.navigation.help.Helper;

/**
 * @author <a href="kmenzli@exoplatform.com">Kmenzli</a>
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/social/portlet/UIHelpPlatformToolbarPortlet.gtmpl")
public class UIHelpPlatformToolbarPortlet extends UIPortletApplication {

  private static final Log LOG               = ExoLogger.getExoLogger(UIHelpPlatformToolbarPortlet.class);

  private String           currentNavigation = "";

  private String           helpPage          = HelpService.DEFAULT_HELP_PAGE;

  private HelpService      helpService       = null;

  private SpaceService     spaceService      = null;

  public UIHelpPlatformToolbarPortlet() throws Exception {
    helpService = getApplicationComponent(HelpService.class);
    try {
      spaceService = getApplicationComponent(SpaceService.class);
    } catch (Exception exception) {
      // spaceService should be "null" because the Social profile isn't
      // activated
      LOG.warn("SpaceService not loaded by the kernel please activate [social] profile");
    }
  }

  public String getHelpPage() {

    currentNavigation = Helper.getCurrentNavigation(spaceService);

    if (Helper.present(currentNavigation) && ((helpService != null))) {
      helpPage = helpService.fetchHelpPage(currentNavigation);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("The navigation [[ " + currentNavigation + " ]] has as page help [[ " + helpPage + " ]]");
    }

    return helpPage;
  }

}
