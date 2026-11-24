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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;

import io.meeds.social.people.model.PeopleDirectoryAttribute;
import io.meeds.social.people.model.PeopleDirectoryAttributeValue;
import io.meeds.social.people.model.PeopleDirectoryEditorGroups;
import io.meeds.social.people.model.PeopleDirectoryGroup;
import io.meeds.social.people.model.PeopleDirectoryPage;
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

  public static final String        GUEST_RESTRICTION_PREFERENCE   = "guestsSpaceMembersOnly";

  public static final String        FILTER_PREFERENCE              = "filter";

  public static final String        DEFAULT_FILTER                 = "all";

  public static final String        CONNECTIONS_FILTER             = "connections";

  /** The request parameter carrying the viewer's people filter. */
  public static final String        FILTER_PARAMETER               = "filter";

  public static final String        ATTRIBUTE_FILTER_PREFERENCE    = "attributeFilter";

  public static final String        GROUP_FILTER_PREFERENCE        = "groupIds";

  public static final String        SETTINGS_RESOURCE_ID           = "settings";

  public static final String        USERS_RESOURCE_ID              = "users";

  public static final String        ATTRIBUTE_VALUES_RESOURCE_ID   = "attributeValues";

  public static final String        GROUPS_RESOURCE_ID             = "groups";

  /** Prefix of a parameter carrying one criterion of the viewer's own filter. */
  public static final String        VIEWER_ATTRIBUTE_PREFIX        = "attr.";

  public static final String        USERS_REST_PATH                = "/v1/social/users";

  public static final String        USERS_EXPAND                   = "settings";

  private static final Log          LOG                            = ExoLogger.getLogger(PeopleListPortlet.class);

  private static final String       CACHE_CONTROL                  = "private, no-store";

  private static final String       FORBIDDEN_STATUS               = "403";

  private static final String       BAD_REQUEST_STATUS             = "400";

  private static final String       GROUPS_KEY                     = "groups";

  /**
   * The only preference names {@link #processAction} stores. Any other posted
   * parameter is ignored: a blanket copy would let an editor store arbitrary
   * preference names on the window, read back as request attributes.
   */
  private static final List<String> STORED_PREFERENCES             = List.of(HEADER_TRANSLATIONS_PREFERENCE,
                                                                             PAGE_SIZE_PREFERENCE,
                                                                             GUEST_RESTRICTION_PREFERENCE,
                                                                             ATTRIBUTE_FILTER_PREFERENCE,
                                                                             GROUP_FILTER_PREFERENCE);

  private static final List<String> RESOURCE_IDS                   = List.of(SETTINGS_RESOURCE_ID,
                                                                             USERS_RESOURCE_ID,
                                                                             ATTRIBUTE_VALUES_RESOURCE_ID,
                                                                             GROUPS_RESOURCE_ID);

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
    return getPeopleDirectoryService().canEditSettings(getPageKey(requestContext),
                                                       requestContext.getSiteKey(),
                                                       uiPortlet.getStorageId(),
                                                       username);
  }

  /**
   * @param username the current user, may be null
   * @return whether the group filter is offered to the user in the settings
   *         drawer, who must also be allowed to edit the window
   */
  public static boolean canSetGroupFilter(String username) {
    return getPeopleDirectoryService().canSetGroupFilter(username);
  }

  /**
   * @param preferences the window preferences
   * @return whether the directory filters its users by profile attribute or
   *         by group, or lists nobody because its stored filter is no longer
   *         valid: the empty state then names the directory's filter, not the
   *         viewer's
   */
  public static boolean isDirectoryFiltered(PortletPreferences preferences) {
    PeopleDirectoryService peopleDirectoryService = getPeopleDirectoryService();
    Map<String, List<String>> attributeFilter =
                                              peopleDirectoryService.getAttributeFilter(preferences.getValue(ATTRIBUTE_FILTER_PREFERENCE,
                                                                                                             null));
    return attributeFilter == null || !attributeFilter.isEmpty()
        || peopleDirectoryService.getGroupFilter(preferences.getValue(GROUP_FILTER_PREFERENCE, null)) != null;
  }

  /**
   * @param preferences the window preferences
   * @return the names of the profile attributes the directory filters on, for
   *         every viewer: the advanced filter shows them greyed out
   */
  public static String directoryAttributes(PortletPreferences preferences) {
    return new JSONArray(getPeopleDirectoryService().getDirectoryAttributeNames(preferences.getValue(ATTRIBUTE_FILTER_PREFERENCE,
                                                                                                      null))).toString();
  }

  /**
   * @param preferences the window preferences
   * @return whether the stored attribute filter is one the directory cannot
   *         read, so that it lists nobody: the settings drawer then shows no
   *         attribute, and Apply replaces the stored value
   */
  public static boolean isAttributeFilterInvalid(PortletPreferences preferences) {
    return getPeopleDirectoryService().getAttributeFilter(preferences.getValue(ATTRIBUTE_FILTER_PREFERENCE, null)) == null;
  }

  /**
   * The group filter as the settings drawer shows it, for a user who may edit
   * the window: the stored groups the user can select, and whether other
   * groups are applied, never named. The other viewers get nothing of it.
   *
   * @param preferences the window preferences
   * @param username the editor
   * @return a JSON object {@code {groups: [{groupId, label}],
   *         otherGroupsApplied, otherGroupsCount, unresolvableGroups,
   *         groupFilterInvalid}}
   */
  public static String storedGroups(PortletPreferences preferences, String username) {
    PeopleDirectoryEditorGroups editorGroups =
                                             getPeopleDirectoryService().getEditorGroups(preferences.getValue(GROUP_FILTER_PREFERENCE,
                                                                                                              null),
                                                                                         username);
    return new JSONObject().put(GROUPS_KEY, toGroupsJson(editorGroups.groups()))
                           .put("otherGroupsApplied", editorGroups.otherGroupsApplied())
                           .put("otherGroupsCount", editorGroups.otherGroupsCount())
                           .put("unresolvableGroups", editorGroups.unresolvableGroups())
                           .put("groupFilterInvalid", editorGroups.groupFilterInvalid())
                           .toString();
  }

  /**
   * The attribute filter as the settings drawer shows it, for a user who may
   * edit the window: the values the filter stores are not shown to the other
   * viewers.
   *
   * @param preferences the window preferences
   * @param locale the editor's language
   * @return a JSON array of {@code {name, values: [{value, label}], available}}
   */
  public static String storedAttributes(PortletPreferences preferences, Locale locale) {
    List<PeopleDirectoryAttribute> attributes =
                                              getPeopleDirectoryService().getEditorAttributes(preferences.getValue(ATTRIBUTE_FILTER_PREFERENCE,
                                                                                                                   null),
                                                                                              locale);
    JSONArray json = new JSONArray();
    for (PeopleDirectoryAttribute attribute : attributes) {
      JSONArray values = new JSONArray();
      attribute.values()
               .forEach(value -> values.put(new JSONObject().put("value", value.value()).put("label", value.label())));
      json.put(new JSONObject().put("name", attribute.name()).put("values", values).put("available", attribute.available()));
    }
    return json.toString();
  }

  /**
   * @param preferences the window preferences
   * @return the number of users listed per page
   */
  public static int storedPageSize(PortletPreferences preferences) {
    return getPeopleDirectoryService().getPageSize(preferences.getValue(PAGE_SIZE_PREFERENCE, null));
  }

  /**
   * @param preferences the window's preferences
   * @return whether the window restricts guest viewers, ON unless the stored
   *         value is {@code false}
   */
  public static boolean storedGuestRestriction(PortletPreferences preferences) {
    return getPeopleDirectoryService().isGuestRestriction(preferences.getValue(GUEST_RESTRICTION_PREFERENCE, null));
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
          preferences.setValue(name, switch (name) {
          case PAGE_SIZE_PREFERENCE -> peopleDirectoryService.normalizePageSize(value);
          case GUEST_RESTRICTION_PREFERENCE -> peopleDirectoryService.normalizeGuestRestriction(value);
          case ATTRIBUTE_FILTER_PREFERENCE -> peopleDirectoryService.normalizeAttributeFilter(value);
          case GROUP_FILTER_PREFERENCE -> peopleDirectoryService.normalizeGroupFilter(value,
                                                                                      preferences.getValue(name, null),
                                                                                      request.getRemoteUser());
          default -> peopleDirectoryService.normalizeHeaderTranslations(value);
          });
        }
      }
    } catch (IllegalArgumentException e) {
      throw new PortletException(e.getMessage(), e);
    }
    preferences.store();
  }

  /**
   * Answers the portlet's resources, each behind a right and varying with the
   * viewer, so never cached: {@code settings}, the stored settings as the
   * directory uses them, which the settings drawer reads back after a save (a
   * refusal of {@link #processAction} is answered with a 200 by the portal, so
   * only the stored values tell whether the save was applied);
   * {@code users}, the users the directory lists to the viewer, filtered
   * server-side by the window's stored filter; {@code attributeValues}, the
   * values an editor can select for a filter attribute; {@code groups}, the
   * groups an editor can select for the group filter. An anonymous request
   * is refused before anything is read.
   */
  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    String resourceId = request.getResourceID();
    if (!RESOURCE_IDS.contains(resourceId)) {
      super.serveResource(request, response);
      return;
    }
    response.setProperty("Cache-Control", CACHE_CONTROL);
    String username = request.getRemoteUser();
    if (StringUtils.isBlank(username)) {
      response.setProperty(ResourceResponse.HTTP_STATUS_CODE, FORBIDDEN_STATUS);
      return;
    }
    try {
      String body = switch (resourceId) {
      case USERS_RESOURCE_ID -> getUsers(request, username);
      case ATTRIBUTE_VALUES_RESOURCE_ID -> getAttributeValues(request, username);
      case GROUPS_RESOURCE_ID -> getGroups(request, username);
      default -> getSettings(request, username);
      };
      response.setContentType("application/json");
      response.getWriter().write(body);
    } catch (IllegalAccessException e) {
      LOG.debug("User {} is not allowed to read the resource {} of the people directory", username, resourceId, e);
      response.setProperty(ResourceResponse.HTTP_STATUS_CODE, FORBIDDEN_STATUS);
    } catch (IllegalArgumentException e) {
      response.setProperty(ResourceResponse.HTTP_STATUS_CODE, BAD_REQUEST_STATUS);
      response.setContentType("text/plain");
      response.getWriter().write(StringUtils.defaultString(e.getMessage()));
    }
  }

  private String getSettings(ResourceRequest request, String username) {
    PortletPreferences preferences = request.getPreferences();
    JSONObject settings = new JSONObject();
    settings.put(HEADER_TRANSLATIONS_PREFERENCE, new JSONObject(storedHeaderTranslations(preferences)));
    settings.put(PAGE_SIZE_PREFERENCE, storedPageSize(preferences));
    settings.put("directoryFiltered", isDirectoryFiltered(preferences));
    settings.put("directoryAttributes", new JSONArray(directoryAttributes(preferences)));
    if (canEdit(username)) {
      settings.put(GUEST_RESTRICTION_PREFERENCE, storedGuestRestriction(preferences));
      settings.put("attributes", new JSONArray(storedAttributes(preferences, request.getLocale())));
      settings.put("attributeFilterInvalid", isAttributeFilterInvalid(preferences));
      settings.put("canSetGroupFilter", canSetGroupFilter(username));
      JSONObject groups = new JSONObject(storedGroups(preferences, username));
      settings.put(GROUPS_KEY, groups.getJSONArray(GROUPS_KEY));
      settings.put("otherGroupsApplied", groups.getBoolean("otherGroupsApplied"));
      settings.put("otherGroupsCount", groups.getInt("otherGroupsCount"));
      settings.put("unresolvableGroups", groups.getBoolean("unresolvableGroups"));
      settings.put("groupFilterInvalid", groups.getBoolean("groupFilterInvalid"));
    }
    return settings.toString();
  }

  private String getUsers(ResourceRequest request, String username) throws IllegalAccessException {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    UIPortlet uiPortlet = UIPortlet.getCurrentUIPortlet();
    if (requestContext == null || uiPortlet == null) {
      throw new IllegalAccessException("No people directory window in the request");
    }
    int offset = parseInt(request.getParameter("offset"), 0);
    int limit = parseInt(request.getParameter("limit"), PeopleDirectoryService.DEFAULT_PAGE_SIZE);
    Map<String, String> viewerAttributes = new HashMap<>();
    request.getParameterMap().forEach((name, values) -> {
      if (StringUtils.startsWith(name, VIEWER_ATTRIBUTE_PREFIX) && values != null && values.length > 0) {
        viewerAttributes.put(name.substring(VIEWER_ATTRIBUTE_PREFIX.length()), values[0]);
      }
    });
    PeopleDirectoryPage page = getPeopleDirectoryService().search(getPageKey(requestContext),
                                                                  requestContext.getSiteKey(),
                                                                  uiPortlet.getStorageId(),
                                                                  request.getPreferences()
                                                                         .getValue(ATTRIBUTE_FILTER_PREFERENCE, null),
                                                                  request.getPreferences().getValue(GROUP_FILTER_PREFERENCE, null),
                                                                  request.getPreferences().getValue(GUEST_RESTRICTION_PREFERENCE, null),
                                                                  request.getParameter("q"),
                                                                  viewerAttributes,
                                                                  StringUtils.equals(request.getParameter(FILTER_PARAMETER),
                                                                                     CONNECTIONS_FILTER),
                                                                  username,
                                                                  offset,
                                                                  limit);
    List<DataEntity> users = new ArrayList<>();
    for (Identity identity : page.identities()) {
      if (identity.getProfile() != null) {
        users.add(EntityBuilder.buildEntityProfile(identity.getProfile(), USERS_REST_PATH, USERS_EXPAND).getDataEntity());
      }
    }
    CollectionEntity collection = new CollectionEntity(users, EntityBuilder.USERS_TYPE, offset, limit);
    collection.setSize(page.size());
    return EntityBuilder.toJsonString(collection);
  }

  private String getAttributeValues(ResourceRequest request, String username) throws IllegalAccessException {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    UIPortlet uiPortlet = UIPortlet.getCurrentUIPortlet();
    if (requestContext == null || uiPortlet == null) {
      throw new IllegalAccessException("No people directory window in the request");
    }
    List<PeopleDirectoryAttributeValue> values = getPeopleDirectoryService().getAttributeValues(getPageKey(requestContext),
                                                                                                requestContext.getSiteKey(),
                                                                                                uiPortlet.getStorageId(),
                                                                                                request.getParameter("attribute"),
                                                                                                request.getParameter("q"),
                                                                                                username,
                                                                                                request.getLocale());
    JSONArray json = new JSONArray();
    values.forEach(value -> json.put(new JSONObject().put("value", value.value()).put("label", value.label())));
    return json.toString();
  }

  private String getGroups(ResourceRequest request, String username) throws IllegalAccessException {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    UIPortlet uiPortlet = UIPortlet.getCurrentUIPortlet();
    if (requestContext == null || uiPortlet == null) {
      throw new IllegalAccessException("No people directory window in the request");
    }
    String[] excluded = request.getParameterValues("exclude");
    List<PeopleDirectoryGroup> groups = getPeopleDirectoryService().getSelectableGroups(getPageKey(requestContext),
                                                                                        requestContext.getSiteKey(),
                                                                                        uiPortlet.getStorageId(),
                                                                                        request.getParameter("q"),
                                                                                        excluded == null ? List.of() : Arrays.asList(excluded),
                                                                                        username);
    return toGroupsJson(groups).toString();
  }

  private static JSONArray toGroupsJson(List<PeopleDirectoryGroup> groups) {
    JSONArray json = new JSONArray();
    groups.forEach(group -> json.put(new JSONObject().put("groupId", group.groupId()).put("label", group.label())));
    return json;
  }

  private static int parseInt(String value, int defaultValue) {
    if (StringUtils.isBlank(value)) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(PeopleDirectoryService.INVALID_RANGE_MESSAGE, e);
    }
  }

  private static PageKey getPageKey(PortalRequestContext requestContext) {
    Page page = requestContext.getPage();
    return page == null ? null : page.getPageKey();
  }

  private static PeopleDirectoryService getPeopleDirectoryService() {
    return CommonsUtils.getService(PeopleDirectoryService.class);
  }

}
