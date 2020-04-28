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
package org.exoplatform.social.portlet;

import javax.portlet.PortletRequest;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;

/**
 * Renders groovy templates by introduce template and windowId only.
 *
 */
@ComponentConfig()
public class UIGroovyPortlet extends UIPortletApplication {
  /** DEFAULT TEMPLATE. */
  private String DEFAULT_TEMPLATE = "war:/groovy/social/portlet/UIGroovyPortlet.gtmpl" ;

  /** Stores template information. */
  private String template_ ;

  /** Store window information. */
  private String windowId ;

  /**
   * Gets and initialize variable.<br>
   *
   * @throws Exception
   */
  public UIGroovyPortlet() throws Exception {
    PortletRequestContext context = (PortletRequestContext)  WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = context.getRequest() ;
    template_ =  prequest.getPreferences().getValue("template", DEFAULT_TEMPLATE) ;
    windowId = prequest.getWindowID() ;
  }

  /**
   * Gets window id.
   *
   * @return id
   *         The id of window.
   */
  public String getId() { return windowId + "-portlet" ; }

  /**
   * Gets template.
   *
   * @return template
   *         The template to be rendered.
   */
  public String getTemplate() {  return template_ ;  }

  /**
   * Gets view mode.<br>
   *
   * @return view mode.
   */
  public UIComponent getViewModeUIComponent() { return null; }

}
