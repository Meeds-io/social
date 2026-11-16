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
package io.meeds.social.portlet;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.application.UIPortlet;

import io.meeds.social.people.service.PeopleDirectoryService;

/**
 * People Directory application. The class is glue: the edit right and the
 * validity of the stored values are {@link PeopleDirectoryService}'s
 * decisions, taken on the page, the site and the window id read from the
 * portal context, never from a request parameter.
 * <p>
 * The JSP reads what it trusts through the static methods of this class,
 * never through a request attribute: {@code GenericDispatchedViewPortlet}
 * copies every stored preference into a request attribute of the same name,
 * and the layout editor can store a preference under any name, so a stored
 * {@code canEdit} would otherwise answer for the edit right.
 */
public class PeopleListPortlet extends GenericDispatchedViewPortlet {

  public static final String        HEADER_TRANSLATIONS_PREFERENCE = "headerTranslations";

  public static final String        PAGE_SIZE_PREFERENCE           = "pageSize";

  public static final String        FILTER_PREFERENCE              = "filter";

  public static final String        DEFAULT_FILTER                 = "all";

  public static final String        CONNECTIONS_FILTER             = "connections";

  public static final String        SETTINGS_RESOURCE_ID           = "settings";

  /**
   * The only preference names {@link #processAction} stores. Any other posted
   * parameter is ignored: a blanket copy would let an editor store arbitrary
   * preference names on the window, read back as request attributes.
   */
  private static final List<String> STORED_PREFERENCES             = List.of(HEADER_TRANSLATIONS_PREFERENCE,
                                                                             PAGE_SIZE_PREFERENCE);

  /**
   * Whether the current user may change the settings of the window being
   * rendered.
   *
   * @param username the current user, may be null
   * @return true when the settings drawer may be offered and saved
   */
  public static boolean canEdit(String username) {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    UIPortlet uiPortlet = UIPortlet.getCurrentUIPortlet();
    if (requestContext == null || uiPortlet == null) {
      return false;
    }
    Page page = requestContext.getPage();
    return getPeopleDirectoryService().canEditSettings(page == null ? null : page.getPageKey(),
                                                       requestContext.getSiteKey(),
                                                       uiPortlet.getStorageId(),
                                                       username);
  }

  /**
   * @param preferences the window preferences
   * @return the number of users listed per page
   */
  public static int storedPageSize(PortletPreferences preferences) {
    return getPeopleDirectoryService().getPageSize(preferences.getValue(PAGE_SIZE_PREFERENCE, null));
  }

  /**
   * @param preferences the window preferences
   * @return the header labels per language, {@code {}} when the header is OFF
   */
  public static String storedHeaderTranslations(PortletPreferences preferences) {
    return getPeopleDirectoryService().getHeaderTranslations(preferences.getValue(HEADER_TRANSLATIONS_PREFERENCE, null));
  }

  /**
   * @param preferences the window preferences
   * @return the quick filter the directory opens with: all or connections
   */
  public static String storedFilter(PortletPreferences preferences) {
    String filter = preferences.getValue(FILTER_PREFERENCE, null);
    return StringUtils.equals(filter, CONNECTIONS_FILTER) ? CONNECTIONS_FILTER : DEFAULT_FILTER;
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canEdit(request.getRemoteUser())) {
      throw new PortletException("User is not allowed to save settings");
    }
    PeopleDirectoryService peopleDirectoryService = getPeopleDirectoryService();
    PortletPreferences preferences = request.getPreferences();
    try {
      for (String name : STORED_PREFERENCES) {
        String value = request.getParameter(name);
        if (value != null) {
          preferences.setValue(name,
                               StringUtils.equals(name, PAGE_SIZE_PREFERENCE) ? peopleDirectoryService.normalizePageSize(value)
                                                                              : peopleDirectoryService.normalizeHeaderTranslations(value));
        }
      }
    } catch (IllegalArgumentException e) {
      throw new PortletException(e.getMessage(), e);
    }
    preferences.store();
  }

  /**
   * Answers the stored display settings, as the directory uses them. The
   * settings drawer reads them back after a save: a refusal of
   * {@link #processAction} is answered with a 200 by the portal, so only the
   * stored values tell whether the save was applied.
   */
  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    if (!SETTINGS_RESOURCE_ID.equals(request.getResourceID())) {
      super.serveResource(request, response);
      return;
    }
    PortletPreferences preferences = request.getPreferences();
    JSONObject settings = new JSONObject();
    settings.put(HEADER_TRANSLATIONS_PREFERENCE, new JSONObject(storedHeaderTranslations(preferences)));
    settings.put(PAGE_SIZE_PREFERENCE, storedPageSize(preferences));
    response.setContentType("application/json");
    response.setProperty("Cache-Control", "private, no-store");
    response.getWriter().write(settings.toString());
  }

  private static PeopleDirectoryService getPeopleDirectoryService() {
    return CommonsUtils.getService(PeopleDirectoryService.class);
  }

}
