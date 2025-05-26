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

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import javax.portlet.*;
import java.io.IOException;

public class SpaceCreationPortlet extends GenericDispatchedViewPortlet {

  private UserACL userAcl;

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifySettings()) {
      throw new PortletException("User is not allowed to save settings");
    }

    PortletPreferences preferences = request.getPreferences();
    String settings = request.getParameter("settings");
    preferences.setValue("settings", settings);
    preferences.store();
  }

  private Identity getCurrentIdentity() {
    return ConversationState.getCurrent() == null ? null : ConversationState.getCurrent().getIdentity();
  }

  private boolean canModifySettings() {
    Page currentPage = getCurrentPage();
    return currentPage != null && getUserAcl().hasEditPermission(currentPage, getCurrentIdentity());
  }

  private UserACL getUserAcl() {
    if (userAcl == null) {
      userAcl = ExoContainerContext.getService(UserACL.class);
    }
    return userAcl;
  }

  private Page getCurrentPage() {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    return requestContext == null ? null : requestContext.getPage();
  }
}
