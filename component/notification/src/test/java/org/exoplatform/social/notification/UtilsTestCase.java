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
package org.exoplatform.social.notification;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.CommonsUtils;

public class UtilsTestCase extends TestCase {
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    System.setProperty(CommonsUtils.CONFIGURED_DOMAIN_URL_KEY, "http://exoplatform.com");
  }

  public void testProcessLinkInActivityTitle() throws Exception {
    String title = "<a href=\"www.yahoo.com\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\">Hotmail Site</a>";
    title = Utils.processLinkTitle(title);
    assertEquals("<a href=\"www.yahoo.com\" style=\"color: #2f5e92; text-decoration: none;\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\" style=\"color: #2f5e92; text-decoration: none;\">Hotmail Site</a>", title);
    
    title = "Shared a document <a href=\"portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\">New design. eXo in Smart Watch.jpg</a>";
    assertEquals("Shared a document <a href=\"http://exoplatform.com/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\" style=\"color: #2f5e92; text-decoration: none;\">New design. eXo in Smart Watch.jpg</a>", Utils.processLinkTitle(title));
    title = "Shared a document <a href=\"/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\">New design. eXo in Smart Watch.jpg</a>";
    assertEquals("Shared a document <a href=\"http://exoplatform.com/portal/rest/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\" style=\"color: #2f5e92; text-decoration: none;\">New design. eXo in Smart Watch.jpg</a>", Utils.processLinkTitle(title));
  }
}
