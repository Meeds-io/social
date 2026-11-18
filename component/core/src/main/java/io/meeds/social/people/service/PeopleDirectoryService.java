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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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

import io.meeds.social.people.model.PeopleDirectoryAttribute;
import io.meeds.social.people.model.PeopleDirectoryAttributeValue;
import io.meeds.social.people.model.PeopleDirectoryFilter;
import io.meeds.social.people.model.PeopleDirectoryPage;
import io.meeds.social.translation.service.TranslationService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Rules of the People Directory application instances (EXO-93255,
 * EXO-93256): who may change an instance's settings or list its users, which
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

  private static final Log    LOG                                = ExoLogger.getLogger(PeopleDirectoryService.class);

  private static final String EMAIL_PROPERTY                     = "email";

  private static final JsonMapper JSON_MAPPER                    = new JsonMapper();

  private static final String PROFILE_PROPERTY_OPTION_OBJECT_TYPE = "propertySettingOption";

  private static final String PROFILE_PROPERTY_OPTION_FIELD_NAME = "optionValue";

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
    Identity identity = StringUtils.isBlank(username) ? null : userAcl.getUserIdentity(username);
    ModelObject holder = identity == null ? null : getWindowHolder(pageKey, siteKey, windowId);
    return switch (holder) {
    case Page page -> userAcl.hasEditPermission(page, identity);
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
   * users matching the window's attribute filter and the viewer's own
   * criteria, of all the users or of the viewer's connections. The
   * directory's filter is read from the window's stored preferences, never
   * from the request; the viewer's criteria on an attribute the directory
   * filters are ignored, since the directory wins.
   *
   * @param pageKey the page resolved from the portal context, may be null
   * @param siteKey the rendering site, may be null
   * @param windowId the storage id of the window
   * @param storedAttributeFilter the window's stored attribute filter
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
    if (!attributeFilter.isEmpty()) {
      filter.setDirectoryFilter(new PeopleDirectoryFilter(toIndexedValues(settings, attributeFilter)));
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
