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
package io.meeds.social.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.plugin.SpaceAclPlugin;

public class ActivityStreamPortlet extends GenericDispatchedViewPortlet {

  private static final String SETTING_NAME = "name";

  private UserACL             userAcl;

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    boolean canModifySettings = canModifySettings();
    request.setAttribute("canEdit", canModifySettings);
    super.doView(request, response);
  }

  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    PortletPreferences preferences = request.getPreferences();
    String settingName = preferences.getValue(SETTING_NAME, "");
    response.getWriter().write(settingName);
    response.setContentType("text/plain");
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifySettings()) {
      throw new PortletException("User is not allowed to save settings");
    }
    PortletPreferences preferences = request.getPreferences();
    String settings = request.getParameter("settings");
    preferences.setValue("settings", settings);
    preferences.store();
    response.setPortletMode(PortletMode.VIEW);
  }

  private boolean canModifySettings() {
    Space space = SpaceUtils.getSpaceByContext();
    Identity identity = getCurrentIdentity();
    if (getUserAcl().isAnonymousUser(identity)) {
      return false;
    } else if (space == null) {
      Page currentPage = getCurrentPage();
      return currentPage != null
             && getUserAcl().hasEditPermission(currentPage, identity);
    } else {
      return getUserAcl().hasEditPermission(SpaceAclPlugin.OBJECT_TYPE,
                                            space.getId(),
                                            identity);
    }
  }

  private Page getCurrentPage() {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    return requestContext == null ? null : requestContext.getPage();
  }

  private Identity getCurrentIdentity() {
    return ConversationState.getCurrent() == null ? null : ConversationState.getCurrent().getIdentity();
  }

  private UserACL getUserAcl() {
    if (userAcl == null) {
      userAcl = ExoContainerContext.getService(UserACL.class);
    }
    return userAcl;
  }

}
