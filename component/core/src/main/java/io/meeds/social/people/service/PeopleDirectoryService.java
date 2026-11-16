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
package io.meeds.social.people.service;

import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;

/**
 * Rules of the People Directory application instances (EXO-93255): who may
 * change an instance's settings, and which stored values are valid. The
 * portlet is glue: it reads the page, the site and its own window id from the
 * portal context and its preferences from the portlet container, and every
 * decision is taken here.
 */
@Service
public class PeopleDirectoryService {

  public static final int     DEFAULT_PAGE_SIZE                  = 12;

  public static final int     MIN_PAGE_SIZE                      = 1;

  public static final int     MAX_PAGE_SIZE                      = 50;

  public static final int     MAX_HEADER_LENGTH                  = 150;

  public static final String  EMPTY_HEADER_TRANSLATIONS          = "{}";

  public static final String  PAGE_SIZE_OUT_OF_RANGE_MESSAGE     = "peopleDirectory.pageSize.outOfRange";

  public static final String  HEADER_TRANSLATIONS_INVALID_MESSAGE = "peopleDirectory.headerTranslations.invalid";

  public static final String  HEADER_TOO_LONG_MESSAGE            = "peopleDirectory.header.tooLong";

  /**
   * A language key of the header map: a language code, optionally followed by
   * a region or a script ({@code en}, {@code pt_BR}, {@code zh-Hant}).
   */
  private static final Pattern LANGUAGE_KEY_PATTERN              = Pattern.compile("^[a-zA-Z]{2,8}([_-][a-zA-Z0-9]{1,8}){0,2}$");

  @Autowired
  protected UserACL                 userAcl;

  @Autowired
  protected LayoutService           layoutService;

  @Autowired
  protected UserPortalConfigService userPortalConfigService;

  /**
   * Whether a user may change the settings of one People Directory window.
   * <p>
   * The right is bound to the window, never to the page the URL routes to:
   * the portlet-viewer page renders any stored window by its id, so the page
   * of the portal context does not necessarily hold the window. The window is
   * looked up first in the stored layout of that page, whose edit right then
   * applies, then in the layout the portal renders for the site (the meta
   * site's when the site is displayed), whose edit right then applies. A
   * window held by neither, such as a catalogue instance previewed in the
   * portlet editor, has no editable settings.
   *
   * @param pageKey the page resolved from the portal context, may be null
   * @param siteKey the rendering site, may be null
   * @param windowId the storage id of the window being rendered
   * @param username the user asking
   * @return true when the user may store the window's settings
   */
  public boolean canEditSettings(PageKey pageKey, SiteKey siteKey, String windowId, String username) {
    if (StringUtils.isBlank(windowId) || StringUtils.isBlank(username)) {
      return false;
    }
    Identity identity = userAcl.getUserIdentity(username);
    if (identity == null) {
      return false;
    }
    Page page = pageKey == null ? null : layoutService.getPage(pageKey);
    if (page != null && holdsWindow(page, windowId)) {
      return userAcl.hasEditPermission(page, identity);
    }
    PortalConfig site = getRenderedSite(siteKey);
    if (site != null && holdsWindow(site.getPortalLayout(), windowId)) {
      return userAcl.hasEditPermission(site, identity);
    }
    return false;
  }

  /**
   * Validates a posted number of items per page.
   *
   * @param value the posted value
   * @return the value to store
   * @throws IllegalArgumentException with
   *           {@link #PAGE_SIZE_OUT_OF_RANGE_MESSAGE} when the value is not an
   *           integer from {@link #MIN_PAGE_SIZE} to {@link #MAX_PAGE_SIZE}
   */
  public String normalizePageSize(String value) {
    int pageSize = NumberUtils.toInt(StringUtils.trim(value), -1);
    if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException(PAGE_SIZE_OUT_OF_RANGE_MESSAGE);
    }
    return String.valueOf(pageSize);
  }

  /**
   * Validates a posted header: a JSON object mapping a language to its label.
   * Blank labels are dropped, so an empty result means the header is OFF.
   *
   * @param value the posted value
   * @return the value to store, re-serialized
   * @throws IllegalArgumentException with
   *           {@link #HEADER_TRANSLATIONS_INVALID_MESSAGE} when the value is
   *           not such an object, or {@link #HEADER_TOO_LONG_MESSAGE} when a
   *           label exceeds {@link #MAX_HEADER_LENGTH} characters
   */
  public String normalizeHeaderTranslations(String value) {
    JSONObject translations;
    try {
      translations = new JSONObject(StringUtils.defaultIfBlank(value, EMPTY_HEADER_TRANSLATIONS));
    } catch (JSONException e) {
      throw new IllegalArgumentException(HEADER_TRANSLATIONS_INVALID_MESSAGE, e);
    }
    JSONObject normalized = new JSONObject();
    for (String language : translations.keySet()) {
      if (!LANGUAGE_KEY_PATTERN.matcher(language).matches() || !(translations.get(language) instanceof String label)) {
        throw new IllegalArgumentException(HEADER_TRANSLATIONS_INVALID_MESSAGE);
      } else if (label.length() > MAX_HEADER_LENGTH) {
        throw new IllegalArgumentException(HEADER_TOO_LONG_MESSAGE);
      } else if (StringUtils.isNotBlank(label)) {
        normalized.put(language, label);
      }
    }
    return normalized.toString();
  }

  /**
   * The stored number of items per page as the directory uses it. A value
   * stored by another writer of the window's preferences (the layout editor
   * stores any value) is brought back within the bounds; a missing or
   * unreadable one gives the default.
   *
   * @param storedValue the stored preference value, may be null
   * @return a page size from {@link #MIN_PAGE_SIZE} to {@link #MAX_PAGE_SIZE}
   */
  public int getPageSize(String storedValue) {
    int pageSize = NumberUtils.toInt(StringUtils.trim(storedValue), DEFAULT_PAGE_SIZE);
    return Math.min(MAX_PAGE_SIZE, Math.max(MIN_PAGE_SIZE, pageSize));
  }

  /**
   * The stored header as the directory uses it. A value another writer stored
   * and that is not valid is read as no header.
   *
   * @param storedValue the stored preference value, may be null
   * @return a valid JSON object of language to label, {@code {}} when none
   */
  public String getHeaderTranslations(String storedValue) {
    try {
      return normalizeHeaderTranslations(storedValue);
    } catch (IllegalArgumentException e) {
      return EMPTY_HEADER_TRANSLATIONS;
    }
  }

  private PortalConfig getRenderedSite(SiteKey siteKey) {
    PortalConfig site = siteKey == null ? null : layoutService.getPortalConfig(siteKey);
    if (site != null && site.isDisplayed()) {
      return userPortalConfigService.getMetaPortalConfig();
    }
    return site;
  }

  private boolean holdsWindow(ModelObject modelObject, String windowId) {
    if (modelObject instanceof Application application) {
      return StringUtils.equals(application.getStorageId(), windowId);
    } else if (modelObject instanceof Container container && CollectionUtils.isNotEmpty(container.getChildren())) {
      return container.getChildren().stream().anyMatch(child -> holdsWindow(child, windowId));
    } else {
      return false;
    }
  }

}
