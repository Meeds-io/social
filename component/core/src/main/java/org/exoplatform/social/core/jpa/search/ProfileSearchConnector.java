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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.storage.impl.StorageUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2015  
 */
public class ProfileSearchConnector {
  private static final Log LOG = ExoLogger.getLogger(ProfileSearchConnector.class);
  private final ElasticSearchingClient client;
  private String index;
  
  public ProfileSearchConnector(InitParams initParams, ElasticSearchingClient client) {
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
   * @param type type type
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
    String expEsForAdvancedFilter = !filter.getProfileSettings().isEmpty() ? buildAdvancedFilterExpression(filter) : null;
    StringBuilder esQuery = new StringBuilder();
    esQuery.append("{\n");
    esQuery.append("   \"from\" : " + offset + ", \"size\" : " + limit + ",\n");
    Sorting sorting = filter.getSorting();
    if(sorting != null && sorting.sortBy != null) {
      esQuery.append("   \"sort\": {");
      switch (sorting.sortBy) {
      case DATE:
        esQuery.append("\"lastUpdatedDate\"");
        break;
      case FIRSTNAME:
        esQuery.append("\"firstName.raw\"");
        break;
      case LASTNAME:
        esQuery.append("\"lastName.raw\"");
        break;
      default:
        // Title, Fullname, any other condition, we will use the fullname
        esQuery.append("\"name.raw\"");
        break;
      }
      esQuery.append(": {\"order\": \"")
             .append(sorting.orderBy == null ? "asc" : sorting.orderBy.name())
             .append("\"}}\n");
    } else {
      esQuery.append("   \"sort\": {\"name.raw\": {\"order\": \"asc\"}}\n");
    }
    StringBuilder esSubQuery = new StringBuilder();
    esSubQuery.append("       ,\n");
    esSubQuery.append("\"query\" : {\n");
    esSubQuery.append("      \"constant_score\" : {\n");
    esSubQuery.append("        \"filter\" : {\n");
    esSubQuery.append("          \"bool\" :{\n");
    boolean subQueryEmpty = true;
    boolean appendCommar = false;
    //filter by profile settings
      if (expEsForAdvancedFilter != null) {
        esSubQuery.append(expEsForAdvancedFilter);
        esSubQuery.append(",\n");

      }
    if (filter.getUserType() != null && !filter.getUserType().isEmpty()) {
      if (filter.getUserType().equals("internal")) {
        esSubQuery.append("    \"should\": [\n");
        esSubQuery.append("                  {\n");
        esSubQuery.append("                    \"term\": {\n");
        esSubQuery.append("                      \"external\": false\n");
        esSubQuery.append("                    }\n");
        esSubQuery.append("                  },\n");
        esSubQuery.append("                  {\n");
        esSubQuery.append("                    \"bool\": {\n");
        esSubQuery.append("                      \"must_not\": {\n");
        esSubQuery.append("                        \"exists\": {\n");
        esSubQuery.append("                          \"field\": \"external\"\n");
        esSubQuery.append("                        }\n");
        esSubQuery.append("                      }\n");
        esSubQuery.append("                    }\n");
        esSubQuery.append("                  }\n");
        esSubQuery.append("                  ]\n");
        subQueryEmpty = false;
        appendCommar = true;
      } else if (filter.getUserType().equals("external")) {
        esSubQuery.append("    \"should\": [\n");
        esSubQuery.append("                  {\n");
        esSubQuery.append("                    \"term\": {\n");
        esSubQuery.append("                      \"external\": true\n");
        esSubQuery.append("                    }\n");
        esSubQuery.append("                  }\n");
        esSubQuery.append("                  ]\n");
        subQueryEmpty = false;
        appendCommar = true;
      }
      esSubQuery.append("                  ,\"minimum_should_match\" : 1\n");

    }
    if (filter.isConnected() != null) {
      esSubQuery.append("    \"should\": [\n");
      esSubQuery.append("                  {\n");
      esSubQuery.append("                    \"bool\": {\n");
      if (filter.isConnected()) {
        esSubQuery.append("                      \"must\": {\n");
      } else {
        esSubQuery.append("                      \"must_not\": {\n");
      }
      esSubQuery.append("                        \"exists\": {\n");
      esSubQuery.append("                          \"field\": \"lastLoginTime\"\n");
      esSubQuery.append("                        }\n");
      esSubQuery.append("                      }\n");
      esSubQuery.append("                    }\n");
      esSubQuery.append("                  }\n");
      esSubQuery.append("                  ]\n");
      esSubQuery.append("                  ,\"minimum_should_match\" : 1\n");
      subQueryEmpty = false;
      appendCommar = true;
    }
    if(filter.getEnrollmentStatus() != null && !filter.getEnrollmentStatus().isEmpty()) {
      switch (filter.getEnrollmentStatus()) {
        case "enrolled": {
          esSubQuery.append("    \"should\": [\n");
          esSubQuery.append("                  {\n");
          esSubQuery.append("                    \"bool\": {\n");
          esSubQuery.append("                      \"must\": {\n");
          esSubQuery.append("                        \"exists\": {\n");
          esSubQuery.append("                          \"field\": \"enrollmentDate\"\n");
          esSubQuery.append("                        }\n");
          esSubQuery.append("                      }\n");
          esSubQuery.append("                    }\n");
          esSubQuery.append("                  }\n");
          esSubQuery.append("                  ]\n");
          esSubQuery.append("                  ,\"minimum_should_match\" : 1\n");
          subQueryEmpty = false;
          appendCommar = true;
          break;
        }

        case "notEnrolled": {
          esSubQuery.append("    \"should\": [\n");
          esSubQuery.append("                  {\n");
          esSubQuery.append("                    \"bool\": {\n");
          esSubQuery.append("                      \"must_not\": [{\n");
          esSubQuery.append("                        \"exists\": {\n");
          esSubQuery.append("                          \"field\": \"enrollmentDate\"\n");
          esSubQuery.append("                          }\n");
          esSubQuery.append("                        },\n");
          esSubQuery.append("                      {\n");
          esSubQuery.append("                        \"exists\": {\n");
          esSubQuery.append("                          \"field\": \"lastLoginTime\"\n");
          esSubQuery.append("                        }\n");
          esSubQuery.append("                      }],\n");
          esSubQuery.append("                      \"must\": {\n");
          esSubQuery.append("                       \"term\": {\n");
          esSubQuery.append("                         \"external\": false\n");
          esSubQuery.append("                         }\n");
          esSubQuery.append("                      }\n");
          esSubQuery.append("                    }\n");
          esSubQuery.append("                  }\n");
          esSubQuery.append("                  ]\n");
          esSubQuery.append("                  ,\"minimum_should_match\" : 1\n");
          subQueryEmpty = false;
          appendCommar = true;
          break;
        }

        case "noEnrollmentPossible": {

          esSubQuery.append("    \"should\": [\n");
          esSubQuery.append("                  {\n");
          esSubQuery.append("                    \"bool\": {\n");
          esSubQuery.append("                      \"must_not\": {\n");
          esSubQuery.append("                        \"exists\": {\n");
          esSubQuery.append("                          \"field\": \"enrollmentDate\"\n");
          esSubQuery.append("                          }\n");
          esSubQuery.append("                        },\n");
          esSubQuery.append("                      \"must\": {\n");
          esSubQuery.append("                        \"exists\": {\n");
          esSubQuery.append("                          \"field\": \"lastLoginTime\"\n");
          esSubQuery.append("                        }\n");
          esSubQuery.append("                      }\n");
          esSubQuery.append("                    }\n");
          esSubQuery.append("                  },\n");
          esSubQuery.append("                  {\n");
          esSubQuery.append("                    \"term\": {\n");
          esSubQuery.append("                      \"external\": true\n");
          esSubQuery.append("                    }\n");
          esSubQuery.append("                  }\n");
          esSubQuery.append("                  ]\n");
          esSubQuery.append("                  ,\"minimum_should_match\" : 1\n");
          subQueryEmpty = false;
          appendCommar = true;
          break;
        }

        default:
          break;
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
      esSubQuery.append("      \"must\" : {\n");
      esSubQuery.append("        \"terms\" :{\n");
      esSubQuery.append("          \"userName\" : [" + remoteIds.toString() + "]\n");
      esSubQuery.append("        } \n");
      esSubQuery.append("      },\n");
      subQueryEmpty = false;
      appendCommar = false;
    }
    if (identity != null && type != null) {
      if(appendCommar) {
        esSubQuery.append("      ,\n");
      }
      esSubQuery.append("      \"must\" : {\n");
      esSubQuery.append("        \"query_string\" : {\n");
      esSubQuery.append("          \"query\" : \""+ identity.getId() +"\",\n");
      esSubQuery.append("          \"fields\" : [\"" + buildTypeEx(type) + "\"]\n");
      esSubQuery.append("        }\n");
      esSubQuery.append("      }\n");
      subQueryEmpty = false;
      appendCommar = true;
    } else if (filter.getExcludedIdentityList() != null && filter.getExcludedIdentityList().size() > 0) {
      if(appendCommar) {
        esSubQuery.append("      ,\n");
      }
      esSubQuery.append("      \"must_not\": [\n");
      esSubQuery.append("        {\n");
      esSubQuery.append("          \"ids\" : {\n");
      esSubQuery.append("             \"values\" : [" + buildExcludedIdentities(filter) + "]\n");
      esSubQuery.append("          }\n");
      esSubQuery.append("        }\n");
      esSubQuery.append("      ]\n");
      subQueryEmpty = false;
      appendCommar = true;
    }
    //if the search fields are existing.
    if (expEs != null && expEs.length() > 0) {
      if(appendCommar) {
        esSubQuery.append("      ,\n");
      }
      esSubQuery.append("    \"filter\": [\n");
      esSubQuery.append("      {");
      esSubQuery.append("          \"query_string\": {\n");
      if (filter.getName().startsWith("\"") && filter.getName().endsWith("\"")) {
        esSubQuery.append("            \"query\": \"" + expEs + "\",\n");
        esSubQuery.append("            \"default_operator\": \"" + "AND" + "\"\n");
      } else {
        esSubQuery.append("            \"query\": \"" + expEs + "\"\n");
      }
      esSubQuery.append("          }\n");
      esSubQuery.append("      }\n");
      esSubQuery.append("    ]\n");
      subQueryEmpty = false;
    } //end if
    esSubQuery.append("     } \n");
    esSubQuery.append("   } \n");
    esSubQuery.append("  }\n");
    esSubQuery.append(" }\n");
    if(!subQueryEmpty) {
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
      String newInputName = null;
      String[] keys = new String[0];
      newInputName = inputName.trim();
      if (newInputName.startsWith("\"") && inputName.endsWith("\"")) {
        newInputName = inputName.replace("\"", "");
      }
      keys = newInputName.split(" ");
      if (keys.length > 1) {
        // We will not search on userName because it doesn't contain a space character
        esExp.append("(");
        for (int i = 0; i < keys.length; i++) {
          if (i != 0 ) {
            esExp.append(") AND (");
          }
          esExp.append(" name.whitespace:").append(StorageUtils.ASTERISK_STR).append(removeAccents(keys[i])).append(StorageUtils.ASTERISK_STR);
          if (filter.isSearchEmail()) {
            esExp.append(" OR email:").append(StorageUtils.ASTERISK_STR).append(removeAccents(keys[i])).append(StorageUtils.ASTERISK_STR);
          }
          if (filter.isSearchUserName()) {
            esExp.append(" OR userName:").append(StorageUtils.ASTERISK_STR).append(removeAccents(keys[i])).append(StorageUtils.ASTERISK_STR);
          }
        }
        esExp.append(")");
      } else if (StringUtils.isNotBlank(newInputName)) {
        esExp.append("name.whitespace:").append(StorageUtils.ASTERISK_STR).append(removeAccents(newInputName)).append(StorageUtils.ASTERISK_STR);
        if (filter.isSearchEmail()) {
          esExp.append(" OR email:").append(StorageUtils.ASTERISK_STR).append(removeAccents(newInputName)).append(StorageUtils.ASTERISK_STR);
        }
        if (filter.isSearchUserName()) {
          esExp.append(" OR userName:").append(StorageUtils.ASTERISK_STR).append(removeAccents(newInputName)).append(StorageUtils.ASTERISK_STR);
        }
      } else {
        esExp.append("name.whitespace:").append(StorageUtils.ASTERISK_STR).append(removeAccents(newInputName)).append(StorageUtils.ASTERISK_STR);
      }
    }
    return esExp.toString();
  }

  private String buildAdvancedFilterExpression(ProfileFilter filter) {
    StringBuilder query = new StringBuilder();
    Map<String, String> settings = filter.getProfileSettings();
    query.append("""
              "must": [""");
    settings.forEach((key, value) -> query.append("""
                    {
                    "term": {
                       "%s.raw": "%s"
                     }
                   },
                """.formatted(key, value)));
    int lastComma = query.lastIndexOf(",");
    query.replace(lastComma, lastComma + 1, "");
    query.append("""
               ]
            """);

    return query.toString();
  }

  private static String removeAccents(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return string;
  }
}
