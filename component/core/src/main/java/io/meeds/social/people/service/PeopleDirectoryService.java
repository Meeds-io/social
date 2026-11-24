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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.search.ProfileSearchConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.identity.permission.service.UserPermissionService;
import io.meeds.social.people.model.DirectoryGroup;
import io.meeds.social.people.model.PeopleDirectoryAttribute;
import io.meeds.social.people.model.PeopleDirectoryAttributeValue;
import io.meeds.social.people.model.PeopleDirectoryEditorGroups;
import io.meeds.social.people.model.PeopleDirectoryFilter;
import io.meeds.social.people.model.PeopleDirectoryGroup;
import io.meeds.social.people.model.PeopleDirectoryPage;
import io.meeds.social.translation.service.TranslationService;
import io.meeds.social.user.plugin.UserAclPlugin;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Rules of the People Directory application instances (EXO-93255,
 * EXO-93256, EXO-93257): who may change an instance's settings or list its users, which
 * stored values are valid, and the users an instance lists. The
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

  public static final int     MAX_ATTRIBUTES                     = 10;

  public static final int     MAX_VALUES_PER_ATTRIBUTE           = 20;

  public static final int     MAX_VALUE_LENGTH                   = 150;

  public static final int     MAX_ATTRIBUTE_VALUES_SUGGESTED     = 50;

  public static final int     MAX_LIMIT                          = 100;

  public static final String  EMPTY_JSON_OBJECT                  = "{}";

  public static final String  UNKNOWN_ATTRIBUTE_MESSAGE          = "peopleDirectory.unknownAttribute";

  public static final String  ATTRIBUTE_FILTER_INVALID_MESSAGE   = "peopleDirectory.attributeFilter.invalid";

  public static final String  TOO_MANY_ATTRIBUTES_MESSAGE        = "peopleDirectory.attributeFilter.tooManyAttributes";

  public static final String  TOO_MANY_VALUES_MESSAGE            = "peopleDirectory.attributeFilter.tooManyValues";

  public static final String  VALUE_TOO_LONG_MESSAGE             = "peopleDirectory.attributeFilter.valueTooLong";

  public static final String  INVALID_RANGE_MESSAGE              = "peopleDirectory.invalidRange";

  public static final int     MAX_GROUPS                         = 20;

  public static final int     MAX_GROUPS_SUGGESTED               = 20;

  public static final int     MAX_GROUP_QUERY_LENGTH             = 100;

  public static final String  EMPTY_JSON_ARRAY                   = "[]";

  public static final String  GROUP_FILTER_INVALID_MESSAGE       = "peopleDirectory.groupFilter.invalid";

  public static final String  GROUP_NOT_SELECTABLE_MESSAGE       = "peopleDirectory.groupFilter.notSelectable";

  public static final String  TOO_MANY_GROUPS_MESSAGE            = "peopleDirectory.groupFilter.tooManyGroups";

  private static final Log    LOG                                = ExoLogger.getLogger(PeopleDirectoryService.class);

  private static final String EMAIL_PROPERTY                     = "email";

  private static final JsonMapper JSON_MAPPER                    = new JsonMapper();

  private static final String PROFILE_PROPERTY_OPTION_OBJECT_TYPE = "propertySettingOption";

  private static final String PROFILE_PROPERTY_OPTION_FIELD_NAME = "optionValue";

  private static final String GROUP_ID_FIELD                     = "groupId";

  private static final String SPACE_ID_FIELD                     = "spaceId";

  private static final String SPACE_GROUP_PREFIX                 = "/spaces/";

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

  @Autowired
  protected ProfilePropertyService  profilePropertyService;

  @Autowired
  protected TranslationService      translationService;

  @Autowired
  protected IdentityManager         identityManager;

  @Autowired
  protected RelationshipManager     relationshipManager;

  @Autowired
  protected ProfileSearchConnector  profileSearchConnector;

  @Autowired
  protected UserPermissionService   userPermissionService;

  @Autowired
  protected OrganizationService     organizationService;

  @Autowired
  protected SpaceService            spaceService;

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
   * <p>
   * On a page, the edit right is the portal's, or a membership matching the
   * page's edit permission: on a space site the portal grants editing to the
   * space layout managers only, whatever the page's edit permission names,
   * while a user that permission names, such as a space publisher, is an
   * editor of the page for its settings. On a site layout, it is the
   * portal's only: a space site's edit permission is the space template's
   * list of layout permissions, not a membership expression.
   *
   * @param pageKey the page resolved from the portal context, may be null
   * @param siteKey the rendering site, may be null
   * @param windowId the storage id of the window being rendered
   * @param username the user asking
   * @return true when the user may store the window's settings
   */
  public boolean canEditSettings(PageKey pageKey, SiteKey siteKey, String windowId, String username) {
    Identity identity = StringUtils.isBlank(username) ? null : userAcl.getUserIdentity(username);
    ModelObject holder = identity == null ? null : getWindowHolder(pageKey, siteKey, windowId);
    return switch (holder) {
    case Page page -> userAcl.hasEditPermission(page, identity)
        || userAcl.isMemberOf(identity, page.getEditPermission());
    case PortalConfig site -> userAcl.hasEditPermission(site, identity);
    case null, default -> false;
    };
  }

  /**
   * Whether a user may see the users a People Directory window lists: the
   * view right of the page, or of the rendered site layout, that holds the
   * window, found as {@link #canEditSettings} finds it.
   *
   * @param pageKey the page resolved from the portal context, may be null
   * @param siteKey the rendering site, may be null
   * @param windowId the storage id of the window
   * @param username the user asking
   * @return true when the user may list the directory
   */
  public boolean canViewDirectory(PageKey pageKey, SiteKey siteKey, String windowId, String username) {
    Identity identity = StringUtils.isBlank(username) ? null : userAcl.getUserIdentity(username);
    ModelObject holder = identity == null ? null : getWindowHolder(pageKey, siteKey, windowId);
    return switch (holder) {
    case Page page -> userAcl.hasAccessPermission(page, identity);
    case PortalConfig site -> userAcl.hasAccessPermission(site, identity);
    case null, default -> false;
    };
  }

  /**
   * Validates a posted attribute filter: a JSON object mapping a profile
   * property name to the values a listed user must hold one of. An empty
   * object means the directory is not filtered by attribute.
   *
   * @param value the posted value
   * @return the value to store, re-serialized, each attribute's values
   *         de-duplicated
   * @throws IllegalArgumentException with {@link #UNKNOWN_ATTRIBUTE_MESSAGE}
   *           when a name is not the name of a directory attribute (a visible,
   *           active, top-level setting, other than email),
   *           {@link #TOO_MANY_ATTRIBUTES_MESSAGE},
   *           {@link #TOO_MANY_VALUES_MESSAGE} or
   *           {@link #VALUE_TOO_LONG_MESSAGE} beyond a bound, and
   *           {@link #ATTRIBUTE_FILTER_INVALID_MESSAGE} when the value is not
   *           such an object, or an attribute has no value or a value that is
   *           not a non-blank text
   */
  public String normalizeAttributeFilter(String value) {
    return toJson(parseAttributeFilter(value));
  }

  /**
   * The stored attribute filter as the directory applies it. A value that
   * fails {@link #normalizeAttributeFilter}, because another writer of the
   * window's preferences stored it or because an attribute is no longer a
   * directory attribute, gives null: the directory then lists nobody.
   *
   * @param storedValue the stored preference value, may be null
   * @return the values per attribute, empty when the directory is not
   *         filtered by attribute, null when it lists nobody
   */
  public Map<String, List<String>> getAttributeFilter(String storedValue) {
    try {
      return parseAttributeFilter(storedValue);
    } catch (IllegalArgumentException e) {
      LOG.debug("The stored attribute filter '{}' is not valid ({}), the directory lists nobody", storedValue, e.getMessage());
      return null; // NOSONAR null means the directory lists nobody
    }
  }

  /**
   * The names of the profile attributes the directory filters on, which a
   * viewer's advanced filter shows greyed out: the names only, never the
   * values. A stored filter the directory cannot read gives none.
   *
   * @param storedValue the stored preference value, may be null
   * @return the attribute names, in their stored order
   */
  public List<String> getDirectoryAttributeNames(String storedValue) {
    Map<String, List<String>> attributeFilter = getAttributeFilter(storedValue);
    return attributeFilter == null ? Collections.emptyList() : List.copyOf(attributeFilter.keySet());
  }

  /**
   * The stored attribute filter as the settings drawer shows it: each entry
   * with its values labelled, and flagged when the attribute is no longer
   * available. Values the directory refuses are left to the drawer to flag;
   * an entry stored with an invalid shape comes back with no value.
   *
   * @param storedValue the stored preference value, may be null
   * @param locale the editor's language, for the option labels
   * @return the attributes, in their stored order
   */
  public List<PeopleDirectoryAttribute> getEditorAttributes(String storedValue, Locale locale) {
    JsonNode stored = readJsonObject(storedValue);
    if (stored == null) {
      return Collections.emptyList();
    }
    List<ProfilePropertySetting> settings = profilePropertyService.getPropertySettings();
    List<PeopleDirectoryAttribute> attributes = new ArrayList<>();
    for (String name : stored.propertyNames()) {
      ProfilePropertySetting setting = getSetting(settings, name);
      List<String> values = toStringValues(stored.get(name));
      // the attribute itself; values the directory refuses are flagged by
      // the drawer, which checks them again as the editor removes some
      boolean available = isDirectoryAttribute(setting);
      List<PeopleDirectoryAttributeValue> labelledValues = values == null ? Collections.emptyList()
                                                                          : values.stream()
                                                                                  .map(v -> toAttributeValue(setting, v, locale))
                                                                                  .toList();
      attributes.add(new PeopleDirectoryAttribute(name, labelledValues, available));
    }
    return attributes;
  }

  /**
   * One page of the users a People Directory window lists to a viewer: the
   * users matching the window's attribute and group filters and the viewer's own
   * criteria, of all the users or of the viewer's connections. The
   * directory's filter is read from the window's stored preferences, never
   * from the request; the viewer's criteria on an attribute the directory
   * filters are ignored, since the directory wins.
   *
   * @param pageKey the page resolved from the portal context, may be null
   * @param siteKey the rendering site, may be null
   * @param windowId the storage id of the window
   * @param storedAttributeFilter the window's stored attribute filter
   * @param storedGroupFilter the window's stored group filter
   * @param storedGroupsToSet the window's stored marker of a directory whose
   *          groups must be set again ({@link #isGroupsToSet}): it then lists
   *          nobody
   * @param storedGuestRestriction the window's stored "Guests see only
   *          connections and space members" setting: any value but
   *          {@code false}, a missing one included, restricts a guest viewer
   *          to the users whose profile they can open
   *          ({@link #isGuestRestriction})
   * @param query the viewer's name search, may be blank
   * @param viewerAttributes the viewer's advanced filter, one text per
   *          attribute, may be null
   * @param connectionsOnly whether only the viewer's connections are listed
   * @param username the viewer
   * @param offset the index of the first user
   * @param limit the number of users, from 1 to {@link #MAX_LIMIT}
   * @return the users of the page and their total number
   * @throws IllegalAccessException when the viewer may not view the page or
   *           site holding the window
   * @throws IllegalArgumentException with {@link #UNKNOWN_ATTRIBUTE_MESSAGE}
   *           when a viewer attribute is not a visible, active, top-level
   *           setting, or {@link #INVALID_RANGE_MESSAGE} for an offset or a
   *           limit out of range
   */
  public PeopleDirectoryPage search(PageKey pageKey, // NOSONAR every parameter is a criterion of the page
                                    SiteKey siteKey,
                                    String windowId,
                                    String storedAttributeFilter,
                                    String storedGroupFilter,
                                    String storedGroupsToSet,
                                    String storedGuestRestriction,
                                    String query,
                                    Map<String, String> viewerAttributes,
                                    boolean connectionsOnly,
                                    String username,
                                    int offset,
                                    int limit) throws IllegalAccessException {
    if (offset < 0 || limit < 1 || limit > MAX_LIMIT) {
      throw new IllegalArgumentException(INVALID_RANGE_MESSAGE);
    }
    if (!canViewDirectory(pageKey, siteKey, windowId, username)) {
      throw new IllegalAccessException("User " + username + " may not view the people directory " + windowId);
    }
    List<ProfilePropertySetting> settings = profilePropertyService.getPropertySettings();
    Map<String, String> viewerSettings = getViewerSettings(settings, viewerAttributes);
    Map<String, List<String>> attributeFilter = getAttributeFilter(storedAttributeFilter);
    if (attributeFilter == null) {
      return new PeopleDirectoryPage(Collections.emptyList(), 0);
    }
    attributeFilter.keySet().forEach(viewerSettings::remove);

    ProfileFilter filter = new ProfileFilter();
    filter.setName(StringUtils.defaultString(StringUtils.trim(query)));
    filter.setEnabled(true);
    if (!viewerSettings.isEmpty()) {
      filter.setProfileSettings(viewerSettings);
    }
    List<String> groupFilter = getGroupFilter(storedGroupFilter, storedGroupsToSet);
    if (!attributeFilter.isEmpty() || groupFilter != null) {
      PeopleDirectoryFilter directoryFilter = new PeopleDirectoryFilter(toIndexedValues(settings, attributeFilter));
      directoryFilter.setGroups(groupFilter);
      filter.setDirectoryFilter(directoryFilter);
    }
    if (isGuestRestriction(storedGuestRestriction) && !restrictGuest(filter, username)) {
      return new PeopleDirectoryPage(Collections.emptyList(), 0);
    }
    ListAccess<org.exoplatform.social.core.identity.model.Identity> users;
    if (connectionsOnly) {
      org.exoplatform.social.core.identity.model.Identity viewer = identityManager.getOrCreateUserIdentity(username);
      users = relationshipManager.getConnectionsByFilter(viewer, filter);
    } else {
      users = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, true);
    }
    try {
      org.exoplatform.social.core.identity.model.Identity[] identities = users.load(offset, limit);
      List<org.exoplatform.social.core.identity.model.Identity> page = identities == null ? Collections.emptyList()
                                                                                          : Arrays.stream(identities)
                                                                                                  .filter(Objects::nonNull)
                                                                                                  .toList();
      return new PeopleDirectoryPage(page, users.getSize());
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException("Error while listing the people directory " + windowId, e);
    }
  }


  /**
   * Whether a user may set the group filter of a People Directory window: a
   * platform administrator, or a manager of at least one group, space or
   * organizational unit (see {@link #getManagedGroupIds}).
   *
   * @param username the user asking, may be null
   * @return true when the group filter is offered to the user
   */
  public boolean canSetGroupFilter(String username) {
    Identity identity = StringUtils.isBlank(username) ? null : userAcl.getUserIdentity(username);
    return identity != null && (userAcl.isAdministrator(identity) || !getManagedGroupIds(username).isEmpty());
  }

  /**
   * The groups an editor can select for a People Directory window's group
   * filter: any group for a platform administrator, the groups the editor
   * manages otherwise, each matched on its label or its id, ignoring case; at
   * most
   * {@link #MAX_GROUPS_SUGGESTED}, the groups the drawer already holds left
   * out before the bound applies.
   *
   * @param pageKey the page resolved from the portal context, may be null
   * @param siteKey the rendering site, may be null
   * @param windowId the storage id of the window
   * @param query a text the groups must contain, may be blank
   * @param excludedGroupIds the groups the drawer already holds, at most
   *          {@link #MAX_GROUPS} of them read, may be null
   * @param username the editor
   * @return the groups, labelled
   * @throws IllegalAccessException when the user may not edit the window's
   *           settings or may not set its group filter
   */
  public List<PeopleDirectoryGroup> getSelectableGroups(PageKey pageKey,
                                                        SiteKey siteKey,
                                                        String windowId,
                                                        String query,
                                                        Collection<String> excludedGroupIds,
                                                        String username) throws IllegalAccessException {
    if (!canEditSettings(pageKey, siteKey, windowId, username) || !canSetGroupFilter(username)) {
      throw new IllegalAccessException("User " + username + " may not set the group filter of the people directory " + windowId);
    }
    String text = StringUtils.left(StringUtils.trimToEmpty(query), MAX_GROUP_QUERY_LENGTH);
    Set<String> excluded = excludedGroupIds == null ? Collections.emptySet()
                                                    : excludedGroupIds.stream()
                                                                      .filter(StringUtils::isNotBlank)
                                                                      .limit(MAX_GROUPS)
                                                                      .collect(Collectors.toSet());
    Stream<Group> candidates = isAdministrator(username) ? getAllGroups().stream()
                                                         : getManagedGroupIds(username).stream()
                                                                                       .map(this::findGroup)
                                                                                       .filter(Objects::nonNull);
    return candidates.filter(group -> !excluded.contains(group.getId()))
                     .map(this::toDirectoryGroup)
                     .filter(Objects::nonNull)
                     .filter(group -> StringUtils.containsIgnoreCase(group.label(), text)
                         || StringUtils.containsIgnoreCase(group.groupId(), text))
                     .limit(MAX_GROUPS_SUGGESTED)
                     .toList();
  }

  /**
   * Validates a posted group filter and merges it with the stored one, for
   * the editor saving it. The filter of an editor who may not set it is kept
   * as stored, whatever is posted. For an editor who may, the stored groups
   * the editor cannot select stay applied, and the posted ones replace the
   * others; an empty post turns the editor's part of the filter OFF and drops
   * the stored groups that no longer exist. A space's group is stored with
   * its space's id, resolved here, never posted.
   *
   * @param posted the posted value: a JSON array of group ids, {@code []}
   *          when the editor's group filter is OFF
   * @param stored the stored preference value, may be null
   * @param username the editor
   * @return the value to store: a JSON array of {@code {groupId, spaceId}}
   * @throws IllegalArgumentException with {@link #GROUP_FILTER_INVALID_MESSAGE}
   *           when the post is not such an array,
   *           {@link #GROUP_NOT_SELECTABLE_MESSAGE} when a posted group is
   *           not one the editor can select, or {@link #TOO_MANY_GROUPS_MESSAGE}
   *           beyond {@link #MAX_GROUPS} existing groups
   */
  public String normalizeGroupFilter(String posted, String stored, String username) {
    if (!canSetGroupFilter(username)) {
      return StringUtils.defaultIfBlank(stored, EMPTY_JSON_ARRAY);
    }
    List<String> postedIds = readPostedGroupIds(posted);
    boolean administrator = isAdministrator(username);
    Set<String> managedGroupIds = administrator ? Collections.emptySet() : getManagedGroupIds(username);
    List<DirectoryGroup> groups = new ArrayList<>();
    for (DirectoryGroup group : readStoredGroups(stored, false)) {
      boolean resolvable = isResolvable(group);
      if (!(resolvable && isSelectable(group.groupId(), administrator, managedGroupIds))
          && (resolvable || !postedIds.isEmpty())) {
        groups.add(group);
      }
    }
    for (String groupId : postedIds) {
      if (!isSelectable(groupId, administrator, managedGroupIds)) {
        throw new IllegalArgumentException(GROUP_NOT_SELECTABLE_MESSAGE);
      }
      // a posted group replaces the stored entry of the same id, which can
      // only be one that no longer resolves here: a space re-created under
      // the same name gets its new id
      groups.removeIf(group -> group.groupId().equals(groupId));
      groups.add(new DirectoryGroup(groupId, getSpaceId(groupId)));
    }
    if (groups.stream().filter(this::isResolvable).count() > MAX_GROUPS) {
      throw new IllegalArgumentException(TOO_MANY_GROUPS_MESSAGE);
    }
    return toGroupsJson(groups);
  }

  /**
   * The stored group filter as the directory applies it: the ids of the
   * stored groups that still exist. A stored group that no longer exists, or
   * a space's group whose space was deleted, matches nobody, and a value that
   * is not a stored group filter lists nobody. A window carrying the marker
   * of a directory whose groups must be set again ({@link #isGroupsToSet}), a
   * directory made from a template, lists nobody until a user who may set
   * groups saves its group filter.
   *
   * @param storedValue the stored group filter, may be null
   * @param storedGroupsToSet the stored marker, may be null
   * @return null when the directory is not filtered by group, the ids of the
   *         groups a listed user must belong to one of otherwise, empty when
   *         it lists nobody
   */
  public List<String> getGroupFilter(String storedValue, String storedGroupsToSet) {
    if (isGroupsToSet(storedGroupsToSet)) {
      LOG.debug("The directory's groups must be set again, it lists nobody");
      return Collections.emptyList();
    }
    List<DirectoryGroup> groups = readStoredGroups(storedValue, true);
    if (groups == null) {
      LOG.debug("The stored group filter '{}' is not valid, the directory lists nobody", storedValue);
      return Collections.emptyList();
    } else if (groups.isEmpty()) {
      return null; // NOSONAR null means the directory is not filtered by group
    }
    List<String> groupIds = new ArrayList<>();
    for (DirectoryGroup group : groups) {
      if (isResolvable(group)) {
        groupIds.add(group.groupId());
      } else {
        LOG.debug("The stored group '{}' of a directory's group filter no longer exists, it matches nobody", group.groupId());
      }
    }
    return groupIds;
  }

  /**
   * The stored group filter as the settings drawer shows it to an editor: the
   * groups the editor can select, as chips, whether other groups are
   * applied, never named, for an editor who may set the filter how many,
   * which count against {@link #MAX_GROUPS}, and whether
   * stored groups no longer exist or the stored value cannot be read, so that
   * the filter shows as set although no chip remains, and turning it OFF
   * clears it. The marker of a directory whose groups must be set again
   * ({@link #isGroupsToSet}) shows to an editor who may set groups as stored
   * groups that no longer exist.
   *
   * @param storedValue the stored group filter, may be null
   * @param storedGroupsToSet the stored marker, may be null
   * @param username the editor
   * @return the editor's groups
   */
  public PeopleDirectoryEditorGroups getEditorGroups(String storedValue, String storedGroupsToSet, String username) {
    List<DirectoryGroup> stored = readStoredGroups(storedValue, false);
    boolean canSetGroupFilter = canSetGroupFilter(username);
    boolean administrator = canSetGroupFilter && isAdministrator(username);
    Set<String> managedGroupIds = !canSetGroupFilter || administrator ? Collections.emptySet()
                                                                      : getManagedGroupIds(username);
    List<PeopleDirectoryGroup> groups = new ArrayList<>();
    int otherGroupsCount = 0;
    // a value the directory cannot read lists nobody (getGroupFilter)
    boolean groupFilterInvalid = canSetGroupFilter && readStoredGroups(storedValue, true) == null;
    boolean unresolvableGroups = groupFilterInvalid || (canSetGroupFilter && isGroupsToSet(storedGroupsToSet));
    for (DirectoryGroup group : stored) {
      if (!isResolvable(group)) {
        unresolvableGroups = canSetGroupFilter;
        continue;
      }
      if (canSetGroupFilter && isSelectable(group.groupId(), administrator, managedGroupIds)) {
        PeopleDirectoryGroup directoryGroup = toDirectoryGroup(findGroup(group.groupId()));
        if (directoryGroup != null) {
          groups.add(directoryGroup);
        }
      } else {
        otherGroupsCount++;
      }
    }
    return new PeopleDirectoryEditorGroups(groups,
                                           otherGroupsCount > 0,
                                           canSetGroupFilter ? otherGroupsCount : 0,
                                           unresolvableGroups,
                                           groupFilterInvalid);
  }

  /**
   * @param storedValue the stored marker of a directory whose groups must be
   *          set again, may be null
   * @return whether the marker is set: a directory made from a template
   *         carries it, since a template does not keep the groups of its
   *         directories
   */
  public boolean isGroupsToSet(String storedValue) {
    return Boolean.TRUE.toString().equals(StringUtils.trim(storedValue));
  }

  /**
   * Whether a save clears the marker of a directory whose groups must be set
   * again: only a save by an editor who may set groups that posts a group
   * filter, groups or the filter OFF, clears it; any other save keeps it, so
   * that the directory keeps listing nobody.
   *
   * @param postedGroupFilter the posted group filter, null when none is
   *          posted
   * @param username the editor saving
   * @return true when the marker is to be removed
   */
  public boolean clearsGroupsToSet(String postedGroupFilter, String username) {
    return postedGroupFilter != null && canSetGroupFilter(username);
  }

  /**
   * @param username a user
   * @return the ids of the groups the user directly holds the manager
   *         membership type, or {@code *}, of: a membership inherited through
   *         a nested-group link does not make a manager
   */
  public Set<String> getManagedGroupIds(String username) {
    if (StringUtils.isBlank(username)) {
      return Collections.emptySet();
    }
    String managerType = userAcl.getAdminMSType();
    Set<String> groupIds = new LinkedHashSet<>();
    for (String token : userPermissionService.getPermissionTokens(username)) {
      int separator = token == null ? -1 : token.indexOf(":/");
      if (separator <= 0 || token.startsWith(UserPermissionService.INHERITED_TOKEN_PREFIX)) {
        continue;
      }
      String membershipType = token.substring(0, separator);
      if (StringUtils.equals(membershipType, managerType)
          || StringUtils.equals(membershipType, UserPermissionService.ANY_MEMBERSHIP_TYPE)) {
        groupIds.add(token.substring(separator + 1));
      }
    }
    return groupIds;
  }

  /**
   * The values an editor can select for an attribute of a People Directory
   * window's filter: a dropdown property's options, or the distinct values
   * the enabled users hold, owner-hidden values excluded, at most
   * {@link #MAX_ATTRIBUTE_VALUES_SUGGESTED}.
   *
   * @param pageKey the page resolved from the portal context, may be null
   * @param siteKey the rendering site, may be null
   * @param windowId the storage id of the window
   * @param attribute the profile property name
   * @param query a text the values must contain, may be blank
   * @param username the editor
   * @param locale the editor's language, for the option labels
   * @return the values
   * @throws IllegalAccessException when the user may not edit the window's
   *           settings
   * @throws IllegalArgumentException with {@link #UNKNOWN_ATTRIBUTE_MESSAGE}
   *           when the attribute is not a directory attribute
   */
  public List<PeopleDirectoryAttributeValue> getAttributeValues(PageKey pageKey, // NOSONAR the window, the attribute and the editor
                                                                SiteKey siteKey,
                                                                String windowId,
                                                                String attribute,
                                                                String query,
                                                                String username,
                                                                Locale locale) throws IllegalAccessException {
    if (!canEditSettings(pageKey, siteKey, windowId, username)) {
      throw new IllegalAccessException("User " + username + " may not edit the people directory " + windowId);
    }
    ProfilePropertySetting setting = getSetting(profilePropertyService.getPropertySettings(), attribute);
    if (!isDirectoryAttribute(setting)) {
      throw new IllegalArgumentException(UNKNOWN_ATTRIBUTE_MESSAGE);
    }
    String text = StringUtils.trimToEmpty(query);
    if (setting.isDropdownList() && CollectionUtils.isNotEmpty(setting.getPropertyOptions())) {
      return setting.getPropertyOptions()
                    .stream()
                    .filter(option -> option != null && option.getId() != null)
                    .map(option -> toAttributeValue(setting, String.valueOf(option.getId()), locale))
                    .filter(value -> StringUtils.containsIgnoreCase(value.label(), text))
                    .limit(MAX_ATTRIBUTE_VALUES_SUGGESTED)
                    .toList();
    }
    return profileSearchConnector.getAttributeValues(setting.getPropertyName(),
                                                     setting.isMultiValued(),
                                                     StringUtils.left(text, MAX_VALUE_LENGTH),
                                                     MAX_ATTRIBUTE_VALUES_SUGGESTED)
                                 .stream()
                                 // a value the filter cannot store is not offered
                                 .filter(value -> value.length() <= MAX_VALUE_LENGTH)
                                 .map(value -> new PeopleDirectoryAttributeValue(value, value))
                                 .toList();
  }

  /**
   * @param setting a profile property setting, may be null
   * @return whether a directory can filter on the property: a visible,
   *         active, top-level setting, other than email, whose value stays
   *         indexed for the name search when its owner hides it
   */
  public static boolean isDirectoryAttribute(ProfilePropertySetting setting) {
    return isSelectableAttribute(setting) && !EMAIL_PROPERTY.equals(setting.getPropertyName());
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
   * Whether the stored "Guests see only connections and space members" setting
   * restricts guest viewers. It fails closed: any value but the literal
   * {@code false}, a missing or unreadable one included, is ON.
   *
   * @param storedValue the stored preference value, may be null
   * @return true unless the setting is OFF
   */
  public boolean isGuestRestriction(String storedValue) {
    return !Boolean.FALSE.toString().equals(StringUtils.trim(storedValue));
  }

  /**
   * Validates a posted "Guests see only connections and space members"
   * setting, stored as {@code true} or {@code false}.
   *
   * @param value the posted value
   * @return {@code false} for a posted {@code false}, {@code true} otherwise
   */
  public String normalizeGuestRestriction(String value) {
    return String.valueOf(isGuestRestriction(value));
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
    return Math.clamp(pageSize, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
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

  /**
   * Restricts the filter to the users a guest viewer can see under the
   * {@link UserAclPlugin} {@code VIEW} rule: themself, their confirmed
   * connections and the members of their spaces. The viewer is a guest when
   * their {@link UserACL} identity holds a membership of
   * {@code /platform/externals}; any other viewer is left unrestricted.
   *
   * @return false when the viewer is a guest whose social identity cannot be
   *         resolved, who then sees nobody
   */
  private boolean restrictGuest(ProfileFilter filter, String username) {
    Identity aclIdentity = userAcl.getUserIdentity(username);
    if (!UserAclPlugin.isGuest(aclIdentity)) {
      return true;
    }
    org.exoplatform.social.core.identity.model.Identity guest = identityManager.getOrCreateUserIdentity(username);
    if (guest == null || StringUtils.isBlank(guest.getId())) {
      return false;
    }
    filter.setViewerSpaceGroupIds(UserAclPlugin.getGuestVisibleGroupIds(aclIdentity));
    filter.setGuestIdentityId(Long.parseLong(guest.getId()));
    return true;
  }

  private boolean isAdministrator(String username) {
    Identity identity = userAcl.getUserIdentity(username);
    return identity != null && userAcl.isAdministrator(identity);
  }

  private boolean isSelectable(String groupId, boolean administrator, Set<String> managedGroupIds) {
    return administrator ? findGroup(groupId) != null : managedGroupIds.contains(groupId);
  }

  /**
   * @return whether a stored group still designates the group it was saved
   *         with: the group exists and, for a space's group, its space is the
   *         one stored
   */
  private boolean isResolvable(DirectoryGroup group) {
    if (findGroup(group.groupId()) == null) {
      return false;
    } else if (!StringUtils.startsWith(group.groupId(), SPACE_GROUP_PREFIX)) {
      return true;
    }
    return group.spaceId() != null && StringUtils.equals(group.spaceId(), getSpaceId(group.groupId()));
  }

  private String getSpaceId(String groupId) {
    if (!StringUtils.startsWith(groupId, SPACE_GROUP_PREFIX)) {
      return null;
    }
    Space space = spaceService.getSpaceByGroupId(groupId);
    return space == null ? null : space.getId();
  }

  private Group findGroup(String groupId) {
    if (StringUtils.isBlank(groupId) || !groupId.startsWith("/")) {
      return null;
    }
    try {
      return organizationService.getGroupHandler().findGroupById(groupId);
    } catch (Exception e) {
      LOG.warn("Error while reading the group {}", groupId, e);
      return null;
    }
  }

  /**
   * Every group, for an administrator's search on labels: the store's own
   * search matches a group's name only.
   */
  private Collection<Group> getAllGroups() {
    try {
      Collection<Group> groups = organizationService.getGroupHandler().getAllGroups();
      return groups == null ? Collections.emptyList() : groups;
    } catch (Exception e) {
      LOG.warn("Error while reading the groups", e);
      return Collections.emptyList();
    }
  }

  private PeopleDirectoryGroup toDirectoryGroup(Group group) {
    if (group == null) {
      return null;
    }
    String label = null;
    if (StringUtils.startsWith(group.getId(), SPACE_GROUP_PREFIX)) {
      Space space = spaceService.getSpaceByGroupId(group.getId());
      label = space == null ? null : space.getDisplayName();
    }
    return new PeopleDirectoryGroup(group.getId(),
                                    StringUtils.firstNonBlank(label, group.getLabel(), group.getGroupName(), group.getId()));
  }

  /**
   * @return the posted group ids, de-duplicated
   * @throws IllegalArgumentException with {@link #GROUP_FILTER_INVALID_MESSAGE}
   *           when the value is not a JSON array of non-blank texts, or
   *           {@link #TOO_MANY_GROUPS_MESSAGE} beyond {@link #MAX_GROUPS}
   */
  private static List<String> readPostedGroupIds(String value) {
    JsonNode posted;
    try {
      posted = JSON_MAPPER.readTree(StringUtils.defaultIfBlank(value, EMPTY_JSON_ARRAY));
    } catch (JacksonException e) {
      throw new IllegalArgumentException(GROUP_FILTER_INVALID_MESSAGE, e);
    }
    List<String> groupIds = toStringValues(posted);
    if (groupIds == null || groupIds.stream().anyMatch(StringUtils::isBlank)) {
      throw new IllegalArgumentException(GROUP_FILTER_INVALID_MESSAGE);
    }
    List<String> distinct = groupIds.stream().distinct().toList();
    if (distinct.size() > MAX_GROUPS) {
      throw new IllegalArgumentException(TOO_MANY_GROUPS_MESSAGE);
    }
    return distinct;
  }

  /**
   * @param strict whether a value that is not a stored group filter gives
   *          null rather than no group
   * @return the stored groups, empty for a blank value
   */
  private static List<DirectoryGroup> readStoredGroups(String value, boolean strict) {
    JsonNode stored;
    try {
      stored = JSON_MAPPER.readTree(StringUtils.defaultIfBlank(value, EMPTY_JSON_ARRAY));
    } catch (JacksonException e) {
      stored = null;
    }
    List<DirectoryGroup> groups = new ArrayList<>();
    if (stored == null || !stored.isArray()) {
      return strict ? null : groups; // NOSONAR null means an invalid value
    }
    for (JsonNode element : stored) {
      JsonNode groupId = element.get(GROUP_ID_FIELD);
      JsonNode spaceId = element.get(SPACE_ID_FIELD);
      if (!element.isObject() || groupId == null || !groupId.isString() || StringUtils.isBlank(groupId.asString())
          || (spaceId != null && !spaceId.isNull() && !spaceId.isString())) {
        if (strict) {
          return null; // NOSONAR null means an invalid value
        }
        continue;
      }
      groups.add(new DirectoryGroup(groupId.asString(), spaceId == null || spaceId.isNull() ? null : spaceId.asString()));
    }
    return groups;
  }

  private static String toGroupsJson(List<DirectoryGroup> groups) {
    List<Map<String, String>> entries = new ArrayList<>();
    for (DirectoryGroup group : groups) {
      Map<String, String> entry = new LinkedHashMap<>();
      entry.put(GROUP_ID_FIELD, group.groupId());
      entry.put(SPACE_ID_FIELD, group.spaceId());
      entries.add(entry);
    }
    return JSON_MAPPER.writeValueAsString(entries);
  }

  private static boolean isSelectableAttribute(ProfilePropertySetting setting) {
    return setting != null && setting.isVisible() && setting.isActive() && setting.getParentId() == null;
  }

  private Map<String, List<String>> parseAttributeFilter(String value) {
    JsonNode posted = readJsonObject(value);
    if (posted == null) {
      throw new IllegalArgumentException(ATTRIBUTE_FILTER_INVALID_MESSAGE);
    } else if (posted.size() > MAX_ATTRIBUTES) {
      throw new IllegalArgumentException(TOO_MANY_ATTRIBUTES_MESSAGE);
    }
    List<ProfilePropertySetting> settings = profilePropertyService.getPropertySettings();
    Map<String, List<String>> attributeFilter = new LinkedHashMap<>();
    for (String name : posted.propertyNames()) {
      if (!isDirectoryAttribute(getSetting(settings, name))) {
        throw new IllegalArgumentException(UNKNOWN_ATTRIBUTE_MESSAGE);
      }
      List<String> values = toStringValues(posted.get(name));
      String error = getValuesError(values);
      if (error != null) {
        throw new IllegalArgumentException(error);
      }
      attributeFilter.put(name, values.stream().distinct().toList());
    }
    return attributeFilter;
  }

  /**
   * @return the message code refusing an attribute's values, null when they
   *         are valid: a non-empty list of non-blank texts of at most
   *         {@link #MAX_VALUE_LENGTH} characters, at most
   *         {@link #MAX_VALUES_PER_ATTRIBUTE} distinct ones
   */
  private static String getValuesError(List<String> values) {
    if (values == null || values.isEmpty() || values.stream().anyMatch(PeopleDirectoryService::isBlankValue)) {
      return ATTRIBUTE_FILTER_INVALID_MESSAGE;
    } else if (values.stream().anyMatch(v -> v.length() > MAX_VALUE_LENGTH)) {
      return VALUE_TOO_LONG_MESSAGE;
    } else if (values.stream().distinct().count() > MAX_VALUES_PER_ATTRIBUTE) {
      return TOO_MANY_VALUES_MESSAGE;
    }
    return null;
  }

  /**
   * @return whether a value holds only white space, as the settings drawer's
   *         {@code trim()} reads it: a no-break space or a byte order mark
   *         included, the separators U+001C to U+001F excluded
   */
  private static boolean isBlankValue(String value) {
    return value.codePoints()
                .allMatch(c -> (Character.isWhitespace(c) && (c < 0x1C || c > 0x1F)) || Character.isSpaceChar(c) || c == 0xFEFF);
  }

  /**
   * @return the texts of a JSON array holding texts only, null for any other
   *         value (null, a text, an array holding a null or a number)
   */
  private static List<String> toStringValues(JsonNode value) {
    if (value == null || !value.isArray()) {
      return null; // NOSONAR null means an invalid shape
    }
    List<String> values = new ArrayList<>();
    for (JsonNode element : value) {
      if (!element.isString()) {
        return null; // NOSONAR null means an invalid shape
      }
      values.add(element.asString());
    }
    return values;
  }

  /**
   * @return the JSON object a value holds, keeping its keys' order, {}
   *         for a blank value, null when the value is not a JSON object
   */
  private static JsonNode readJsonObject(String value) {
    try {
      JsonNode json = JSON_MAPPER.readTree(StringUtils.defaultIfBlank(value, EMPTY_JSON_OBJECT));
      return json != null && json.isObject() ? json : null;
    } catch (JacksonException e) {
      return null;
    }
  }

  private static String toJson(Map<String, List<String>> attributeFilter) {
    return JSON_MAPPER.writeValueAsString(attributeFilter);
  }

  private Map<String, String> getViewerSettings(List<ProfilePropertySetting> settings, Map<String, String> viewerAttributes) {
    Map<String, String> viewerSettings = new HashMap<>();
    if (viewerAttributes == null) {
      return viewerSettings;
    }
    viewerAttributes.forEach((name, value) -> {
      if (!isSelectableAttribute(getSetting(settings, name))) {
        throw new IllegalArgumentException(UNKNOWN_ATTRIBUTE_MESSAGE);
      }
      if (StringUtils.isNotBlank(value)) {
        viewerSettings.put(name, value.trim());
      }
    });
    return viewerSettings;
  }

  private Map<String, List<String>> toIndexedValues(List<ProfilePropertySetting> settings,
                                                    Map<String, List<String>> attributeFilter) {
    Map<String, List<String>> indexedValues = new LinkedHashMap<>();
    attributeFilter.forEach((name, values) -> {
      ProfilePropertySetting setting = getSetting(settings, name);
      indexedValues.put(name,
                        values.stream()
                              .map(value -> ProfileIndexingServiceConnector.toIndexedValue(setting,
                                                                                           value,
                                                                                           setting.isMultiValued(),
                                                                                           translationService))
                              .distinct()
                              .toList());
    });
    return indexedValues;
  }

  private PeopleDirectoryAttributeValue toAttributeValue(ProfilePropertySetting setting, String value, Locale locale) {
    ProfilePropertyOption option = getDropdownOption(setting, value);
    if (option == null) {
      return new PeopleDirectoryAttributeValue(value, value);
    }
    String label = null;
    try {
      label = translationService.getTranslationLabelOrDefault(PROFILE_PROPERTY_OPTION_OBJECT_TYPE,
                                                              option.getId(),
                                                              PROFILE_PROPERTY_OPTION_FIELD_NAME,
                                                              locale);
    } catch (Exception e) {
      LOG.debug("No label for the option {} of the profile property {}", option.getId(), setting.getPropertyName(), e);
    }
    return new PeopleDirectoryAttributeValue(value, StringUtils.defaultIfBlank(label, option.getValue()));
  }

  /**
   * @return the dropdown option a stored value designates, null for a value
   *         typed before the property became a dropdown
   */
  private static ProfilePropertyOption getDropdownOption(ProfilePropertySetting setting, String value) {
    if (setting == null || !setting.isDropdownList() || !StringUtils.isNumeric(value)
        || CollectionUtils.isEmpty(setting.getPropertyOptions())) {
      return null;
    }
    long optionId;
    try {
      optionId = Long.parseLong(value);
    } catch (NumberFormatException e) {
      return null;
    }
    return setting.getPropertyOptions()
                  .stream()
                  .filter(option -> option != null && option.getId() != null && option.getId() == optionId)
                  .findFirst()
                  .orElse(null);
  }

  /**
   * Looks a setting up in the full list: a lookup by an unknown name caches
   * the miss and evicts real settings.
   */
  private static ProfilePropertySetting getSetting(List<ProfilePropertySetting> settings, String name) {
    return settings.stream().filter(setting -> StringUtils.equals(setting.getPropertyName(), name)).findFirst().orElse(null);
  }

  private ModelObject getWindowHolder(PageKey pageKey, SiteKey siteKey, String windowId) {
    if (StringUtils.isBlank(windowId)) {
      return null;
    }
    Page page = pageKey == null ? null : layoutService.getPage(pageKey);
    if (page != null && holdsWindow(page, windowId)) {
      return page;
    }
    PortalConfig site = getRenderedSite(siteKey);
    if (site != null && holdsWindow(site.getPortalLayout(), windowId)) {
      return site;
    }
    return null;
  }

  private PortalConfig getRenderedSite(SiteKey siteKey) {
    PortalConfig site = siteKey == null ? null : layoutService.getPortalConfig(siteKey);
    if (site != null && site.isDisplayed()) {
      return userPortalConfigService.getMetaPortalConfig();
    }
    return site;
  }

  private boolean holdsWindow(ModelObject modelObject, String windowId) {
    return switch (modelObject) {
    case Application application -> StringUtils.equals(application.getStorageId(), windowId);
    case Container container -> CollectionUtils.isNotEmpty(container.getChildren())
        && container.getChildren().stream().anyMatch(child -> holdsWindow(child, windowId));
    case null, default -> false;
    };
  }

}
