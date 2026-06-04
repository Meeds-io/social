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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

public class ProfileSearchConnector {
  private static final Log LOG = ExoLogger.getLogger(ProfileSearchConnector.class);
  private final ElasticSearchingClient client;
  private final ProfilePropertyService profilePropertyService;

  private String index;
  
  public ProfileSearchConnector(InitParams initParams,
                                ElasticSearchingClient client,
                                ProfilePropertyService profilePropertyService) {
    this.profilePropertyService = profilePropertyService;
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

    if (CollectionUtils.isNotEmpty(filter.getSpaceIdentityIds())) {
      String permissionsQuery = buildSpacePermissionsExpression(filter);
      mustClauses.add(permissionsQuery);
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
            + "                      }],\n"
            + "                      \"must\": {\n"
            + "                       \"term\": {\n"
            + "                         \"external\": false\n"
            + "                         }\n"
            + "                      }\n"
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
          enrollmentShoulds.add("                  {\n"
            + "                    \"term\": {\n"
            + "                      \"external\": true\n"
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
        remoteIds.append("\"").append(remoteId).append("\"");
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
      keys = newInputName.split(" ");
      if (keys.length > 1) {
        StringBuilder nameEsExp = new StringBuilder();
        // We will not search on userName because it doesn't contain a space character
        for (int i = 0; i < keys.length; i++) {
          if(StringUtils.isNotBlank(keys[i])) {
            if (i != 0) {
              nameEsExp.append(") AND (");
            }
            String searchedWord = StorageUtils.ASTERISK_STR + removeAccents(keys[i]) + StorageUtils.ASTERISK_STR;
            nameEsExp.append(" name.whitespace:").append(searchedWord);
            if (filter.isSearchEmail()) {
              nameEsExp.append(" OR email:").append(searchedWord);
            }
            if (filter.isSearchUserName()) {
              nameEsExp.append(" OR userName:").append(searchedWord);
            }
          }
        }
        if(StringUtils.isNotBlank(nameEsExp.toString())) {
          esExp.append("(").append(nameEsExp).append(")");
        }
      } else if (StringUtils.isNotBlank(newInputName)) {
        String searchedText = StorageUtils.ASTERISK_STR + removeAccents(newInputName) + StorageUtils.ASTERISK_STR;
        esExp.append("name.whitespace:").append(searchedText);

        if (filter.isSearchEmail()) {
          esExp.append(" OR email:").append(searchedText);
        }
        if (filter.isSearchUserName()) {
          esExp.append(" OR userName:").append(searchedText);
        }
      } else {
        esExp.append("name.whitespace:").append(StorageUtils.ASTERISK_STR).append(removeAccents(newInputName)).append(StorageUtils.ASTERISK_STR);
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
    for(String key : settings.keySet()) {
      ProfilePropertySetting property = profilePropertyService.getProfileSettingByName(key);
      if (property != null) {
        if (index > 0) {
          query.append(",");
        }
        String value = settings.get(key);
        value = escapeESReservedChars(value);
        if (StringUtils.isNotBlank(value)) {
          StringBuilder expression = new StringBuilder();
          String[] splittedValues = value.split(" ");
          if (splittedValues.length > 1) {
            for (int i = 0; i < splittedValues.length; i++) {
              if (StringUtils.isNotBlank(splittedValues[i])) {
                if (i != 0 && StringUtils.isNotBlank(expression)) {
                  expression.append(" AND ");
                }
                String searchedText;
                if (filter.isWildcardSearch()) {
                  searchedText = StorageUtils.ASTERISK_STR + removeAccents(splittedValues[i]) + StorageUtils.ASTERISK_STR;
                } else {
                  searchedText = removeAccents(splittedValues[i]);
                }
                expression.append(" ").append(key.replace(" ", "\\\\ ")).append(isEmailProfileProperty(key) ? ":" : ".whitespace:").append(searchedText);
              }
            }
            query.append("""
                        {
                          "query_string": {
                            "query": "%s"
                          }
                       }
                      """.formatted(expression.toString()));
          } else {
            String searchedTex = filter.isWildcardSearch()
                                                          ? StorageUtils.ASTERISK_STR + removeAccents(value)
                                                              + StorageUtils.ASTERISK_STR
                                                          : removeAccents(value);
            String propertyName = property.getPropertyName();
            String filedName = isEmailProfileProperty(propertyName) ? propertyName : "%s.whitespace".formatted(propertyName);
            query.append("""
                  {
                    "query_string": {
                      "query": "%s:%s"
                    }
                 }
                """.formatted(filedName.replace(" ", "\\\\ "), searchedTex));
          }
          index++;
        }
      }
    }

    return query.toString();
  }

  private static String removeAccents(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return string;
  }

  public static String escapeESReservedChars(String string) {
    String [] ES_RESERVED_CHARS = new String []{"+","-","=","&&","||",">","<","!","(",")","{","}","[","]","^","\"","~","*","?",":","\\","/"};
    for(String c : ES_RESERVED_CHARS) {
      string = string.replace(c, "\\" + c);
    }
    return string;
  }

  private String buildSpacePermissionsExpression(ProfileFilter filter) {
    List<String> permissions = filter.getSpaceIdentityIds();
    StringBuilder query = new StringBuilder();
    query.append("      {\n");
    query.append("        \"terms\": {\n");
    query.append("          \"permissions\": [\n");
    query.append(String.join(",\n", permissions));
    query.append("\n");
    query.append("          ]\n");
    query.append("        }\n");
    query.append("      }");
    return query.toString();
  }
  private boolean isEmailProfileProperty(String propertyName) {
    return propertyName.equals("email");
  }
}
