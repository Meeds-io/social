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
package org.exoplatform.commons.search.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.test.mock.MockHttpServletRequest;


/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Mar 25, 2013  
 */
public class UnifiedSearchMockHttpServletRequest extends MockHttpServletRequest{

  public UnifiedSearchMockHttpServletRequest(String url,
                                             InputStream data,
                                             int length,
                                             String method,
                                             Map<String, List<String>> headers) {
    super(url, data, length, method, headers);
    // TODO Auto-generated constructor stub
  }
  
  
  /**
   * {@inheritDoc}
   */
  public String getServerName() {
    try {
     return super.getServerName();
    } catch (Exception e) { }
    return "localhost";
  }

  /**
   * {@inheritDoc}
   */
  public int getServerPort() {
    try {
     return super.getServerPort();
    } catch (Exception e) { }
    return 8080;
  }  
  

}
