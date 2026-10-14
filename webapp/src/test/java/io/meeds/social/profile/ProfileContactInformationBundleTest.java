/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * The contact information of a profile names its properties from the
 * {@code profileContactInformation.<propertyName>} keys, and shows the raw
 * property name when the key is missing.
 */
class ProfileContactInformationBundleTest {

  private static final Path BUNDLE = Path.of("src/main/resources/locale/portlet/social/ProfileContactInformation_en.properties");

  /**
   * The manager property is labelled as a field, not by its lowercase
   * property name.
   *
   * @throws IOException when the bundle can't be read
   */
  @Test
  void theManagerPropertyHasItsLabel() throws IOException {
    Properties bundle = new Properties();
    try (InputStream input = Files.newInputStream(BUNDLE)) {
      bundle.load(input);
    }
    assertEquals("Manager", bundle.getProperty("profileContactInformation.manager"));
  }
}
