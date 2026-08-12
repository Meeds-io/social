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
package io.meeds.social.cms.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Shared helpers telling whether a {@link io.meeds.social.cms.model.CMSSetting}
 * is still backed by a widget actually present on its page's layout.
 * <p>
 * This rule has to be applied identically by everything that decides what
 * belongs in the "page" search index: a {@code CMSSetting} surviving after
 * its widget was removed from the page doesn't count as a live content
 * block — nothing ever deletes that setting, and the plugin's own content
 * (e.g. the note it names) can very well still resolve just fine, so
 * content availability alone can't tell the two cases apart. Applying the
 * rule in the incremental indexing listener but not in the connector's
 * {@code getAllIds} would make every full reindex resurrect exactly the
 * blocks the listener had removed.
 */
public class PageContentBlockUtils {

  /** Class-level logger. */
  private static final Log    LOG                            = ExoLogger.getExoLogger(PageContentBlockUtils.class);

  /**
   * The portlet preference holding a content-block widget's {@link io.meeds.social.cms.model.CMSSetting}
   * name — must match {@code io.meeds.social.portlet.CMSPortlet.NAME}
   * (component/web can't be depended on from here, so the literal is
   * duplicated rather than shared).
   */
  public static final String  WIDGET_SETTING_NAME_PREFERENCE = "name";

  private PageContentBlockUtils() {
    // Utility class
  }

  /**
   * @param  layoutService used to load each widget's portlet preferences
   * @param  root          the page (or any container within it) to walk
   * @return the {@link io.meeds.social.cms.model.CMSSetting} names of every
   *         widget currently present anywhere in the page's layout tree,
   *         read from each widget's own {@value #WIDGET_SETTING_NAME_PREFERENCE}
   *         preference.
   */
  public static Set<String> collectWidgetSettingNames(LayoutService layoutService, ModelObject root) {
    Set<String> names = new HashSet<>();
    collectWidgetSettingNames(layoutService, root, names);
    return names;
  }

  private static void collectWidgetSettingNames(LayoutService layoutService, ModelObject node, Set<String> names) {
    if (node instanceof Application application) {
      String name = readWidgetSettingName(layoutService, application);
      if (StringUtils.isNotBlank(name)) {
        names.add(name);
      }
    } else if (node instanceof Container container && container.getChildren() != null) {
      container.getChildren().forEach(child -> collectWidgetSettingNames(layoutService, child, names));
    }
  }

  /**
   * Same rule as {@link #collectWidgetSettingNames}, for when only one setting
   * name is in question: stops at the first widget carrying it instead of
   * loading the portlet preferences of every widget on the page.
   *
   * @param  layoutService used to load each widget's portlet preferences
   * @param  node          the page (or any container within it) to walk
   * @param  settingName   the {@link io.meeds.social.cms.model.CMSSetting} name to look for
   * @return whether a widget currently present on the page carries that setting name
   */
  public static boolean hasWidgetWithSettingName(LayoutService layoutService, ModelObject node, String settingName) {
    if (node instanceof Application application) {
      return StringUtils.equals(readWidgetSettingName(layoutService, application), settingName);
    } else if (node instanceof Container container && container.getChildren() != null) {
      return container.getChildren()
                      .stream()
                      .anyMatch(child -> hasWidgetWithSettingName(layoutService, child, settingName));
    }
    return false;
  }

  private static String readWidgetSettingName(LayoutService layoutService, Application application) {
    try {
      Portlet preferences = layoutService.load(application.getState());
      return preferences == null ? null : preferences.getValue(WIDGET_SETTING_NAME_PREFERENCE);
    } catch (Exception e) {
      LOG.debug("Cannot load preferences of application {}", application.getStorageId(), e);
      return null;
    }
  }

}
