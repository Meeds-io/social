/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.permlink.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.plugin.PermanentLinkPlugin;

/**
 * A plugin to generate a permanent link to a given activity/comment/reply
 */
public class ActivityPermanentLinkPlugin implements PermanentLinkPlugin {

  public static final String      OBJECT_TYPE = "activity";

  public static final String      URL_FORMAT  = "/portal/%s/activity?id=%s";

  private ActivityManager         activityManager;

  private UserPortalConfigService portalConfigService;

  public ActivityPermanentLinkPlugin(ActivityManager activityManager,
                                     UserPortalConfigService portalConfigService) {
    this.activityManager = activityManager;
    this.portalConfigService = portalConfigService;
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canAccess(PermanentLinkObject object, Identity identity) throws ObjectNotFoundException {
    if (identity == null || identity.getUserId() == null) {
      return false;
    } else {
      String activtyId = object.getObjectId();
      ExoSocialActivity activity = activityManager.getActivity(activtyId);
      if (activity == null) {
        throw new ObjectNotFoundException(String.format("Activity with id %s not found", object.getObjectId()));
      }
      return activityManager.isActivityViewable(activity, identity);
    }
  }

  @Override
  public String getDirectAccessUrl(PermanentLinkObject object) throws ObjectNotFoundException {
    return String.format(URL_FORMAT, portalConfigService.getMetaPortal(), object.getObjectId().replace("comment", ""));
  }

}
