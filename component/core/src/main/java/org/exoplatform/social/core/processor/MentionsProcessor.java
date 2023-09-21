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
package org.exoplatform.social.core.processor;

import java.util.List;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.utils.MentionUtils;

/**
 * A processor that substitute @username expressions by a link on the user profile
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class MentionsProcessor extends BaseActivityProcessorPlugin {

  private UserPortalConfigService userPortalConfigService;

  public MentionsProcessor(InitParams params, UserPortalConfigService userPortalConfigService) {
    super(params);
    this.userPortalConfigService = userPortalConfigService;
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null) {
      String portalOwner = null;
      try {
        portalOwner = Util.getPortalRequestContext().getPortalOwner();
      } catch (Exception e){
        //default value for testing and social
        portalOwner = userPortalConfigService.getDefaultPortal();
      }
      activity.setTitle(MentionUtils.substituteUsernames(portalOwner, activity.getTitle()));
      activity.setBody(MentionUtils.substituteUsernames(portalOwner, activity.getBody()));
      Map<String, String> templateParams = activity.getTemplateParams();
      List<String> templateParamKeys = getTemplateParamKeysToFilter(activity);
      for(String key : templateParamKeys){
        templateParams.put(key, MentionUtils.substituteUsernames(portalOwner, templateParams.get(key)));
      }
      if (templateParams.containsKey("comment")) {
        templateParams.put("comment", MentionUtils.substituteUsernames(portalOwner, templateParams.get("comment")));
      }
      if (templateParams.containsKey("default_title")) {
        templateParams.put("default_title", MentionUtils.substituteUsernames(portalOwner, templateParams.get("default_title")));
      }
    }
  }

}
