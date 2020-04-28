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
package org.exoplatform.social.user.portlet;

import org.exoplatform.social.user.form.UIEditUserProfileForm;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "war:/groovy/social/portlet/user/UIEditUserProfilePortlet.gtmpl"
)
public class UIEditUserProfilePortlet extends UIAbstractUserPortlet {

  public UIEditUserProfilePortlet() throws Exception {
    addChild(UIEditUserProfileForm.class, null, null);
    addChild(PopupContainer.class, null, "AvatarPopupContainer");
  }

  @Override
  public void beforeProcessRender(WebuiRequestContext context) {
    super.beforeProcessRender(context);
    //
    RequireJS requireJs = context.getJavascriptManager().getRequireJS();
    requireJs.require("SHARED/user-profile", "userprofile").addScripts("userprofile.init('" + getId() + "');");
  }
  
}
