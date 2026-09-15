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
package io.meeds.social.portlet;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;

/**
 * Portlet listing the sub-spaces of the space whose page hosts it. The class
 * is glue only: preferences in, {@link SpaceService} calls, nothing decided
 * here. Its three preferences are declared in {@code portlet.xml} and written
 * only through {@link #processAction}, under the parent space management
 * guard, so that {@code showHiddenSubspaces} — the flag that lets a page
 * expose hidden sub-spaces to non-members — is never influenced by a client
 * value.
 */
public class SubspacesListPortlet extends GenericDispatchedViewPortlet {

  public static final String        HEADER_TRANSLATIONS_PREFERENCE   = "headerTranslations";

  public static final String        SHOW_HIDDEN_SUBSPACES_PREFERENCE = "showHiddenSubspaces";

  public static final String        SUBSPACES_LIMIT_PREFERENCE       = "subspacesLimit";

  public static final int           DEFAULT_SUBSPACES_LIMIT          = 4;

  public static final int           MIN_SUBSPACES_LIMIT              = 1;

  public static final int           MAX_SUBSPACES_LIMIT              = 25;

  public static final String        LIMIT_OUT_OF_RANGE_MESSAGE       = "subspacesList.limit.outOfRange";

  public static final String        HEADER_TRANSLATIONS_INVALID_MESSAGE = "subspacesList.headerTranslations.invalid";

  public static final String        SHOW_HIDDEN_SUBSPACES_INVALID_MESSAGE = "subspacesList.showHiddenSubspaces.invalid";

  /**
   * The only preference names {@link #processAction} stores. Any other posted
   * parameter is ignored: a blanket copy would let a space admin store
   * arbitrary preference names on the page window.
   */
  private static final List<String> STORED_PREFERENCES               = List.of(HEADER_TRANSLATIONS_PREFERENCE,
                                                                               SHOW_HIDDEN_SUBSPACES_PREFERENCE,
                                                                               SUBSPACES_LIMIT_PREFERENCE);

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifySettings(request.getRemoteUser())) {
      throw new PortletException("User is not allowed to save settings");
    }
    PortletPreferences preferences = request.getPreferences();
    for (String name : STORED_PREFERENCES) {
      String value = request.getParameter(name);
      if (value != null) {
        preferences.setValue(name, normalizePreferenceValue(name, value));
      }
    }
    preferences.store();
  }

  private String normalizePreferenceValue(String name, String value) throws PortletException {
    if (StringUtils.equals(name, SUBSPACES_LIMIT_PREFERENCE)) {
      int limit = NumberUtils.toInt(value, -1);
      if (limit < MIN_SUBSPACES_LIMIT || limit > MAX_SUBSPACES_LIMIT) {
        throw new PortletException(LIMIT_OUT_OF_RANGE_MESSAGE);
      }
      return String.valueOf(limit);
    } else if (StringUtils.equals(name, SHOW_HIDDEN_SUBSPACES_PREFERENCE)) {
      // the flag exposes hidden sub-spaces to non-members: only an explicit true/false is stored
      if (!StringUtils.equalsAny(value, "true", "false")) {
        throw new PortletException(SHOW_HIDDEN_SUBSPACES_INVALID_MESSAGE);
      }
      return value;
    } else {
      // headerTranslations: a JSON object (lang -> label), labels being
      // strings, re-serialized so that the JSP always hands the widget
      // something JSON.parse accepts and the title prop can render
      try {
        JSONObject translations = new JSONObject(value);
        for (String lang : translations.keySet()) {
          if (!(translations.get(lang) instanceof String)) {
            throw new PortletException(HEADER_TRANSLATIONS_INVALID_MESSAGE);
          }
        }
        return translations.toString();
      } catch (JSONException e) {
        throw new PortletException(HEADER_TRANSLATIONS_INVALID_MESSAGE, e);
      }
    }
  }

  private boolean canModifySettings(String username) {
    Space space = SpaceUtils.getSpaceByContext();
    // resolved per call, as the sibling ParentSpaceListingPortlet does: the
    // portlet instance is not Spring-managed and must not pin a container
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    return spaceService.canManageSpace(space, username);
  }
}
