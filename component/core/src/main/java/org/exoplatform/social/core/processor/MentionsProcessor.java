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
package org.exoplatform.social.core.processor;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.service.LinkProvider;

/**
 * A processor that substitute @username expressions by a link on the user profile
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MentionsProcessor extends BaseActivityProcessorPlugin {

  private static final Pattern pattern = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");

  private UserPortalConfigService userPortalConfigService;

  public MentionsProcessor(InitParams params, UserPortalConfigService userPortalConfigService) {
    super(params);
    this.userPortalConfigService = userPortalConfigService;
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null) {
      String portalOwner = null;
      try{
        portalOwner = Util.getPortalRequestContext().getPortalOwner();
      } catch (Exception e){
        //default value for testing and social
        portalOwner = userPortalConfigService.getDefaultPortal();
      }
      activity.setTitle(substituteUsernames(portalOwner, activity.getTitle()));
      activity.setBody(substituteUsernames(portalOwner, activity.getBody()));
      Map<String, String> templateParams = activity.getTemplateParams();
      List<String> templateParamKeys = getTemplateParamKeysToFilter(activity);
      for(String key : templateParamKeys){
        templateParams.put(key, substituteUsernames(portalOwner, templateParams.get(key)));
      }
    }
  }

  /*
   * Substitute @username expressions by full user profile link
   */
  private String substituteUsernames(String portalOwner, String message) {
    if (message == null || message.trim().isEmpty()) {
      return message;
    }
    //
    Matcher matcher = pattern.matcher(message);

    // Replace all occurrences of pattern in input
    StringBuffer buf = new StringBuffer();
    while (matcher.find()) {
      // Get the match result
      String username = matcher.group().substring(1);
      if (username == null || username.isEmpty()) {
        continue;
      }
      Identity identity = LinkProvider.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
      if (identity == null || identity.isDeleted() || !identity.isEnable()) {
        continue;
      }
      try {
        username = LinkProvider.getProfileLink(username, portalOwner);
      } catch (Exception e) {
        continue;
      }
      // Insert replacement
      if (username != null) {
        matcher.appendReplacement(buf, username);
      }
    }
    if (buf.length() > 0) {
      matcher.appendTail(buf);
      return buf.toString();
    }
    return message;
  }
}
