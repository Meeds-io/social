/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.digest.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.services.resources.ResourceBundleService;

import io.meeds.commons.digest.plugin.DigestCategoryProvider;

@RunWith(MockitoJUnitRunner.class)
public class DigestCategoryLabelResolverTest {

  private static final String         BUNDLE_PATH = "locale.notification.template.Notification";

  private static final String         LABEL_KEY   = "digest.category.spaces";

  @Mock
  private PluginSettingService        pluginSettingService;

  @Mock
  private ResourceBundleService       resourceBundleService;

  private DigestCategoryLabelResolver labelResolver;

  @Before
  public void setUp() {
    labelResolver = new DigestCategoryLabelResolver(pluginSettingService, resourceBundleService);
    PluginConfig pluginConfig = new PluginConfig();
    pluginConfig.setBundlePath(BUNDLE_PATH);
    lenient().when(pluginSettingService.getPluginConfig("SpaceInvitationPlugin")).thenReturn(pluginConfig);
    lenient().when(resourceBundleService.getResourceBundle(BUNDLE_PATH, Locale.ENGLISH)).thenReturn(bundle("Spaces"));
    lenient().when(resourceBundleService.getResourceBundle(BUNDLE_PATH, Locale.FRENCH)).thenReturn(bundle("Espaces"));
  }

  /**
   * The digest REST services are served by the social webapp, which doesn't
   * fill the portal locale of the thread: reading the language anywhere else
   * than in the request would serve English to everybody.
   */
  @Test
  public void testLabelIsTranslatedInTheLanguageOfTheUser() {
    assertEquals("Espaces", labelResolver.getLabel(category(), Locale.FRENCH));
    assertEquals("Spaces", labelResolver.getLabel(category(), Locale.ENGLISH));
  }

  @Test
  public void testLabelFallsBackToEnglishWithoutLanguage() {
    assertEquals("Spaces", labelResolver.getLabel(category(), null));
  }

  @Test
  public void testLabelFallsBackToTheCategoryIdWhenNoPluginIsRegistered() {
    when(pluginSettingService.getPluginConfig("SpaceInvitationPlugin")).thenReturn(null);
    assertEquals("spaces", labelResolver.getLabel(category(), Locale.FRENCH));
  }

  @Test
  public void testLabelFallsBackToTheCategoryIdWhenNoBundleHoldsIt() {
    when(resourceBundleService.getResourceBundle(BUNDLE_PATH, Locale.FRENCH)).thenReturn(bundle(null));
    assertEquals("spaces", labelResolver.getLabel(category(), Locale.FRENCH));
  }

  private ResourceBundle bundle(String label) {
    return new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return label == null ? new Object[0][0] : new Object[][] { { LABEL_KEY, label } };
      }
    };
  }

  private DigestCategoryProvider category() {
    return new DigestCategoryProvider() {
      @Override
      public String getId() {
        return "spaces";
      }

      @Override
      public String getLabelKey() {
        return LABEL_KEY;
      }

      @Override
      public int getOrder() {
        return 10;
      }

      @Override
      public List<String> getPluginIds() {
        return List.of("SpaceInvitationPlugin");
      }
    };
  }

}
