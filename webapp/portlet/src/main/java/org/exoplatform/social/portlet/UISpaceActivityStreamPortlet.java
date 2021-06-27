/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.portlet;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UISpaceActivityPortlet.java
 * <br>
 * Displaying space activities and its member's posts
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Apr 6, 2010
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  events = {
    @EventConfig(listeners = UISpaceActivityStreamPortlet.SwitchStreamActionListener.class)
  }
)
public class UISpaceActivityStreamPortlet extends UIPortletApplication {
  private ExoFeatureService featureService;
  private Space space;
  private UISpaceActivitiesDisplay uiDisplaySpaceActivities;

  private boolean useNewStream;

  /**
   * constructor
   */
  public UISpaceActivityStreamPortlet() throws Exception {
    addChild(UIComposer.class, null, null);

    uiDisplaySpaceActivities = addChild(UISpaceActivitiesDisplay.class, null, null);
    space = getSpaceService().getSpaceByUrl(Utils.getSpaceUrlByContext());
    uiDisplaySpaceActivities.setSpace(space);
    addChild(PopupContainer.class, null, "HiddenContainer_" + hashCode());

    this.featureService = getApplicationComponent(ExoFeatureService.class);
    this.useNewStream = isNewStreamFeatureEnabled();
  }

  public void switchStream() {
    useNewStream = !useNewStream;
  }

  public boolean isNewStreamFeatureEnabled() {
    return this.featureService.isFeatureActiveForUser("NewActivityStream",
                                                      ConversationState.getCurrent().getIdentity().getUserId());
  }

  public boolean isUseNewStream() {
    return useNewStream;
  }

  @Override
  public String getTemplate() {
    return useNewStream ? "war:/groovy/social/portlet/NewActivityStreamPortlet.gtmpl"
                        : "war:/groovy/social/portlet/UISpaceActivityStreamPortlet.gtmpl";
  }

  public SpaceService getSpaceService() {
    return getApplicationComponent(SpaceService.class);
  }

  public Space getSpace() {
    return space;
  }

  public void setSpace(Space space) {
    this.space = space;
  }

  /**
   * resets to reload all activities
   *
   * @throws Exception
   */
  public void refresh() throws Exception {
    space = getSpaceService().getSpaceByUrl(Utils.getSpaceUrlByContext());
    uiDisplaySpaceActivities.setSpace(space);
  }

  public static class SwitchStreamActionListener extends EventListener<UISpaceActivityStreamPortlet> {
    @Override
    public void execute(Event<UISpaceActivityStreamPortlet> event) throws Exception {
      UISpaceActivityStreamPortlet spaceActivityStreamPortlet = event.getSource();
      spaceActivityStreamPortlet.switchStream();
      event.getRequestContext().addUIComponentToUpdateByAjax(spaceActivityStreamPortlet);
    }
  }
}
