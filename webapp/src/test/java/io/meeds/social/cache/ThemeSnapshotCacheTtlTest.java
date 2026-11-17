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
package io.meeds.social.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import io.meeds.portal.theme.storage.ThemeStorage;

/**
 * Pins the life of the {@link ThemeStorage#SNAPSHOT_CACHE} cache.
 * <p>
 * A write evicts the cache on its own node only: the other nodes of a cluster
 * see a publication when their entry expires, so this property is the 60
 * seconds bound of that propagation (the cross-node bound of the theme
 * management note, section 3.1). The kernel cache adapter reads it under
 * {@code meeds.cache.<cache name>.ttl} and, when it is missing or the key
 * drifts, silently keeps the kernel default life, which no other test
 * notices: the cache tests run on a manager without expiry.
 */
class ThemeSnapshotCacheTtlTest {

  private static final String SOCIAL_PROPERTIES = "social.properties";

  @Test
  void themeSnapshotCacheExpiresAfterSixtySeconds() throws IOException {
    Properties properties = new Properties();
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(SOCIAL_PROPERTIES)) {
      assertNotNull(in, SOCIAL_PROPERTIES + " is not on the classpath");
      properties.load(in);
    }
    assertEquals("60",
                 properties.getProperty("meeds.cache." + ThemeStorage.SNAPSHOT_CACHE + ".ttl"),
                 "The theme snapshot cache must expire after 60 seconds so that other cluster nodes read a publication");
  }

}
