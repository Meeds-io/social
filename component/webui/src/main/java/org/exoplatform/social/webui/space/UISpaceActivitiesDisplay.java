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
package org.exoplatform.social.webui.space;

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.AbstractActivitiesDisplay;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.activity.UIActivitiesLoader;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UISpaceActivitiesDisplay.java
 * <p>
 * Displays space activities and its member's activities
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Apr 6, 2010
 */
@ComponentConfig(
  template = "war:/groovy/social/webui/space/UISpaceActivitiesDisplay.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceActivitiesDisplay.RefreshStreamActionListener.class)
  }
)

public class UISpaceActivitiesDisplay extends AbstractActivitiesDisplay {
  static private final Log LOG = ExoLogger.getLogger(UISpaceActivitiesDisplay.class);

  private static final String ACTIVITIES_PER_PAGE_KEY = "social.activities.per.page";
  private static int ACTIVITY_PER_PAGE = 10;
  private Space space;
  private UIActivitiesLoader activitiesLoader;

  /**
   * Constructor
   *
   * @throws Exception
   */
  public UISpaceActivitiesDisplay() throws Exception {
    ACTIVITY_PER_PAGE = Integer.valueOf(PrivilegedSystemHelper.getProperty(ACTIVITIES_PER_PAGE_KEY, "10").trim());
  }

  /**
   * Sets space to work with
   * @param space
   * @throws Exception
   */
  public void setSpace(Space space) throws Exception {
    this.space = space;
    init();
  }

  /**
   * Returns current space to work with
   * @return
   */
  public Space getSpace() {
    return space;
  }


  public UIActivitiesLoader getActivitiesLoader() {
    return activitiesLoader;
  }
  
  /**
   * Checks the Stream Context to make 
   * the decision Share button shows or not.
   * @return TRUE/FALSE
   */
  public boolean isSingleContext() {
    return (getActivitiesLoader().getPostContext() == UIComposer.PostContext.SINGLE);
  }

  /**
   * initialize
   * @throws Exception
   */
  public void init() throws Exception {
    if (space == null) {
      LOG.warn("space is null! Can not display spaceActivites");
      return;
    }

    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, 
                                                                            space.getPrettyName(), false);
    
    removeChild(UIActivitiesLoader.class);
    activitiesLoader = addChild(UIActivitiesLoader.class, null, "UIActivitiesLoader");
    activitiesLoader.setSpace(space);
    activitiesLoader.setPostContext(PostContext.SPACE);
    activitiesLoader.setLoadingCapacity(ACTIVITY_PER_PAGE);
    activitiesLoader.setActivityListAccess(Utils.getActivityManager().getActivitiesOfSpaceWithListAccess(spaceIdentity));
    activitiesLoader.init();
    activitiesLoader.getChild(UIActivitiesContainer.class).setRenderFull(isRenderFull(), true);
  }

  public static class RefreshStreamActionListener extends EventListener<UISpaceActivitiesDisplay> {
    public void execute(Event<UISpaceActivitiesDisplay> event) throws Exception {
     UISpaceActivitiesDisplay uiSpaceActivities = event.getSource();
     uiSpaceActivities.init();
     event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceActivities);
   }
  }
}
