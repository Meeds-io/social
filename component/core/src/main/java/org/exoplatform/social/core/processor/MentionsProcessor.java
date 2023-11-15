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

import org.apache.commons.collections.MapUtils;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.utils.MentionUtils;

/**
 * A processor that substitute @username expressions by a link on the user
 * profile
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class MentionsProcessor extends BaseActivityProcessorPlugin {

  private UserPortalConfigService userPortalConfigService;

  private IdentityManager         identityManager;

  public MentionsProcessor(UserPortalConfigService userPortalConfigService,
                           IdentityManager identityManager,
                           InitParams params) {
    super(params);
    this.userPortalConfigService = userPortalConfigService;
    this.identityManager = identityManager;
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null) {
      String portalOwner = userPortalConfigService.getDefaultPortal();
      activity.setTitle(MentionUtils.substituteUsernames(identityManager, portalOwner, activity.getTitle()));
      activity.setBody(MentionUtils.substituteUsernames(identityManager, portalOwner, activity.getBody()));
      Map<String, String> templateParams = activity.getTemplateParams();
      if (MapUtils.isNotEmpty(templateParams)) {
        List<String> templateParamKeys = getTemplateParamKeysToFilter(activity);
        for (String key : templateParamKeys) {
          templateParams.put(key, MentionUtils.substituteUsernames(identityManager, portalOwner, templateParams.get(key)));
        }
        if (templateParams.containsKey("comment")) {
          templateParams.put("comment", MentionUtils.substituteUsernames(identityManager, portalOwner, templateParams.get("comment")));
        }
        if (templateParams.containsKey("default_title")) {
          templateParams.put("default_title", MentionUtils.substituteUsernames(identityManager, portalOwner, templateParams.get("default_title")));
        }
      }
    }
  }

}
