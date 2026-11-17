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
package org.exoplatform.social.core.jpa.search;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.relationship.model.Relationship;

import io.meeds.social.identity.permission.service.UserPermissionService;

public class ProfileIndexingServiceConnector extends ElasticIndexingServiceConnector {

  public static final String           TYPE                         = "profile";

  private static final Log             LOG                          = ExoLogger.getLogger(ProfileIndexingServiceConnector.class);

  private final IdentityManager        identityManager;

  private final ConnectionDAO          connectionDAO;

  private final IdentityDAO            identityDAO;

  private final ProfilePropertyService profilePropertyService;

  private final TranslationService     translationService;

  private final UserACL                userACL;

  private final UserPermissionService  userPermissionService;

  private static final String          POSITION_PROPERTY            = "position";

  private static final String          PROFILE_PROPERTY_FIELD_NAME  = "optionValue";

  private static final String          PROFILE_PROPERTY_OBJECT_TYPE = "propertySettingOption";

  public ProfileIndexingServiceConnector(InitParams initParams,
                                         IdentityManager identityManager,
                                         IdentityDAO identityDAO,
                                         ConnectionDAO connectionDAO,
                                         ProfilePropertyService profilePropertyService,
                                         TranslationService translationService,
                                         UserACL userACL,
                                         UserPermissionService userPermissionService) {
    super(initParams);
    this.identityManager = identityManager;
    this.identityDAO = identityDAO;
    this.connectionDAO = connectionDAO;
    this.profilePropertyService = profilePropertyService;
    this.translationService = translationService;
    this.userACL = userACL;
    this.userPermissionService = userPermissionService;
  }

  /**
   * @param propertyName name of a profile property setting
   * @return the name of the profile index field holding that property's value:
   *         a dot, which Elasticsearch reads as an object path, becomes an
   *         underscore. Every query on a property value uses this field name.
   */
  public static String getIndexedFieldName(String propertyName) {
    return propertyName.replace(".", "_");
  }

  @Override
  public Document create(String id) {
    return getDocument(id);
  }

  @Override
  public Document update(String id) {
    return getDocument(id);
  }

  @Override
  public String getConnectorName() {
    return TYPE;
  }

  private String buildConnectionString(Identity identity, Relationship.Type type) {
    StringBuilder sb = new StringBuilder();

    final int limit = 200;
    List<Long> list = null;
    long id = Long.parseLong(identity.getId());

    boolean inSender = true;
    boolean inReceiver = true;

    if (type == Relationship.Type.OUTGOING) {
      inSender = false;
      type = Relationship.Type.PENDING;

    } else if (type == Relationship.Type.INCOMING) {
      inReceiver = false;
      type = Relationship.Type.PENDING;
    }

    if (inSender) {
      int offset = 0;
      do {
        list = connectionDAO.getSenderIds(id, type, offset, limit);
        sb = append(sb, list);
        offset += limit;
      } while (list != null && list.size() >= limit);
    }

    if (inReceiver) {
      int offset = 0;
      do {
        list = connectionDAO.getReceiverIds(id, type, offset, limit);
        sb = append(sb, list);
        offset += limit;
      } while (list != null && list.size() >= limit);
    }

    // Remove the last ","
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }

    return sb.toString();
  }

  private StringBuilder append(StringBuilder sb, List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return sb;
    }
    int len = ids.size() * 10;

    if (sb.capacity() < sb.length() + len) {
      sb = new StringBuilder(sb.capacity() + len).append(sb);
    }
    for (Long id : ids) {
      sb.append(id).append(",");
    }
    return sb;
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    List<Long> ids = identityDAO.getAllIdsByProvider(OrganizationIdentityProvider.NAME, offset, limit);

    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    } else {
      List<String> result = new ArrayList<>(ids.size());
      for (Long id : ids) {
        result.add(String.valueOf(id));
      }
      return result;
    }
  }

  @Override
  public String getMapping() {
    StringBuilder profileSettingsFieldsMapping = new StringBuilder();
    for(ProfilePropertySetting propertySetting : profilePropertyService.getPropertySettings()) {
      if(propertySetting.isVisible() && propertySetting.isEditable() && !propertySetting.isMultiValued() && propertySetting.getParentId() == null && !propertySetting.isHasChildProperties() && !"email".equals(propertySetting.getPropertyName())) {
        profileSettingsFieldsMapping.append("    \"").append(propertySetting.getPropertyName().equals("fullName")? "name" : propertySetting.getPropertyName()).append("\" : {")
                .append("      \"type\" : \"text\",")
                .append("      \"index_options\": \"offsets\",")
                .append("      \"fields\": {")
                .append("        \"raw\": {")
                .append("          \"type\": \"keyword\"")
                .append("        },")
                .append("        \"whitespace\": {")
                .append("          \"type\": \"text\",")
                .append("          \"analyzer\": \"whitespace_lowercase_asciifolding\"")
                .append("        }")
                .append("      }")
                .append("    },\n");
      }
    }
    StringBuilder mapping = new StringBuilder()
            .append("{")
            .append("""
                      "dynamic_templates": [
                        {
                        "strings_dynamic_mapping": {
                            "match_mapping_type": "string",
                            "mapping": {
                              "type": "text",
                              "fields": {
                                "raw": {
                                "type": "keyword"
                              },
                              "whitespace": {
                                "type": "text",
                                "analyzer": "whitespace_lowercase_asciifolding"
                              }
                            },
                            "index_options": "offsets"
                            }
                          }
                        }
                      ],
                    """)
            .append("  \"properties\" : {\n")
            .append("    \"userName\" : {\"type\" : \"keyword\"},\n")
            .append(profileSettingsFieldsMapping)
            .append("    \"email\" : {\"type\" : \"keyword\"},\n")
            .append("    \"permissions\" : {\"type\" : \"keyword\"},\n")
            .append("    \"connections\" : {\"type\" : \"long\"},\n")
            .append("    \"avatarUrl\" : {\"type\" : \"text\", \"index\": false},\n")
            .append("    \"skills\" : {\"type\" : \"text\", \"index_options\": \"offsets\"},\n")
            .append("    \"aboutMe\" : {\"type\" : \"text\", \"index_options\": \"offsets\"},\n")
            .append("    \"lastUpdatedDate\" : {\"type\" : \"date\", \"format\": \"epoch_millis\"}\n")
            .append("  }\n")
            .append("}");

    return mapping.toString();
  }

  /**
   * The string the profile index holds for one stored value of a profile
   * property: a dropdown option id becomes the option's value joined with
   * {@code -} to each of its translated labels, any other value (free text
   * typed before the property became a dropdown included) is kept as stored;
   * the accents are removed for a value of a multi-valued property and for
   * {@code position}, as the document does. A query matching a stored value
   * against the index computes the indexed string through this method, with
   * the setting's multi-valued flag. Known limit: values stored as a list
   * before the property was made single-valued stay indexed as a joined,
   * accent-free string until the user saves the property again.
   *
   * @param setting the property's setting, may be null
   * @param storedValue the value as the profile stores it
   * @param multiValued whether the value is one of a multi-valued property's
   * @param translationService reads the option labels
   * @return the indexed string
   */
  public static String toIndexedValue(ProfilePropertySetting setting,
                                      String storedValue,
                                      boolean multiValued,
                                      TranslationService translationService) {
    String value = parseValue(setting, storedValue, translationService);
    boolean position = setting != null && POSITION_PROPERTY.equals(setting.getPropertyName());
    return multiValued || position ? removeAccents(value) : value;
  }

  private Document getDocument(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }

    long ts = System.currentTimeMillis();
    LOG.debug("get profile document for identity id={}", id);

    Identity identity = identityManager.getIdentity(id);
    if (identity == null || !identity.isEnable()) {
      // only enabled users are listed: the bulk processor skips a null
      // document, so no per-user reindex puts a disabled user back
      LOG.debug("no profile document for the disabled identity id={}", id);
      return null;
    }
    Profile profile = identity.getProfile();
    List<Long> hiddenPropertyIds = profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(id));
    boolean isExternal = profile.getProperty(Profile.EXTERNAL) != null && (profile.getProperty(Profile.EXTERNAL)).equals("true");
    Map<String, String> fields = new HashMap<>();
    fields.put("name", profile.getFullName());
    fields.put("firstName", (String) profile.getProperty(Profile.FIRST_NAME));
    fields.put("lastName", (String) profile.getProperty(Profile.LAST_NAME));
    fields.put("aboutMe", removeAccents(profile.getAboutMe()));
    fields.put("skills", removeAccents((String) profile.getProperty(Profile.EXPERIENCES_SKILLS)));
    fields.put("avatarUrl", profile.getAvatarUrl());
    fields.put("userName", identity.getRemoteId());
    // the name search reads the email: a hidden email stays indexed, which is
    // why it is never a directory attribute
    fields.put("email", profile.getEmail());
    fields.put("external", String.valueOf(isExternal));
    if (profile.getProperty(Profile.LAST_LOGIN_TIME) != null) {
      fields.put("lastLoginTime", profile.getProperty(Profile.LAST_LOGIN_TIME).toString());
    }
    if (profile.getProperty(Profile.ENROLLMENT_DATE) != null) {
      fields.put("enrollmentDate", profile.getProperty(Profile.ENROLLMENT_DATE).toString());
    }
    for (ProfilePropertySetting profilePropertySetting : profilePropertyService.getPropertySettings()) {
      String propertyName = profilePropertySetting.getPropertyName();
      // position is the one fixed field a filter can select: it follows the
      // visibility and hidden rule of the settings
      boolean position = POSITION_PROPERTY.equals(propertyName);
      if (!profilePropertySetting.isVisible()
          || (!position && fields.containsKey(propertyName))
          || hiddenPropertyIds.contains(profilePropertySetting.getId())) {
        // a non-visible setting and an owner-hidden value are absent from the
        // document, so that no query matches them, whoever the viewer
        continue;
      }
      Object propertyValue = position ? profile.getPosition() : profile.getProperty(propertyName);
      if (propertyValue instanceof String value && StringUtils.isNotEmpty(value)) {
        // the setting decides the rule, as for the query: a property made
        // multi-valued keeps the plain values stored before
        fields.put(getIndexedFieldName(propertyName),
                   toIndexedValue(profilePropertySetting, value, profilePropertySetting.isMultiValued()));
      } else if (propertyValue instanceof List) {
        List<Map<String, String>> multiValues = (List<Map<String, String>>) propertyValue;
        if (CollectionUtils.isNotEmpty(multiValues)) {
          String value = multiValues.stream()
                                    .filter(property -> property.get("value") != null)
                                    .map(property -> toIndexedValue(profilePropertySetting, property.get("value"), true))
                                    .collect(Collectors.joining(",", "", ""));
          if (StringUtils.isNotEmpty(value)) {
            fields.put(getIndexedFieldName(propertyName), value);
          }
        }
      }
    }

    // confirmed connections
    String connectionsStr = buildConnectionString(identity, Relationship.Type.CONFIRMED);
    if (!connectionsStr.isEmpty()) {
      fields.put("connections", "@@@[" + connectionsStr + "]@@@");
    }

    Document document = new ProfileIndexDocument(id, null, new Date(), (Set<String>) null, fields);
    document.setPermissions(getPermissions(identity));
    LOG.info("profile document generated for identity id={} remote_id={} duration_ms={}",
            id,
            identity.getRemoteId(),
            System.currentTimeMillis() - ts);

    return document;
  }

  private String toIndexedValue(ProfilePropertySetting setting, String storedValue, boolean multiValued) {
    return toIndexedValue(setting, storedValue, multiValued, translationService);
  }

  private static String removeAccents(String string) {
    if (StringUtils.isNotBlank(string)) {
      string = Normalizer.normalize(string, Normalizer.Form.NFD);
      string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
    return string;
  }

  private static String parseValue(ProfilePropertySetting profilePropertySetting,
                                   String value,
                                   TranslationService translationService) {
    if (profilePropertySetting == null || !profilePropertySetting.isDropdownList() || StringUtils.isBlank(value)) {
      return value;
    }
    ProfilePropertyOption option = getDropdownOption(profilePropertySetting, value);
    if (option == null) {
      // free text typed before the property became a dropdown: turning the
      // switch on rewrites no stored value, so it is indexed as typed
      return value;
    }
    String optionValue = option.getValue();
    try {
      TranslationField translationField = translationService.getTranslationField(PROFILE_PROPERTY_OBJECT_TYPE,
                                                                                 option.getId(),
                                                                                 PROFILE_PROPERTY_FIELD_NAME);
      if (translationField != null && !translationField.getLabels().isEmpty()) {
        String translations = String.join("-", translationField.getLabels().values());
        return String.join("-", optionValue, translations);
      }
    } catch (Exception e) {
      LOG.error("Error parsing profile property value translations", e);
    }
    return optionValue;
  }

  private static ProfilePropertyOption getDropdownOption(ProfilePropertySetting profilePropertySetting, String value) {
    if (!StringUtils.isNumeric(value) || CollectionUtils.isEmpty(profilePropertySetting.getPropertyOptions())) {
      return null;
    }
    long optionId;
    try {
      optionId = Long.parseLong(value);
    } catch (NumberFormatException e) {
      return null;
    }
    return profilePropertySetting.getPropertyOptions()
                                 .stream()
                                 .filter(option -> option.getId() != null && option.getId() == optionId)
                                 .findFirst()
                                 .orElse(null);
  }

  private Set<String> getPermissions(Identity ownerIdentity) {
    return new HashSet<>(userPermissionService.getPermissionTokens(ownerIdentity.getRemoteId()));
  }
}
