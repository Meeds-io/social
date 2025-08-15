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
package org.exoplatform.social.core.processor;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link OSHtmlSanitizerProcessor}.
 *
 */
public class OSHtmlSanitizerProcessorTest extends AbstractCoreTest {

  private OSHtmlSanitizerProcessor processor;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    processor = (OSHtmlSanitizerProcessor) PortalContainer.getInstance().
                                           getComponentInstanceOfType(OSHtmlSanitizerProcessor.class);

  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }


  public void testProcessActivity() throws Exception {
    System.setProperty("gatein.email.domain.url", "test.com");
    try {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      String sample = "this is a <strong> tag to keep</strong>";
      activity.setTitle(sample);
      activity.setBody(sample);
      processor.processActivity(activity);
  
      assertEquals(sample, activity.getTitle());
      assertEquals(sample, activity.getBody());
  
      // tags with attributes
      sample = "text <a href='test.com'>bar</a> zed";
  
      activity.setTitle(sample);
      processor.processActivity(activity);
  
      assertEquals("text <a href=\"test.com\" rel=\"nofollow\" target=\"_self\">bar</a> zed", activity.getTitle());
  
      // only with open tag
      sample = "<strong> only open!!!";
      activity.setTitle(sample);
      processor.processActivity(activity);
      assertEquals("<strong> only open!!!</strong>", activity.getTitle());
  
      // self closing tags
      sample = "<script href='#' />bar</a>";
      activity.setTitle(sample);
      processor.processActivity(activity);
      assertEquals("", activity.getTitle());
  
      // forbidden tag
      sample = "<script>foo</script>";
      activity.setTitle(sample);
      processor.processActivity(activity);
      assertEquals("", activity.getTitle());
  
      // embedded
      sample = "<span><strong>foo</strong>bar<script>zed</script></span>";
      activity.setTitle(sample);
      processor.processActivity(activity);
      assertEquals("<strong>foo</strong>bar", activity.getTitle().trim());
    } finally {
      System.clearProperty("gatein.email.domain.url");
    }
  }

}
