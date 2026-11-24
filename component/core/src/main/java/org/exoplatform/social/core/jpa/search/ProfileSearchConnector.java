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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.meeds.social.identity.permission.service.UserPermissionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.storage.impl.StorageUtils;

import io.meeds.social.people.model.PeopleDirectoryFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class ProfileSearchConnector {

  /**
   * Number of value combinations aggregated for a multi-valued property, whose
   * index field holds one joined string per user.
   */
  public static final int     MULTI_VALUED_AGGREGATION_SIZE = 500;

  private static final Log LOG = ExoLogger.getLogger(ProfileSearchConnector.class);

  private static final String ES_RESERVED_CHARS = "+-=&|><!(){}[]^\"~*?:\\/";

  /** Characters the Lucene regexp grammar reads as operators. */
  private static final String REGEXP_RESERVED_CHARS = ".?+*|{}[]()\"\\#@&<>~";

  /** The former placeholder of an owner-hidden value, never suggested. */
  private static final String HIDDEN_VALUE = "hidden";

  private static final JsonMapper JSON_MAPPER = new JsonMapper();

  private static final String MATCH_NONE_CLAUSE = "{\"match_none\": {}}";

  private static final String PERMISSIONS_FIELD = "permissions";

  private final ElasticSearchingClient client;
  private final ProfilePropertyService profilePropertyService;
  private final UserPermissionService  userPermissionService;

  private String index;
  
  public ProfileSearchConnector(InitParams initParams,
                                ElasticSearchingClient client,
                                ProfilePropertyService profilePropertyService,
                                UserPermissionService userPermissionService) {
    this.profilePropertyService = profilePropertyService;
    this.userPermissionService = userPermissionService;
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");
    this.client = client;
  }

  public List<String> search(Identity identity,
                             ProfileFilter filter,
                             Type type,
                             long offset,
                             long limit) {
    if (identity == null && filter.getViewerIdentity() != null) {
      identity = filter.getViewerIdentity();
    }
    String esQuery = buildQueryStatement(identity, filter, type, offset, limit);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    return buildResult(jsonResponse);
  }

  /**
   * TODO it will be remove to use "_count" query
   * 
   * @param identity the Identity
   * @param filter the filter
   * @param type type
   * @return number of identities
   */
  public int count(Identity identity,
                   ProfileFilter filter,
                   Type type) {
    String esQuery = buildQueryStatement(identity, filter, type, 0, 1);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    return getCount(jsonResponse);
  }

  private int getCount(String jsonResponse) {
    
    LOG.debug("Search Query response from ES : {} ", jsonResponse);
    JSONParser parser = new JSONParser();

    Map<?, ?> json = null;
    try {
      json = (Map<?, ?>)parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) return 0;

    Object totalSize = ((JSONObject) jsonResult.get("total")).get("value");
    return totalSize == null ? 0 : Integer.parseInt(totalSize.toString());
  }
  
  private List<String> buildResult(String jsonResponse) {

    LOG.debug("Search Query response from ES : {} ", jsonResponse);
    JSONParser parser = new JSONParser();

    Map<?, ?> json = null;
    try {
      json = (Map<?, ?>)parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) {
      return Collections.emptyList();
    } else {
      JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
      List<String> results = new ArrayList<>();
      for(Object jsonHit : jsonHits) {
        String identityId = (String) ((JSONObject) jsonHit).get("_id");
        results.add(identityId);
      }
      return results;
    }
  }

  private String buildQueryStatement(Identity identity, ProfileFilter filter, Type type, long offset, long limit) {
    String expEs = buildExpression(filter);
    Map<String, String> profileSettings = filter.getProfileSettings();
    String expEsForAdvancedFilter = MapUtils.isNotEmpty(profileSettings) ? buildAdvancedFilterExpression(filter) : "";
    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{\n");
    esQuery.append("   \"from\" : ").append(offset).append(", \"size\" : ").append(limit).append(",\n");
    Sorting sorting = filter.getSorting();
    esQuery.append("   \"sort\": {");
    String sortField;
    String sortOrder = sorting != null && sorting.orderBy != null ? sorting.orderBy.name() : "asc";

    if (sorting != null && sorting.sortBy != null) {
      switch (sorting.sortBy) {
        case DATE:
          sortField = "\"lastUpdatedDate\"";
          break;
        case FIRSTNAME:
          sortField = "\"firstName.raw\"";
          break;
        case LASTNAME:
          sortField = "\"lastName.raw\"";
          break;
        default:
          sortField = "\"name.raw\"";
          break;
      }
    } else {
      sortField = "\"name.raw\"";
    }
    esQuery.append(sortField)
      .append(": {\"order\": \"")
      .append(sortOrder)
      .append("\"}}\n");
    StringBuilder esSubQuery = new StringBuilder();
    esSubQuery.append("\"query\" : {\n");
    esSubQuery.append("      \"constant_score\" : {\n");
    esSubQuery.append("        \"filter\" : {\n");
    esSubQuery.append("          \"bool\" :{\n");
    boolean subQueryEmpty = true;
    boolean appendCommar = false;
    List<String> mustClauses = new ArrayList<>();
    List<String> mustNotClauses = new ArrayList<>();
    List<String> filterClauses = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(filter.getGroupIds())) {
      String membershipPermissionsQuery = buildMembershipPermissionsExpression(filter);
      mustClauses.add(membershipPermissionsQuery);
      subQueryEmpty = false;
    }
    if (filter.getUserType() != null && !filter.getUserType().isEmpty()) {
      List<String> userTypeShoulds = new ArrayList<>();

      if (filter.getUserType().equals("internal")) {
        userTypeShoulds.add("                  {\n"
          + "                    \"term\": {\n"
          + "                      \"external\": false\n"
          + "                    }\n"
          + "                  }");
        userTypeShoulds.add("                  {\n"
          + "                    \"bool\": {\n"
          + "                      \"must_not\": {\n"
          + "                        \"exists\": {\n"
          + "                          \"field\": \"external\"\n"
          + "                        }\n"
          + "                      }\n"
          + "                    }\n"
          + "                  }");
      } else if (filter.getUserType().equals("external")) {
        userTypeShoulds.add("                  {\n"
          + "                    \"term\": {\n"
          + "                      \"external\": true\n"
          + "                    }\n"
          + "                  }");
      }

      if (CollectionUtils.isNotEmpty(userTypeShoulds)) {
        StringBuilder userTypeBlockBuilder = new StringBuilder();
        userTypeBlockBuilder.append("      {\n")
          .append("        \"bool\": {\n")
          .append("          \"should\": [\n")
          .append(String.join(",\n", userTypeShoulds))
          .append("          ],\n")
          .append("          \"minimum_should_match\" : 1\n")
          .append("        }\n")
          .append("      }");
        mustClauses.add(userTypeBlockBuilder.toString());
        subQueryEmpty = false;
      }
    }
    if (filter.isConnected() != null) {
      StringBuilder existsClauseBuilder = new StringBuilder();
      existsClauseBuilder.append("                  {\n")
        .append("                    \"bool\": {\n");

      if (filter.isConnected()) {
        existsClauseBuilder.append("                      \"must\": {\n");
      } else {
        existsClauseBuilder.append("                      \"must_not\": {\n");
      }
      existsClauseBuilder.append("                        \"exists\": {\n")
        .append("                          \"field\": \"lastLoginTime\"\n")
        .append("                        }\n")
        .append("                      }\n")
        .append("                    }\n")
        .append("                  }");

      StringBuilder connectedBlockBuilder = new StringBuilder();
      connectedBlockBuilder.append("      {\n")
        .append("        \"bool\": {\n")
        .append("          \"should\": [\n")
        .append(existsClauseBuilder.toString())
        .append("          ],\n")
        .append("          \"minimum_should_match\" : 1\n")
        .append("        }\n")
        .append("      }");
      mustClauses.add(connectedBlockBuilder.toString());
      subQueryEmpty = false;
    }
    if(filter.getEnrollmentStatus() != null && !filter.getEnrollmentStatus().isEmpty()) {
      List<String> enrollmentShoulds = new ArrayList<>();

      switch (filter.getEnrollmentStatus()) {
        case "enrolled": {
          enrollmentShoulds.add("                  {\n"
            + "                    \"bool\": {\n"
            + "                      \"must\": {\n"
            + "                        \"exists\": {\n"
            + "                          \"field\": \"enrollmentDate\"\n"
            + "                        }\n"
            + "                      }\n"
            + "                    }\n"
            + "                  }");
          break;
        }

        case "notEnrolled": {
          enrollmentShoulds.add("                  {\n"
            + "                    \"bool\": {\n"
            + "                      \"must_not\": [{\n"
            + "                        \"exists\": {\n"
            + "                          \"field\": \"enrollmentDate\"\n"
            + "                          }\n"
            + "                        },\n"
            + "                      {\n"
            + "                        \"exists\": {\n"
            + "                          \"field\": \"lastLoginTime\"\n"
            + "                        }\n"
            + "                      }]\n"
            + "                    }\n"
            + "                  }");
          break;
        }

        case "noEnrollmentPossible": {
          enrollmentShoulds.add("                  {\n"
            + "                    \"bool\": {\n"
            + "                      \"must_not\": {\n"
            + "                        \"exists\": {\n"
            + "                          \"field\": \"enrollmentDate\"\n"
            + "                          }\n"
            + "                        },\n"
            + "                      \"must\": {\n"
            + "                        \"exists\": {\n"
            + "                          \"field\": \"lastLoginTime\"\n"
            + "                        }\n"
            + "                      }\n"
            + "                    }\n"
            + "                  }");
          break;
        }

        default:
          break;
      }

      if (CollectionUtils.isNotEmpty(enrollmentShoulds)) {
        StringBuilder enrollmentBlockBuilder = new StringBuilder();
        enrollmentBlockBuilder.append("      {\n")
          .append("        \"bool\": {\n")
          .append("          \"should\": [\n")
          .append(String.join(",\n", enrollmentShoulds))
          .append("          ],\n")
          .append("          \"minimum_should_match\" : 1\n")
          .append("        }\n")
          .append("      }");
        mustClauses.add(enrollmentBlockBuilder.toString());
        subQueryEmpty = false;
      }
    }
    if (filter.getRemoteIds() != null && !filter.getRemoteIds().isEmpty()) {
      StringBuilder remoteIds = new StringBuilder();
      for (String remoteId : filter.getRemoteIds()) {
        if (remoteIds.length() > 0) {
          remoteIds.append(",");
        }
        remoteIds.append("\"").append(JSONValue.escape(remoteId)).append("\"");
      }
      StringBuilder remoteIdClauseBuilder = new StringBuilder();
      remoteIdClauseBuilder.append("      {\n")
        .append("        \"terms\" :{\n")
        .append("          \"userName\" : [").append(remoteIds.toString()).append("]\n")
        .append("        } \n")
        .append("      }\n");

      mustClauses.add(remoteIdClauseBuilder.toString());
      subQueryEmpty = false;
    }
    if (identity != null && type != null) {
      StringBuilder identityClauseBuilder = new StringBuilder();
      identityClauseBuilder.append("      {\n")
        .append("        \"query_string\" : {\n")
        .append("          \"query\" : \"").append(identity.getId()).append("\",\n")
        .append("          \"fields\" : [\"").append(buildTypeEx(type)).append("\"]\n")
        .append("        }\n")
        .append("      }\n");

      mustClauses.add(identityClauseBuilder.toString());
      subQueryEmpty = false;
    } else if (filter.getExcludedIdentityList() != null && filter.getExcludedIdentityList().size() > 0) {
      StringBuilder excludedClauseBuilder = new StringBuilder();
      excludedClauseBuilder.append("      {\n")
        .append("          \"ids\" : {\n")
        .append("             \"values\" : [").append(buildExcludedIdentities(filter)).append("]\n")
        .append("          }\n")
        .append("        }\n");

      mustNotClauses.add(excludedClauseBuilder.toString());
      subQueryEmpty = false;
    }
    if (!expEs.isEmpty() || !expEsForAdvancedFilter.isEmpty()) {
      if(!expEs.isEmpty()) {
        StringBuilder qsClause = new StringBuilder();
        qsClause.append("      {");
        qsClause.append("          \"query_string\": {\n");
        if (filter.getName().startsWith("\"") && filter.getName().endsWith("\"")) {
          qsClause.append("            \"query\": \"").append(expEs).append("\",\n")
            .append("            \"default_operator\": \"").append("AND").append("\"\n");
        } else {
          qsClause.append("            \"query\": \"").append(expEs).append("\"\n");
        }
        qsClause.append("          }\n");
        qsClause.append("      }\n");
        filterClauses.add(qsClause.toString());
      }

      if(!expEsForAdvancedFilter.isEmpty()) {
        filterClauses.add(expEsForAdvancedFilter);
      }
    }
    filterClauses.addAll(buildDirectoryClauses(filter.getDirectoryFilter()));
    if (filter.getGuestIdentityId() != null) {
      filterClauses.add(buildGuestRestrictionClause(filter.getViewerSpaceGroupIds(), filter.getGuestIdentityId()));
    }

    if (CollectionUtils.isNotEmpty(mustClauses)) {
      if(appendCommar) {
        esSubQuery.append("      ,\n");
      }
      esSubQuery.append("      \"must\": [\n");
      esSubQuery.append(String.join(",\n", mustClauses));
      esSubQuery.append("      ]\n");
      appendCommar = true;
    }

    if (CollectionUtils.isNotEmpty(mustNotClauses)) {
      if(appendCommar) {
        esSubQuery.append("      ,\n");
      }
      esSubQuery.append("      \"must_not\": [\n");
      esSubQuery.append(String.join(",\n", mustNotClauses));
      esSubQuery.append("      ]\n");
      appendCommar = true;
    }

    if (CollectionUtils.isNotEmpty(filterClauses)) {
      if(appendCommar) {
        esSubQuery.append("      ,\n");
      }
      esSubQuery.append("      \"filter\": [\n");
      esSubQuery.append(String.join(",\n", filterClauses));
      esSubQuery.append("      ]\n");
      appendCommar = true;
    }
    if (appendCommar) {
      subQueryEmpty = false;
    }
    esSubQuery.append("     } \n");
    esSubQuery.append("   } \n");
    esSubQuery.append("  }\n");
    esSubQuery.append(" }\n");
    if(!subQueryEmpty) {
      esQuery.append(",\n");
      esQuery.append(esSubQuery);
    }

    esQuery.append(",\"_source\": false\n");
    esQuery.append(",\"fields\": [\"_id\"]\n");
    esQuery.append("}\n");
    return esQuery.toString();
  }

  /**
   * 
   * @param filter
   * @return
   */
  private String buildExcludedIdentities(ProfileFilter filter) {
    StringBuilder typeExp = new StringBuilder();
    if (filter.getExcludedIdentityList() != null && filter.getExcludedIdentityList().size() > 0) {
      
      Iterator<Identity> iter = filter.getExcludedIdentityList().iterator();
      Identity first = iter.next();
      typeExp.append("\"").append(first.getId()).append("\"");
      
      if (!iter.hasNext()) {
        return typeExp.toString();
      }
      Identity next;
      while (iter.hasNext()) {
        next = iter.next();
        typeExp.append(",\"").append(next.getId()).append("\"");
      }
    }
    return typeExp.toString();
  }
  
  /**
   * 
   * @param type
   * @return
   */
  private String buildTypeEx(Type type) {
    String result;
    switch(type) {
      case CONFIRMED:
        result = "connections";
        break;
      case INCOMING:
        // Search for identity of current user viewer
        // in the outgoings relationships field of other identities
        result = "outgoings";
        break;
      case OUTGOING:
        // Search for identity of current user viewer
        // in the incomings relationships field of other identities
        result = "incomings";
        break;
      default:
        throw new IllegalArgumentException("Type ["+type+"] not supported");
    }
    return result;
  }

  private String buildExpression(ProfileFilter filter) {
    StringBuilder esExp = new StringBuilder();
    //
    String inputName = StringUtils.isBlank(filter.getName()) ? null : filter.getName().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
    if (StringUtils.isNotBlank(inputName)) {
     //
      String newInputName = inputName.trim();
      String[] keys;
      if (newInputName.startsWith("\"") && inputName.endsWith("\"")) {
        newInputName = inputName.replace("\"", "");
      }
      // same whitespace split and escaping as the profile property values
      keys = StringUtils.split(newInputName);
      if (keys.length > 1) {
        StringBuilder nameEsExp = new StringBuilder();
        // We will not search on userName because it doesn't contain a space character
        for (int i = 0; i < keys.length; i++) {
          if (i != 0) {
            nameEsExp.append(") AND (");
          }
          String searchedWord = StorageUtils.ASTERISK_STR + escapeESReservedChars(removeAccents(keys[i]))
              + StorageUtils.ASTERISK_STR;
          nameEsExp.append(" name.whitespace:").append(searchedWord);
          if (filter.isSearchEmail()) {
            nameEsExp.append(" OR email:").append(searchedWord);
          }
          if (filter.isSearchUserName()) {
            nameEsExp.append(" OR userName:").append(searchedWord);
          }
        }
        esExp.append("(").append(nameEsExp).append(")");
      } else if (keys.length == 1) {
        String searchedText = StorageUtils.ASTERISK_STR + escapeESReservedChars(removeAccents(keys[0]))
            + StorageUtils.ASTERISK_STR;
        esExp.append("name.whitespace:").append(searchedText);

        if (filter.isSearchEmail()) {
          esExp.append(" OR email:").append(searchedText);
        }
        if (filter.isSearchUserName()) {
          esExp.append(" OR userName:").append(searchedText);
        }
      } else {
        // a quoted name holding only whitespace: no word to search
        esExp.append("name.whitespace:").append(StorageUtils.ASTERISK_STR).append(StorageUtils.ASTERISK_STR);
      }
    }
    return esExp.toString();
  }

  private String buildAdvancedFilterExpression(ProfileFilter filter) {
    Map<String, String> settings = filter.getProfileSettings();
    if (MapUtils.isEmpty(settings)) {
      return StringUtils.EMPTY;
    }
    StringBuilder query = new StringBuilder();
    int index = 0;
    // looked up in the full list: a lookup by an unknown name caches the miss,
    // and the index holds the visible settings only
    List<ProfilePropertySetting> propertySettings = profilePropertyService.getPropertySettings();
    for(String key : settings.keySet()) {
      String settingName = key;
      ProfilePropertySetting property = propertySettings.stream()
                                                        .filter(setting -> StringUtils.equals(setting.getPropertyName(), settingName))
                                                        .findFirst()
                                                        .orElse(null);
      if (property != null && !property.isVisible()) {
        // the index holds no value for a non-visible setting: a criterion on
        // it matches nobody, never widens the list
        if (index > 0) {
          query.append(",");
        }
        query.append(MATCH_NONE_CLAUSE);
        index++;
      } else if (property != null) {
        // split on whitespace, as Character#isWhitespace and the index's
        // whitespace tokenizer define it, before escaping: query_string
        // separates terms on tabs and line breaks too, which would leave a
        // word with no field, searched on every field
        String[] splittedValues = StringUtils.split(settings.get(key));
        if (ArrayUtils.isNotEmpty(splittedValues)) {
          if (index > 0) {
            query.append(",");
          }
          StringBuilder expression = new StringBuilder();
          if (splittedValues.length > 1) {
            for (int i = 0; i < splittedValues.length; i++) {
              if (i != 0) {
                expression.append(" AND ");
              }
              String searchedWord = escapeESReservedChars(removeAccents(splittedValues[i]));
              String searchedText;
              if (filter.isWildcardSearch()) {
                searchedText = StorageUtils.ASTERISK_STR + searchedWord + StorageUtils.ASTERISK_STR;
              } else {
                searchedText = searchedWord;
              }
              key = ProfileIndexingServiceConnector.getIndexedFieldName(key);
              expression.append(" ").append(escapeESFieldName(key)).append(isEmailProfileProperty(key) ? ":" : ".whitespace:").append(searchedText);
            }
            query.append("""
                        {
                          "query_string": {
                            "query": "%s"
                          }
                       }
                      """.formatted(expression.toString()));
          } else {
            String searchedWord = escapeESReservedChars(removeAccents(splittedValues[0]));
            String searchedTex = filter.isWildcardSearch()
                                                          ? StorageUtils.ASTERISK_STR + searchedWord
                                                              + StorageUtils.ASTERISK_STR
                                                          : searchedWord;
            String propertyName = property.getPropertyName();
            propertyName = ProfileIndexingServiceConnector.getIndexedFieldName(propertyName);
            String filedName = isEmailProfileProperty(propertyName) ? propertyName : "%s.whitespace".formatted(propertyName);
            query.append("""
                  {
                    "query_string": {
                      "query": "%s:%s"
                    }
                 }
                """.formatted(escapeESFieldName(filedName), searchedTex));
          }
          index++;
        }
      }
    }

    return query.toString();
  }

  /**
   * The distinct values the profile index holds for a property, at most limit,
   * most frequent first. A multi-valued property's field holds one joined
   * string per user: up to {@link #MULTI_VALUED_AGGREGATION_SIZE} combinations
   * are aggregated, then split, so a value held only in rarer combinations is
   * reached by narrowing with a query. Known limit: the joined string does not
   * tell a separator from a comma inside a value, so a multi-valued value
   * holding a comma ({@code Paris, France}) is listed as its pieces
   * ({@code Paris}, {@code France}), each stripped of its spaces, which the
   * directory clause skips. The values are the index's strings: a
   * multi-valued property's and {@code position}'s come without their
   * accents.
   *
   * @param propertyName the property name, a visible setting's
   * @param multiValued whether the property is multi-valued
   * @param query a text the values must contain, ignoring case, may be blank
   * @param limit the maximum number of values
   * @return the values, as the index holds them
   */
  public List<String> getAttributeValues(String propertyName, boolean multiValued, String query, int limit) {
    String esQuery = buildAttributeValuesQuery(propertyName, multiValued, query, limit);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    Set<String> values = new LinkedHashSet<>();
    try {
      JsonNode buckets = JSON_MAPPER.readTree(jsonResponse).path("aggregations").path("values").path("buckets");
      for (JsonNode bucket : buckets) {
        String key = bucket.path("key").asString();
        if (multiValued) {
          for (String value : StringUtils.split(key, ',')) {
            // only the spaces the directory clause skips around a separator
            String trimmed = StringUtils.strip(value, " ");
            if (StringUtils.isNotEmpty(trimmed) && (StringUtils.isEmpty(query) || StringUtils.containsIgnoreCase(trimmed, query))) {
              values.add(trimmed);
            }
          }
        } else {
          values.add(key);
        }
      }
    } catch (Exception e) { // NOSONAR a malformed engine answer yields no value
      LOG.warn("Error while parsing the attribute values of {}", propertyName, e);
    }
    return values.stream().limit(limit).toList();
  }

  String buildAttributeValuesQuery(String propertyName, boolean multiValued, String query, int limit) {
    ObjectNode terms = JSON_MAPPER.createObjectNode();
    terms.put("field", ProfileIndexingServiceConnector.getIndexedFieldName(propertyName) + ".raw");
    terms.put("size", multiValued ? MULTI_VALUED_AGGREGATION_SIZE : limit);
    terms.putArray("exclude").add(HIDDEN_VALUE);
    if (StringUtils.isNotEmpty(query)) {
      terms.put("include", ".*" + toCaseInsensitiveRegexp(query) + ".*");
    }
    terms.putObject("order").put("_count", "desc");
    ObjectNode root = JSON_MAPPER.createObjectNode();
    root.put("size", 0);
    root.putObject("aggs").putObject("values").set("terms", terms);
    return root.toString();
  }

  /**
   * The group clause (see {@link #buildDirectoryGroupsClause}), then one clause
   * per directory attribute, each matching a user holding one of
   * its values: a term on the indexed field for a single-valued property, a
   * regexp matching one value of the joined string for a multi-valued one,
   * spaces around it ignored as the value picker strips them. An attribute
   * that is not a visible, active, top-level setting matches nobody.
   * Known limit: a multi-valued value holding a comma is read as several
   * values, so {@code Paris} also matches a user holding {@code Paris, France}.
   */
  private List<String> buildDirectoryClauses(PeopleDirectoryFilter directoryFilter) {
    if (directoryFilter == null) {
      return Collections.emptyList();
    }
    List<String> clauses = new ArrayList<>();
    if (directoryFilter.getGroups() != null) {
      clauses.add(buildDirectoryGroupsClause(directoryFilter.getGroups()));
    }
    if (MapUtils.isEmpty(directoryFilter.getAttributes())) {
      return clauses;
    }
    List<ProfilePropertySetting> settings = profilePropertyService.getPropertySettings();
    for (Map.Entry<String, List<String>> attribute : directoryFilter.getAttributes().entrySet()) {
      ProfilePropertySetting setting = settings.stream()
                                               .filter(s -> StringUtils.equals(s.getPropertyName(), attribute.getKey()))
                                               .findFirst()
                                               .orElse(null);
      List<String> values = attribute.getValue() == null ? Collections.emptyList()
                                                         : attribute.getValue()
                                                                    .stream()
                                                                    .filter(StringUtils::isNotEmpty)
                                                                    .toList();
      if (setting == null || !setting.isVisible() || !setting.isActive() || setting.getParentId() != null
          || values.isEmpty()) {
        clauses.add(MATCH_NONE_CLAUSE);
        continue;
      }
      String field = ProfileIndexingServiceConnector.getIndexedFieldName(setting.getPropertyName()) + ".raw";
      ObjectNode bool = JSON_MAPPER.createObjectNode();
      ArrayNode should = bool.putObject("bool").putArray("should");
      for (String value : values) {
        if (setting.isMultiValued()) {
          ObjectNode regexp = should.addObject().putObject("regexp").putObject(field);
          regexp.put("value", "(.*,)? *" + escapeRegexp(value) + " *(,.*)?");
          regexp.put("flags", "NONE");
        } else {
          should.addObject().putObject("term").put(field, value);
        }
      }
      ((ObjectNode) bool.get("bool")).put("minimum_should_match", 1);
      clauses.add(bool.toString());
    }
    return clauses;
  }

  /**
   * The clause matching a user who is a member of one of the groups, or of one
   * of their sub-groups, through any membership type, directly or through a
   * nested-group link: a term on each membership token of a group, and a
   * prefix on the tokens of its sub-groups ({@code member:/organization/hr/}
   * matches {@code member:/organization/hr/payroll}), so neither the group
   * tree nor a member list is loaded. No group matches nobody.
   */
  String buildDirectoryGroupsClause(List<String> groupIds) {
    List<String> groups = groupIds.stream().filter(StringUtils::isNotBlank).distinct().toList();
    if (groups.isEmpty()) {
      return MATCH_NONE_CLAUSE;
    }
    Set<String> membershipTypes = new LinkedHashSet<>(userPermissionService.getMembershipTypes());
    membershipTypes.add(UserPermissionService.ANY_MEMBERSHIP_TYPE);
    ObjectNode root = JSON_MAPPER.createObjectNode();
    ObjectNode bool = root.putObject("bool");
    ArrayNode should = bool.putArray("should");
    ArrayNode tokens = should.addObject().putObject("terms").putArray(PERMISSIONS_FIELD);
    for (String groupId : groups) {
      for (String membershipType : membershipTypes) {
        String token = membershipType + ":" + groupId;
        tokens.add(token);
        tokens.add(UserPermissionService.INHERITED_TOKEN_PREFIX + token);
        should.addObject().putObject("prefix").put(PERMISSIONS_FIELD, token + "/");
        should.addObject().putObject("prefix").put(PERMISSIONS_FIELD, UserPermissionService.INHERITED_TOKEN_PREFIX + token + "/");
      }
    }
    bool.put("minimum_should_match", 1);
    return root.toString();
  }

  /**
   * The guest restriction, the query form of the {@code VIEW} rule of
   * {@code UserAclPlugin} for a guest viewer: a user who is a member of one of
   * the guest's space groups, through any membership type, directly or through
   * a nested-group link; a confirmed connection of the guest; or the guest
   * themself. One {@code bool.should}, ANDed with the other filters and never
   * merged into the directory's group filter, whose terms are an OR. With no
   * space group, it matches the guest's connections and the guest only.
   */
  String buildGuestRestrictionClause(List<String> spaceGroupIds, long guestIdentityId) {
    ObjectNode root = JSON_MAPPER.createObjectNode();
    ObjectNode bool = root.putObject("bool");
    ArrayNode should = bool.putArray("should");
    List<String> groups = spaceGroupIds == null ? Collections.emptyList()
                                                : spaceGroupIds.stream().filter(StringUtils::isNotBlank).distinct().toList();
    if (!groups.isEmpty()) {
      Set<String> membershipTypes = new LinkedHashSet<>(userPermissionService.getMembershipTypes());
      membershipTypes.add(UserPermissionService.ANY_MEMBERSHIP_TYPE);
      ArrayNode tokens = should.addObject().putObject("terms").putArray(PERMISSIONS_FIELD);
      for (String groupId : groups) {
        for (String membershipType : membershipTypes) {
          String token = membershipType + ":" + groupId;
          tokens.add(token);
          tokens.add(UserPermissionService.INHERITED_TOKEN_PREFIX + token);
        }
      }
    }
    should.addObject().putObject("term").put("connections", guestIdentityId);
    should.addObject().putObject("ids").putArray("values").add(String.valueOf(guestIdentityId));
    bool.put("minimum_should_match", 1);
    return root.toString();
  }

  /**
   * A Lucene regexp matching a text literally, ignoring case: each cased
   * letter becomes the class of its lower and upper forms ({@code hr} gives
   * {@code [hH][rR]}), every other character is escaped as by
   * {@link #escapeRegexp}.
   */
  static String toCaseInsensitiveRegexp(String value) {
    StringBuilder regexp = new StringBuilder(value.length() * 4);
    for (char c : value.toCharArray()) {
      char lower = Character.toLowerCase(c);
      char upper = Character.toUpperCase(c);
      if (lower != upper) {
        regexp.append('[').append(lower).append(upper).append(']');
      } else {
        regexp.append(escapeRegexp(String.valueOf(c)));
      }
    }
    return regexp.toString();
  }

  /**
   * Escapes every character the Lucene regexp grammar reads as an operator,
   * so that the value is matched literally.
   */
  static String escapeRegexp(String value) {
    StringBuilder escaped = new StringBuilder(value.length());
    for (char c : value.toCharArray()) {
      if (REGEXP_RESERVED_CHARS.indexOf(c) >= 0) {
        escaped.append('\\');
      }
      escaped.append(c);
    }
    return escaped.toString();
  }

  private static String removeAccents(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return string;
  }

  /**
   * Escapes a value for a query_string query written inside a JSON string, at
   * two levels: each query_string reserved character is escaped once, then the
   * result is escaped for JSON, so that a backslash, a slash or a double quote
   * in the value reaches Elasticsearch as a literal character.
   *
   * @param string value to search for
   * @return the value to write between the quotes of the query JSON string
   */
  public static String escapeESReservedChars(String string) {
    return JSONValue.escape(escapeQueryString(string, false));
  }

  /**
   * Escapes a property's field name for a query_string query written inside a
   * JSON string: its reserved characters and its whitespace, which query_string
   * reads as a term separator, are escaped once, then the result is escaped for
   * JSON.
   */
  private static String escapeESFieldName(String fieldName) {
    return JSONValue.escape(escapeQueryString(fieldName, true));
  }

  private static String escapeQueryString(String string, boolean escapeWhitespace) {
    StringBuilder escaped = new StringBuilder(string.length());
    for (char c : string.toCharArray()) {
      if (ES_RESERVED_CHARS.indexOf(c) >= 0 || (escapeWhitespace && Character.isWhitespace(c))) {
        escaped.append('\\');
      }
      escaped.append(c);
    }
    return escaped.toString();
  }

  private String buildMembershipPermissionsExpression(ProfileFilter filter) {
    String membershipType = filter.getMembershipType();
    boolean includeInherited = filter.isIncludeInheritedMemberships();
    StringBuilder permissions = new StringBuilder();
    for (String groupId : filter.getGroupIds()) {
      if (StringUtils.isNotBlank(membershipType)) {
        appendTypedMembershipToken(permissions, membershipType, groupId, includeInherited);
        appendTypedMembershipToken(permissions, UserPermissionService.ANY_MEMBERSHIP_TYPE, groupId, includeInherited);
      } else {
        userPermissionService.getMembershipTypes().forEach(m -> {
          appendTypedMembershipToken(permissions, m, groupId, includeInherited);
        });
      }
    }
    StringBuilder query = new StringBuilder();
    query.append("      {\n");
    query.append("        \"terms\" :{\n");
    query.append("          \"permissions\" : [").append(permissions).append("]\n");
    query.append("        } \n");
    query.append("      }\n");
    return query.toString();
  }

  private void appendTypedMembershipToken(StringBuilder permissions, String membershipType, String groupId, boolean includeInherited) {
    String token = membershipType + ":" + groupId;
    appendToken(permissions, token, false);
    if (includeInherited) {
      appendToken(permissions, token, true);
    }
  }

  private void appendToken(StringBuilder permissions, String token, boolean inherited) {
    if (permissions.length() > 0) {
      permissions.append(",");
    }
    String prefix = inherited ? UserPermissionService.INHERITED_TOKEN_PREFIX : "";
    permissions.append("\"").append(JSONValue.escape(prefix + token)).append("\"");
  }
  private boolean isEmailProfileProperty(String propertyName) {
    return propertyName.equals("email");
  }
}
