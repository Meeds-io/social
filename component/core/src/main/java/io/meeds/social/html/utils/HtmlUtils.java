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
package io.meeds.social.html.utils;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.localization.LocalizationFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

import io.meeds.social.html.model.HtmlProcessorContext;
import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.service.HtmlProcessorService;
import io.meeds.social.html.service.HtmlTransformerService;

public class HtmlUtils {

  private static final Log              LOG = ExoLogger.getLogger(HtmlUtils.class);

  private static HtmlProcessorService   htmlProcessorService;

  private static HtmlTransformerService htmlTransformerService;

  private HtmlUtils() {
    // Utils class
  }

  /**
   * Process the HTML input into content before storing in database
   * 
   * @param html HTML input
   * @param context HTML processor context of type {@link HtmlProcessorContext}
   * @return processed HTML output
   */
  public static String process(String html, HtmlProcessorContext context) {
    HtmlProcessorService service = getHtmlProcessorService();
    if (service == null || StringUtils.isBlank(html)) {
      return html;
    } else {
      try {
        if (context == null) {
          context = new HtmlProcessorContext();
        }
        if (context.getUserIdentity() == null && ConversationState.getCurrent() != null) {
          context.setUserIdentity(ConversationState.getCurrent().getIdentity());
        }
        return service.process(html, context);
      } catch (Exception e) {
        LOG.warn("Error while processing html content: {}. The error is considered as non-blocker, thus continue processing",
                 html,
                 e);
        return html;
      }
    }
  }

  /**
   * Transforms the HTML input into content ready to display
   * 
   * @param html HTML input
   * @param context HTML transformation context of type
   *          {@link HtmlTransformerContext}
   * @return transformed HTML output
   */
  public static String transform(String html, HtmlTransformerContext context) {
    if (StringUtils.isBlank(html)) {
      return html;
    }
    HtmlTransformerService service = getHtmlTransformerService();
    if (service == null) {
      return html;
    } else {
      try {
        if (context == null) {
          context = new HtmlTransformerContext();
        }
        setUserIdentity(context);
        setUserLocale(context);
        return service.transform(html, context);
      } catch (Exception e) {
        LOG.warn("Error while transforming html content: {}. The error is considered as non-blocker, thus continue transformation",
                 html,
                 e);
        return html;
      }
    }
  }

  private static void setUserIdentity(HtmlTransformerContext context) {
    if (context.getUserIdentity() == null) {
      if (ConversationState.getCurrent() == null) {
        context.setSystem(true);
      } else if (!context.isSystem()) {
        context.setUserIdentity(ConversationState.getCurrent().getIdentity());
      }
    }
  }

  private static void setUserLocale(HtmlTransformerContext context) {
    if (context.getLocale() == null) {
      context.setLocale(LocalizationFilter.getCurrentLocale());
    }
  }

  private static HtmlProcessorService getHtmlProcessorService() {
    if (htmlProcessorService == null && PortalContainer.getInstanceIfPresent() != null) {
      htmlProcessorService = PortalContainer.getInstance().getComponentInstanceOfType(HtmlProcessorService.class);
    }
    return htmlProcessorService;
  }

  private static HtmlTransformerService getHtmlTransformerService() {
    if (htmlTransformerService == null && PortalContainer.getInstanceIfPresent() != null) {
      htmlTransformerService = PortalContainer.getInstance().getComponentInstanceOfType(HtmlTransformerService.class);
    }
    return htmlTransformerService;
  }

}
