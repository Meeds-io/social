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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.common.xmlprocessor.XMLProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class OSHtmlSanitizerProcessor extends BaseActivityProcessorPlugin {
  private XMLProcessor xmlProcessor;

  public OSHtmlSanitizerProcessor(InitParams params, XMLProcessor xmlProcessor) {
    super(params);
    this.xmlProcessor = xmlProcessor;
  }

  public void processActivity(ExoSocialActivity activity) {
    activity.setTitle((String) xmlProcessor.process(activity.getTitle()));
    activity.setBody((String) xmlProcessor.process(activity.getBody()));

    Map<String, String> templateParams = activity.getTemplateParams();

    List<String> templateParamKeys = getTemplateParamKeysToFilter(activity);
    for(String key : templateParamKeys){
      templateParams.put(key, (String) xmlProcessor.process(templateParams.get(key)));
    }
  }

}
