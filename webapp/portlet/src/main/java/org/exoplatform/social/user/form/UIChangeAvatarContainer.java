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
package org.exoplatform.social.user.form;

import java.io.Writer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.user.portlet.UserProfileHelper;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputSet;

@ComponentConfig(
  events = {
    @EventConfig(listeners = UIChangeAvatarContainer.ChangeAvatarActionListener.class)
  }
)
public class UIChangeAvatarContainer extends UIFormInputSet {
  
  public UIChangeAvatarContainer() throws Exception {
    
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    Profile ownerProfile = Utils.getViewerIdentity(true).getProfile();
    String fullName = StringEscapeUtils.escapeHtml(ownerProfile.getFullName());
    String avatarURL = ownerProfile.getAvatarUrl();
    if(StringUtils.isBlank(avatarURL)) {
      avatarURL = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
    }
    Writer writer = context.getWriter();
    writer.append("<div class=\"uiRelationshipAction\" id=\"").append(getId()).append("\">");
    writer.append("  <div class=\"avatarLarge\">")
          .append("    <img title=\"").append(fullName).append("\" alt=\"").append(fullName).append("\" src=\"").append(avatarURL)
          .append("?").append(String.valueOf(ownerProfile.getAvatarLastUpdated()))
          .append("\"/>")
          .append("  </div>")
          .append("  <div class=\"btn btn-mini changeAvatar\" onclick=\"").append(event("ChangeAvatar")).append("\">")
          .append(UserProfileHelper.getLabel(context, "UIChangeAvatarContainer.label.ChangeAvatar"))
          .append("  </div>");
    writer.append("</div>");
  }
 
  public static class ChangeAvatarActionListener extends EventListener<UIChangeAvatarContainer> {
    @Override
    public void execute(Event<UIChangeAvatarContainer> event) throws Exception {
      UIChangeAvatarContainer avatarContainer = event.getSource();
      PopupContainer uiPopup = avatarContainer.getAncestorOfType(UIPortletApplication.class).getChild(PopupContainer.class);
      uiPopup.activate(UIAvatarUploader.class, 500, "UIWindowAvatarUploader");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
}
