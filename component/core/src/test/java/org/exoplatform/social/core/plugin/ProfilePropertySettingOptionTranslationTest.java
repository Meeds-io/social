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
package org.exoplatform.social.core.plugin;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml") })
public class ProfilePropertySettingOptionTranslationTest extends AbstractKernelTest {

  private TranslationService translationService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    translationService = getContainer().getComponentInstanceOfType(TranslationService.class);
    begin();
  }

  public void testTranslatePropertyOption() throws ObjectNotFoundException {

    Map<Locale, String> labels = new HashMap<>();
    labels.put(Locale.US, "option en");
    labels.put(Locale.FRANCE, "option fr");
    translationService.saveTranslationLabels("propertySettingOption", 1L, "optionValue", labels);

    TranslationField translationField = translationService.getTranslationField("propertySettingOption", 1L, "optionValue");
    assertNotNull(translationField);
    assertNotNull(translationField.getLabels());
    assertEquals(2, translationField.getLabels().size());
    end();
  }
}
