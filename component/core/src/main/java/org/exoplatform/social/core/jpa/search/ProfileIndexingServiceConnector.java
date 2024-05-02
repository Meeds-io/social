/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.search;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 29, 2015
 */
public class ProfileIndexingServiceConnector extends ElasticIndexingServiceConnector {

  public static final String           TYPE         = "profile";

  private static final Log             LOG          = ExoLogger.getLogger(ProfileIndexingServiceConnector.class);

  private final IdentityManager        identityManager;

  private final ConnectionDAO          connectionDAO;

  private final IdentityDAO            identityDAO;

  private final ProfilePropertyService profilePropertyService;

  private static final String          HIDDEN_VALUE = "hidden";

  public ProfileIndexingServiceConnector(InitParams initParams,
                                         IdentityManager identityManager,
                                         IdentityDAO identityDAO,
                                         ConnectionDAO connectionDAO,
                                         ProfilePropertyService profilePropertyService) {
    super(initParams);
    this.identityManager = identityManager;
    this.identityDAO = identityDAO;
    this.connectionDAO = connectionDAO;
    this.profilePropertyService = profilePropertyService;
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
      if(!propertySetting.isMultiValued() && propertySetting.getParentId() == null && !profilePropertyService.hasChildProperties(propertySetting) && !"email".equals(propertySetting.getPropertyName())) {
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
            .append("    \"avatarUrl\" : {\"type\" : \"text\", \"index\": false},\n")
            .append("    \"skills\" : {\"type\" : \"text\", \"index_options\": \"offsets\"},\n")
            .append("    \"aboutMe\" : {\"type\" : \"text\", \"index_options\": \"offsets\"},\n")
            .append("    \"lastUpdatedDate\" : {\"type\" : \"date\", \"format\": \"epoch_millis\"}\n")
            .append("  }\n")
            .append("}");

    return mapping.toString();
  }

  private Document getDocument(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }

    long ts = System.currentTimeMillis();
    LOG.debug("get profile document for identity id={}", id);

    Identity identity = identityManager.getIdentity(id);
    Profile profile = identity.getProfile();
    boolean isExternal = profile.getProperty(Profile.EXTERNAL) != null && (profile.getProperty(Profile.EXTERNAL)).equals("true");
    Map<String, String> fields = new HashMap<>();
    fields.put("name", profile.getFullName());
    fields.put("firstName", (String) profile.getProperty(Profile.FIRST_NAME));
    fields.put("lastName", (String) profile.getProperty(Profile.LAST_NAME));
    fields.put("position", removeAccents(profile.getPosition()));
    fields.put("aboutMe", removeAccents(profile.getAboutMe()));
    fields.put("skills", removeAccents((String) profile.getProperty(Profile.EXPERIENCES_SKILLS)));
    fields.put("avatarUrl", profile.getAvatarUrl());
    fields.put("userName", identity.getRemoteId());
    fields.put("email", profile.getEmail());
    fields.put("external", String.valueOf(isExternal));
    if (profile.getProperty(Profile.LAST_LOGIN_TIME) != null) {
      fields.put("lastLoginTime", profile.getProperty(Profile.LAST_LOGIN_TIME).toString());
    }
    if (profile.getProperty(Profile.ENROLLMENT_DATE) != null) {
      fields.put("enrollmentDate", profile.getProperty(Profile.ENROLLMENT_DATE).toString());
    }
    Date createdDate = new Date(profile.getCreatedTime());

    for (String profilePropertySettingName : profilePropertyService.getPropertySettingNames()) {
      if (!fields.containsKey(profilePropertySettingName)) {
        if (profile.getProperty(profilePropertySettingName) != null && profile.getProperty(profilePropertySettingName) instanceof String value) {
          if (StringUtils.isNotEmpty(value)) {
            // Avoid having dots in field names in ES, otherwise properties with String values may be converted in Objects in some cases
            addPropertyToDocumentFields(fields, profilePropertySettingName, value, Long.parseLong(id));
          }
        } else {
          List<Map<String, String>> multiValues = (List<Map<String, String>>) profile.getProperty(profilePropertySettingName);
          if (CollectionUtils.isNotEmpty(multiValues)) {
            String value = multiValues.stream()
                .filter(property -> property.get("value") != null)
                .map(property -> property.get("value"))
                .collect(Collectors.joining(",", "", ""));
            if (StringUtils.isNotEmpty(value)) {
              addPropertyToDocumentFields(fields, profilePropertySettingName, removeAccents(value), Long.parseLong(id));
            }
          }
        }
      }
    }

    // confirmed connections
    String connectionsStr = buildConnectionString(identity, Relationship.Type.CONFIRMED);
    if (!connectionsStr.isEmpty()) {
      fields.put("connections", "@@@[" + connectionsStr + "]@@@");
    }

    Document document = new ProfileIndexDocument(id, null, createdDate, (Set<String>) null, fields);
    LOG.info("profile document generated for identity id={} remote_id={} duration_ms={}",
            id,
            identity.getRemoteId(),
            System.currentTimeMillis() - ts);

    return document;
  }

  private void addPropertyToDocumentFields(Map<String, String> fields, String propertyName, String value, long userIdentityId) {
    if(isPropertyHidden(propertyName, userIdentityId)) {
      fields.put(propertyName.replace(".", "_"), HIDDEN_VALUE);
    } else {
      fields.put(propertyName.replace(".", "_"), value);
    }
  }

  private Boolean isPropertyHidden(String propertyName, long userIdentityId) {
    ProfilePropertySetting propertySetting = profilePropertyService.getProfileSettingByName(propertyName);
    if (propertySetting != null) {
      return profilePropertyService.getHiddenProfilePropertyIds(userIdentityId).contains(propertySetting.getId());
    }
    return false;
  }

  private static String removeAccents(String string) {
    if (StringUtils.isNotBlank(string)) {
      string = Normalizer.normalize(string, Normalizer.Form.NFD);
      string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
    return string;
  }
}
