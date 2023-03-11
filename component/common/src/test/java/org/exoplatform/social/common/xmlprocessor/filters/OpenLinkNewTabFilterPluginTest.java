/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.common.xmlprocessor.filters;

import junit.framework.TestCase;
import org.exoplatform.social.common.xmlprocessor.Filter;

/**
 * @author <a href="mailto:tuyennt@test.com">Tuyen Nguyen The</a>.
 */
public class OpenLinkNewTabFilterPluginTest extends TestCase {
  public void testFilterLinkTag() {
    System.setProperty("gatein.email.domain.url", "test.com");
    Filter linkTagFilter = new OpenLinkNewTabFilterPlugin();
    assertEquals("This is <a href=\"http://test.com\" target=\"_self\">link</a>", linkTagFilter.doFilter("This is <a href=\"http://test.com\">link</a>"));
    assertEquals("This is <a href=\"http://test.com\" target=\"_self\">link</a> <a href=\"exoplatform.net\" target=\"_blank\">link2</a>",
              linkTagFilter.doFilter("This is <a href=\"http://test.com\">link</a> <a href=\"exoplatform.net\">link2</a>"));
    assertEquals("This is <a href=\"http://exoplatform.net\" target=\"_blank\">link</a>",
            linkTagFilter.doFilter("This is <a href=\"http://exoplatform.net\" target=\"_self\">link</a>"));
    assertEquals("This is <a href=\"http://test.com\" target=\"_self\">link</a>",
            linkTagFilter.doFilter("This is <a href=\"http://test.com\" target=\"_parent\">link</a>"));
    assertEquals("This is <a href=\"http://test.com\" target=\"_self\">link</a>",
            linkTagFilter.doFilter("This is <a href=\"http://test.com\" target=\"frame_name\">link</a>"));
    System.clearProperty("gatein.email.domain.url");
  }

  public void testShouldReturnNullWhenFilteringNullInput() {
    // Given
    OpenLinkNewTabFilterPlugin openLinkNewTabFilterPlugin = new OpenLinkNewTabFilterPlugin();

    // When
    Object filteredInput = openLinkNewTabFilterPlugin.doFilter(null);

    // Then
    assertNull(filteredInput);
  }
}
