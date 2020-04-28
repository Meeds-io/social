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
package org.exoplatform.social.core.profile.settings;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

/**
 * Adds one or several IM types in declarative form: by ID and name, where ID configured as value-param name
 * and type name as the value.
 * 
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: IMTypesPlugin.java 00000 Apr 27, 2017 pnedonosko $
 */
public class IMTypesPlugin extends BaseComponentPlugin {

  /** The types. */
  protected List<IMType> types = new LinkedList<>();

  /**
   * Instantiates a new simple IM type plugin.
   *
   * @param initParams the init params
   */
  public IMTypesPlugin(InitParams initParams) {
    Iterator<ValueParam> imtIter = initParams.getValueParamIterator();
    while (imtIter.hasNext()) {
      ValueParam imt = imtIter.next();
      types.add(new IMType(imt.getName(), imt.getValue()));
    }
  }

  /**
   * Gets the types.
   *
   * @return the types
   */
  public Collection<IMType> getTypes() {
    return types;
  }

}
