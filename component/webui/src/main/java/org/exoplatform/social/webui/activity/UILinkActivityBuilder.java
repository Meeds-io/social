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
package org.exoplatform.social.webui.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;

public class UILinkActivityBuilder extends BaseUIActivityBuilder {
  private static final Log LOG = ExoLogger.getLogger(UILinkActivityBuilder.class);
  
  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    UILinkActivity uiLinkActivity = (UILinkActivity) uiActivity;
    
    if (activity.getTemplateParams() == null) {
      saveToNewDataFormat(activity);
    }
    
    Map<String, String> templateParams = activity.getTemplateParams();
    if (activity.getTemplateParams().get(UILinkActivity.MESSAGE) != null) {
      uiLinkActivity.setMessage(templateParams.get(UILinkActivity.MESSAGE));
    }
    uiLinkActivity.setDefaultTitle(templateParams.get(UILinkActivity.DEFAULT_TITLE));
    uiLinkActivity.setLinkSource(templateParams.get(UILinkActivity.LINK_PARAM));
    uiLinkActivity.setLinkTitle(templateParams.get(UILinkActivity.TITLE_PARAM));
    uiLinkActivity.setLinkImage(templateParams.get(UILinkActivity.IMAGE_PARAM));
    uiLinkActivity.setLinkDescription(templateParams.get(UILinkActivity.DESCRIPTION_PARAM));
    uiLinkActivity.setLinkComment(templateParams.get(UILinkActivity.COMMENT_PARAM));
    uiLinkActivity.setEmbedHtml(templateParams.get(UILinkActivity.HTML_PARAM));
  }

  private void saveToNewDataFormat(ExoSocialActivity activity) {
    try {
      JSONObject jsonObj = new JSONObject(activity.getTitle());

      StringBuilder linkTitle = new StringBuilder("Shared a link:");
      linkTitle.append(" <a href=\"${").append(UILinkActivity.LINK_PARAM).append("}\">${")
          .append(UILinkActivity.TITLE_PARAM).append("} </a>");
      activity.setTitle(linkTitle.toString());
      
      Map<String, String> templateParams = new HashMap<String, String>();
      templateParams.put(UILinkActivity.LINK_PARAM, jsonObj.getString(UILinkActivity.LINK_PARAM));
      templateParams.put(UILinkActivity.TITLE_PARAM, jsonObj.getString(UILinkActivity.TITLE_PARAM));
      templateParams.put(UILinkActivity.IMAGE_PARAM, jsonObj.getString(UILinkActivity.IMAGE_PARAM));
      templateParams.put(UILinkActivity.DESCRIPTION_PARAM, jsonObj.getString(UILinkActivity.DESCRIPTION_PARAM));
      templateParams.put(UILinkActivity.COMMENT_PARAM, jsonObj.getString(UILinkActivity.COMMENT_PARAM));
      activity.setTemplateParams(templateParams);
      
      CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(activity);
    } catch (JSONException je) {
      LOG.error("Error with activity's title data");
    } catch (ActivityStorageException ase) {
      LOG.warn("Could not save new data format for document activity.", ase);
    } catch (Exception e) {
      LOG.error("Unknown error  to save document activity.", e);
    }
  }
}
