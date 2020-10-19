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
package org.exoplatform.social.navigation.help;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;

/**
 * @author <a href="kmenzli@exoplatform.com">Kmenzli</a>
 */

public class Helper {

  private static final Log   LOG             = ExoLogger.getExoLogger(Helper.class);

  public static final String DEFAULT_HELP_ID = "default";

  private Helper() {
  }

  public static boolean present(String theString) {
    boolean present = false;
    if (theString != null && theString.length() != 0) {
      present = true;
    }
    return present;
  }

  public static String getCurrentNavigation(SpaceService spaceService) {
    try {
      String nav = Util.getUIPortal().getNavPath().getName();
      String url = Util.getPortalRequestContext().getRequest().getRequestURL().toString();
      if ((url.contains("/:spaces:")) || (url.contains("/spaces/"))) {
        if (url.contains("documents")) {
          return "space:document";
        } else if (url.contains("wiki")) {
          return "space:wiki";
        } else if (url.contains("tasks")) {
          return "space:tasks";
        } else if ((url.toLowerCase().contains("answer")) || (url.contains("faq")) || (url.contains("poll"))) {
          return "space:faq_annswer";
        } else if (url.contains("calendar")) {
          return "space:calendar";
        } else if (url.contains("forum")) {
          return "space:forum";
        } else if (nav.equals("settings")) {
          return "space:manager";
        } else {
          String spaceUrl = getSelectedPageNode().getURI();
          Space space = spaceService.getSpaceByUrl(spaceUrl);
          if (space != null) {
            if (space.getPrettyName().equals(nav)) {
              return "space:activity_stream";
            }
          } else {
            return DEFAULT_HELP_ID;
          }
        }
      } else {
        boolean isOwner = Utils.isOwner();
        if (url.contains("wiki") && isOwner) {
          return "personnal:wiki";
        } else if ((url.contains("profile")) && isOwner) {
          return "personnal:profile";
        } else if ((url.contains("connections")) && isOwner) {
          return "personnal:connections";
        } else if ((url.contains("activities")) && isOwner) {
          return "personnal:activities";
        } else if ((url.contains("notifications")) && isOwner) {
          return "personnal:notifications";
        } else if (url.contains("all-spaces")) {
          return "personnal:all-spaces";
        } else if ((nav != null) && (nav.equals("home"))) {
          if ((SiteKey.portal(getCurrentPortal()) != null) && (SiteKey.portal(getCurrentPortal()).getName().equals("intranet"))) {
            return "Company Context Home";
          }
        } else if ((nav != null) && (nav.equals("calendar"))) {
          return "Company Context Calendar";
        } else if ((nav != null) && (nav.equals("forum"))) {
          return "Company Context Forum";
        } else if ((nav != null) && (nav.equals("wiki"))) {
          return "Company Context Wiki";
        } else if ((nav != null) && (nav.equals("tasks"))) {
          return "Company Context Tasks";
        } else if ((nav != null) && (nav.equals("documents"))) {
          return "Company Context Documents";
        } else if ((nav != null) && ((nav.equals("FAQ")) || (nav.equals("answers")))) {
          return "Company Context FAQ:Answers";
        } else if ((nav != null) && (nav.equals("connexions"))) {
          return "Company Context Connections";
        }
      }
      return DEFAULT_HELP_ID;
    } catch (Exception E) {
      LOG.warn("Can not load the currentNavigation ", E);
      return null;
    }
  }

  public static String getCurrentPortal() {
    return Util.getPortalRequestContext().getPortalOwner();
  }

  public static UserNode getSelectedPageNode() throws Exception {
    return Util.getUIPortal().getSelectedUserNode();
  }
}
