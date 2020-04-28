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
package org.exoplatform.social.core.space;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/**
 * Component plugin for configuring the template of the space.
 */
public class SpaceTemplateConfigPlugin extends BaseComponentPlugin {

  private static final String SPACE_TEMPLATE_PARAM_NAME = "template";

  private SpaceTemplate spaceTemplate;

  /**
   * Constructor with init params
   *
   * @param initParams
   */
  public SpaceTemplateConfigPlugin(InitParams initParams) {
    spaceTemplate = (SpaceTemplate) initParams.getObjectParam(SPACE_TEMPLATE_PARAM_NAME).getObject();
  }

  /**
   * Sets space template.
   *
   * @param template
   */
  public void setSpaceTemplate(SpaceTemplate template) {
    spaceTemplate = template;
  }

  /**
   * Gets space template.
   *
   * @return
   */
  public SpaceTemplate getSpaceTemplate() {
    return spaceTemplate;
  }
}

