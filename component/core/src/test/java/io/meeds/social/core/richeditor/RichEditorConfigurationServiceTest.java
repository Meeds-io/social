/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.core.richeditor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class RichEditorConfigurationServiceTest extends AbstractCoreTest {

  public void testGetRichEditorConfiguration() {
    RichEditorConfigurationService richEditorConfigurationService = getContainer().getComponentInstanceOfType(RichEditorConfigurationService.class);
    assertEquals("// Test Default JS\n", richEditorConfigurationService.getRichEditorConfiguration(""));
    assertEquals("// Test Default JS\n", richEditorConfigurationService.getRichEditorConfiguration(null));
    assertEquals("// Test Default JS\n", richEditorConfigurationService.getRichEditorConfiguration("not-extended-config"));
    assertEquals("""
        // Test Default JS
        // Test Extension JS
        """.trim(), richEditorConfigurationService.getRichEditorConfiguration("test-extension").trim());
  }

  public void testGetRichEditorConfigurationWithJSVariables() throws IOException, Exception {
    RichEditorConfigurationService richEditorConfigurationService = getContainer().getComponentInstanceOfType(RichEditorConfigurationService.class);
    String jsConfiguration = richEditorConfigurationService.getRichEditorConfiguration("js-variable-test");
    jsConfiguration = jsConfiguration.replace("// Test Default JS\n", "");
    jsConfiguration = jsConfiguration.replaceAll("\n", "");
    ConfigurationManager configurationManager = getContainer().getComponentInstanceOfType(ConfigurationManager.class);
    String expectedConfiguration = IOUtils.toString(configurationManager.getInputStream("jar:/ckeditor-config-js-variable-test.js"), StandardCharsets.UTF_8);
    assertEquals(expectedConfiguration, jsConfiguration);
  }

  public void testGetRichEditorConfigurationWithJavaVariables() {
    String value = "test-value";
    System.setProperty("io.meeds.test.key", value);

    RichEditorConfigurationService richEditorConfigurationService = getContainer().getComponentInstanceOfType(RichEditorConfigurationService.class);
    String jsConfiguration = richEditorConfigurationService.getRichEditorConfiguration("java-variable-test");
    jsConfiguration = jsConfiguration.replace("// Test Default JS\n", "");
    jsConfiguration = jsConfiguration.replaceAll("\n", "");
    assertEquals("const javaProp = '" + value + "';", jsConfiguration);
  }

}
