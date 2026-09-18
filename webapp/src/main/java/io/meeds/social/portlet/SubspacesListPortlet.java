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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.util.JsonUtils;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

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

  public static final String        LIMIT_PARAMETER                  = "limit";

  /**
   * Hard bound of one resource call. The 'see more' drawer lists every
   * sub-space at once (no pagination yet), so the cap is what keeps one parent
   * with an unexpected number of sub-spaces from making the call unbounded. A
   * constant rather than a property: the real bound is the parent template's
   * subspacesMaxLimit, and beyond this the answer is pagination, not a bigger
   * number.
   */
  public static final int           MAX_RESOURCE_LIMIT               = 500;

  /**
   * Statuses of the resource phase, following the platform's exception to
   * status contract: not found, then access, then parameter validation.
   * Declared here because this module has no servlet API on its classpath.
   */
  private static final int          STATUS_BAD_REQUEST               = 400;

  private static final int          STATUS_FORBIDDEN                 = 403;

  private static final int          STATUS_NOT_FOUND                 = 404;

  private static final Log          LOG                              = ExoLogger.getLogger(SubspacesListPortlet.class);

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

  /**
   * Answers the widget with everything it needs to render, in one envelope:
   * whether the hosting space is a parent space at all, what the viewer may do
   * on it, and the sub-spaces they may see. Every decision is the
   * {@link SpaceService}'s; this method only reads the trusted preference and
   * the page context.
   * <p>
   * The space is resolved from the page ({@link SpaceUtils#getSpaceByContext()}),
   * never from a request parameter, and {@code showHiddenSubspaces} is read
   * from the portlet preferences, never from the request: a client value must
   * not be able to widen what is listed.
   */
  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    response.setContentType("application/json");
    String username = request.getRemoteUser();
    Space space = SpaceUtils.getSpaceByContext();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);

    if (space == null || !spaceService.isParentSpace(space)) {
      // no space context, a sub-space, or a template allowing no sub-space:
      // nothing to show and no listing query
      writeJson(response, SubspacesEnvelope.notParentSpace());
      return;
    }

    try {
      int limit = parseLimit(request.getParameter(LIMIT_PARAMETER));
      boolean showHiddenSubspaces = Boolean.parseBoolean(request.getPreferences()
                                                                .getValue(SHOW_HIDDEN_SUBSPACES_PREFERENCE, "false"));
      List<Space> subspaces = spaceService.getSubspaces(space.getSpaceId(), username, showHiddenSubspaces, 0, limit);
      writeJson(response,
                new SubspacesEnvelope(true,
                                      spaceService.canManageSpace(space, username),
                                      spaceService.canCreateSubspace(space, username, request.getLocale()),
                                      subspaces.stream().map(subspace -> toItem(spaceService, subspace, username)).toList()));
    } catch (ObjectNotFoundException e) {
      LOG.debug("Parent space {} not found while listing its subspaces", space.getSpaceId(), e);
      sendError(response, STATUS_NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      LOG.debug("User {} isn't allowed to list subspaces of space {}", username, space.getSpaceId(), e);
      sendError(response, STATUS_FORBIDDEN, e.getMessage());
    } catch (IllegalArgumentException e) {
      sendError(response, STATUS_BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * @param value the requested limit, absent when the drawer asks for the whole
   *          list
   * @return the number of sub-spaces to load, never above
   *         {@link #MAX_RESOURCE_LIMIT}
   * @throws IllegalArgumentException when the parameter is present and is not a
   *           strictly positive number
   */
  private int parseLimit(String value) {
    if (StringUtils.isBlank(value)) {
      return MAX_RESOURCE_LIMIT;
    }
    int limit = NumberUtils.toInt(value, -1);
    if (limit <= 0) {
      throw new IllegalArgumentException(LIMIT_OUT_OF_RANGE_MESSAGE);
    }
    return Math.min(limit, MAX_RESOURCE_LIMIT);
  }

  private SubspaceItem toItem(SpaceService spaceService, Space subspace, String username) {
    return new SubspaceItem(subspace.getId(),
                            subspace.getDisplayName(),
                            subspace.getPrettyName(),
                            subspace.getAvatarUrl(),
                            subspace.getVisibility(),
                            spaceService.isMember(subspace, username));
  }

  private void writeJson(ResourceResponse response, SubspacesEnvelope envelope) throws IOException {
    response.getWriter().write(JsonUtils.toJsonString(envelope));
  }

  private void sendError(ResourceResponse response, int status, String message) throws IOException {
    response.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(status));
    response.setContentType("text/plain");
    response.getWriter().write(StringUtils.defaultString(message));
  }

  /**
   * What the widget renders from. {@code parentSpace} false means the widget
   * hides its whole application: the other fields are then empty and no listing
   * query ran.
   */
  public record SubspacesEnvelope(boolean parentSpace,
                                  boolean canManageSpace,
                                  boolean canCreateSubspace,
                                  List<SubspaceItem> subspaces) {
    public static SubspacesEnvelope notParentSpace() {
      return new SubspacesEnvelope(false, false, false, Collections.emptyList());
    }
  }

  /**
   * One listed sub-space. {@code isMember} carries the viewer's membership for
   * the hidden-space rendering only; it is not an access decision, which the
   * Service already made by returning the space at all.
   */
  public record SubspaceItem(String id,
                             String displayName,
                             String prettyName,
                             String avatarUrl,
                             String visibility,
                             boolean isMember) {
  }

  private boolean canModifySettings(String username) {
    Space space = SpaceUtils.getSpaceByContext();
    // resolved per call, as the sibling ParentSpaceListingPortlet does: the
    // portlet instance is not Spring-managed and must not pin a container
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    return spaceService.canManageSpace(space, username);
  }
}
