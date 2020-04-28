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
package org.exoplatform.social.user;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.user.portlet.UIRecentActivitiesPortlet;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

import java.util.Locale;

@ComponentConfig(
   template = "war:/groovy/social/webui/profile/UIRecentActivity.gtmpl"
)
public class UIRecentActivity extends UIContainer {
  public static String COMPONENT_ID = "Activity";
  private ExoSocialActivity activity = null;

  public UIRecentActivity() {
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context);
    ((UIRecentActivitiesPortlet) getParent()).initProfilePopup();
    ((UIRecentActivitiesPortlet) getParent()).initSpacePopup();
  }

  public static String buildComponentId(String activityId) {
    return COMPONENT_ID + activityId;
  }

  protected ExoSocialActivity getActivity() {
    if (activity.getTitleId() != null) {
      WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
      I18NActivityProcessor i18NActivityProcessor = getApplicationComponent(I18NActivityProcessor.class);
      Locale userLocale = requestContext.getLocale();
      activity = i18NActivityProcessor.process(activity, userLocale);
    }
    return this.activity;
  }

  public void setActivity(ExoSocialActivity activity) {
    this.activity = activity;
  }
}
