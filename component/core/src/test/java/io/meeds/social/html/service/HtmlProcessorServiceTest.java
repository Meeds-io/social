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
package io.meeds.social.html.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.meeds.social.AbstractSpringConfigurationTest;
import io.meeds.social.html.model.HtmlProcessorContext;
import io.meeds.social.html.plugin.HtmlProcessorPlugin;
import io.meeds.social.html.utils.HtmlUtils;

public class HtmlProcessorServiceTest extends AbstractSpringConfigurationTest {

  private static final String  CONTENT_NO_MATCH     = "Content";

  private static final String  CONTENT_MATCH        = """
      Content1
      test:556
      """;

  private static final String  CONTENT_MATCH_RESULT = """
      Content1
      Content2
      """;

  @Autowired
  private HtmlProcessorService htmlProcessorService;

  @Test
  public void processContentNoMatch() {
    assertEquals(CONTENT_NO_MATCH, HtmlUtils.process(CONTENT_NO_MATCH, null));
  }

  @Test
  public void processContentWithContext() {
    assertEquals(CONTENT_NO_MATCH, HtmlUtils.process(CONTENT_NO_MATCH, new HtmlProcessorContext()));
  }

  @Test
  public void processContent() {
    HtmlProcessorTest plugin = new HtmlProcessorTest();
    try {
      htmlProcessorService.addPlugin(plugin);
      assertEquals(CONTENT_MATCH_RESULT, HtmlUtils.process(CONTENT_MATCH, new HtmlProcessorContext()));
    } finally {
      htmlProcessorService.removePlugin(plugin);
    }
  }

  public static class HtmlProcessorTest implements HtmlProcessorPlugin {
    @Override
    public String process(String html, HtmlProcessorContext context) {
      return html.replace("test:556", "Content2");
    }
  }

}
