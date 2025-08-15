/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

/**
 * A base plugin to configure {@link ActivityProcessor}s for
 * {@link org.exoplatform.social.core.manager.ActivityManager}.
 *
 */
public abstract class BaseActivityProcessorPlugin extends BaseComponentPlugin implements
    ActivityProcessor {
  protected int            priority;

  private static final Log LOG = ExoLogger.getLogger(BaseActivityProcessorPlugin.class);

  public static final String TEMPLATE_PARAM_TO_PROCESS = "registeredKeysForProcessor";
  public static final String TEMPLATE_PARAM_LIST_DELIM = "\\|";
  
  public BaseActivityProcessorPlugin(InitParams params) {
    try {
      priority = Integer.valueOf(params.getValueParam("priority").getValue());
    } catch (Exception e) {
      priority = 5; //default, it should be in range of 1-10
      LOG.warn("an <value-param> 'priority' of type int is recommanded for component " + getClass());
    }
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }
  
  /**
   * This method is helper for ActivityProcessor incase we want to get list of template params to be filter.
   * @param activity
   * @return
   */
  public List<String> getTemplateParamKeysToFilter(ExoSocialActivity activity){
    Map<String, String> templateParams = activity.getTemplateParams();
    ArrayList<String> keys = new ArrayList<String>();
    
    if(templateParams != null && templateParams.containsKey(TEMPLATE_PARAM_TO_PROCESS)){
      String[] templateParamKeys = activity.getTemplateParams().get(TEMPLATE_PARAM_TO_PROCESS).split(TEMPLATE_PARAM_LIST_DELIM);
      for(String key : templateParamKeys){
        if (key.endsWith("\\")) {
          key = key.replace("\\", "");
        }
        if(templateParams.containsKey(key.replace("\\", ""))){
          keys.add(key);
        }
      }
    }
    return keys;
  }

  public abstract void processActivity(ExoSocialActivity activity);
}
