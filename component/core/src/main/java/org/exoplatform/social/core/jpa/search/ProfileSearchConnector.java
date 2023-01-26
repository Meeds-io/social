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

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.service.LinkProvider;
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
  private String searchType;
  
  public ProfileSearchConnector(InitParams initParams, ElasticSearchingClient client) {
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");
    this.searchType = param.getProperty("searchType");
    this.client = client;
  }

  public List<Identity> search(Identity identity,
                                     ProfileFilter filter,
                                     Type type,
                                     long offset,
                                     long limit) {
    if(identity == null && filter.getViewerIdentity() != null) {
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
  
  private List<Identity> buildResult(String jsonResponse) {

    LOG.debug("Search Query response from ES : {} ", jsonResponse);
    List<Identity> results = new ArrayList<>();
    JSONParser parser = new JSONParser();

    Map<?, ?> json = null;
    try {
      json = (Map<?, ?>)parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) return results;

    //
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    Identity identity = null;
    Profile p;
    for(Object jsonHit : jsonHits) {
      JSONObject hitSource = (JSONObject) ((JSONObject) jsonHit).get("_source");
      String position = (String) hitSource.get("position");
      String aboutMe = (String) hitSource.get("aboutMe");
      String name = (String) hitSource.get("name");
      String userName = (String) hitSource.get("userName");
      String firstName = (String) hitSource.get("firstName");
      String lastName = (String) hitSource.get("lastName");
      String avatarUrl = (String) hitSource.get("avatarUrl");
      String email = (String) hitSource.get("email");
      String identityId = (String) ((JSONObject) jsonHit).get("_id");
      identity = new Identity(OrganizationIdentityProvider.NAME, userName);
      identity.setId(identityId);
      p = new Profile(identity);
      Profile profile = getIdentityProfile(userName);
      if (profile == null) {
        continue;
      }
      p.setId(identityId);
      p.setAvatarUrl(avatarUrl);
      p.setUrl(LinkProvider.getProfileUri(userName));
      p.setProperty(Profile.FULL_NAME, name);
      p.setProperty(Profile.FIRST_NAME, firstName);
      p.setProperty(Profile.LAST_NAME, lastName);
      p.setProperty(Profile.POSITION, position);
      p.setProperty(Profile.EMAIL, email);
      p.setProperty(Profile.USERNAME, userName);
      p.setProperty(Profile.ABOUT_ME, aboutMe);
      if ((String) profile.getProperty(Profile.EXTERNAL) != null && !((String) profile.getProperty(Profile.EXTERNAL)).isEmpty()) {
        p.setProperty(Profile.EXTERNAL, (String) profile.getProperty(Profile.EXTERNAL));
      }
      if( (String) profile.getProperty(Profile.SYNCHRONIZED_DATE) != null && !((String) profile.getProperty(Profile.SYNCHRONIZED_DATE)).isEmpty()){
        p.setProperty(Profile.SYNCHRONIZED_DATE, (String) profile.getProperty(Profile.SYNCHRONIZED_DATE));
      }
      if( (String) profile.getProperty(Profile.ENROLLMENT_DATE) != null && !((String) profile.getProperty(Profile.ENROLLMENT_DATE)).isEmpty()){
        p.setProperty(Profile.ENROLLMENT_DATE, (String) profile.getProperty(Profile.ENROLLMENT_DATE));
      }
      identity.setProfile(p);
      results.add(identity);
    }
    return results;
  }

  private Profile getIdentityProfile(String userName) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    return identity == null ? null : identity.getProfile();
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

        esSubQuery.append("    \"filter\": [\n");
        esSubQuery.append("      {");
        esSubQuery.append("          \"query_string\": {\n");
        esSubQuery.append("            \"query\": \"" + expEsForAdvancedFilter + "\"\n");
        esSubQuery.append("          }\n");
        esSubQuery.append("      }\n");
        esSubQuery.append("    ],\n");

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
    }
    if (identity != null && type != null) {
      esSubQuery.append("      \"must\" : {\n");
      esSubQuery.append("        \"query_string\" : {\n");
      esSubQuery.append("          \"query\" : \""+ identity.getId() +"\",\n");
      esSubQuery.append("          \"fields\" : [\"" + buildTypeEx(type) + "\"]\n");
      esSubQuery.append("        }\n");
      esSubQuery.append("      }\n");
      subQueryEmpty = false;
      appendCommar = true;
    } else if (filter.getExcludedIdentityList() != null && filter.getExcludedIdentityList().size() > 0) {
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

    esQuery.append("}\n");
    LOG.debug("Search Query request to ES : {} ", esQuery);

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
    char firstChar = filter.getFirstCharacterOfName();
    //
    if (firstChar != '\u0000') {
      String filterField = "name";
      if (filter.getFirstCharFieldName() != null) {
        switch (SortBy.valueOf(filter.getFirstCharFieldName().toUpperCase())) {
        case FIRSTNAME:
          filterField = "firstName";
          break;
        case LASTNAME:
          filterField = "lastName";
          break;
        default:
          // Filter by first character on full name
          filterField = "name";
          break;
        }
      }

      esExp.append(filterField)
           .append(".whitespace:")
           .append(firstChar)
           .append(StorageUtils.ASTERISK_STR);
      return esExp.toString();
    }

    //
    String inputName = StringUtils.isBlank(filter.getName()) ? null : filter.getName().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
    if (StringUtils.isNotBlank(inputName)) {
     //
      String newInputName = null;
      String[] keys = new String[0];
      if (inputName.startsWith("\"") && inputName.endsWith("\"")) {
        newInputName = inputName.replace("\"", "");
      } else {
        keys = inputName.split(" ");
      }
      if (keys.length > 1) {
        // We will not search on username because it doesn't contain a space character
        esExp.append("(");
        for (int i = 0; i < keys.length; i++) {
          if (i != 0 ) {
            esExp.append(" AND ") ;
          }
          esExp.append(" name.whitespace:").append(StorageUtils.ASTERISK_STR).append(removeAccents(keys[i])).append(StorageUtils.ASTERISK_STR);
        }
        esExp.append(")");
      } else if (StringUtils.isNotBlank(newInputName)) {
        esExp.append("name:").append(removeAccents(newInputName));
      } else {
        esExp.append("( name.whitespace:").append(StorageUtils.ASTERISK_STR).append(removeAccents(inputName)).append(StorageUtils.ASTERISK_STR);
        esExp.append(")");
      }
    }
    return esExp.toString();
  }

  private String buildAdvancedFilterExpression(ProfileFilter filter) {
    StringBuilder esExp = new StringBuilder();
    esExp.append("( ");
    Map<String, String> settings = filter.getProfileSettings();
    int index = 0 ;
    for (Map.Entry<String, String> entry : settings.entrySet()){
      String[] splittedValue = new String[0];
      String inputValue = entry.getValue().replace(StorageUtils.ASTERISK_STR, StorageUtils.EMPTY_STR);
      if (inputValue.startsWith("\"") && inputValue.endsWith("\"")) {
        inputValue = inputValue.replace("\"", "");
      }
      splittedValue = inputValue.split(" ");
      if (splittedValue.length > 1) {
        esExp.append("(");
        for (int i = 0; i < splittedValue.length; i++) {
          if (i != 0 ) {
            esExp.append(" AND ") ;
          }
          esExp.append(entry.getKey()+":").append(StorageUtils.ASTERISK_STR).append(removeAccents(splittedValue[i])).append(StorageUtils.ASTERISK_STR);
        }
        esExp.append(")");
      } else {
        esExp.append("( "+entry.getKey()+":").append(StorageUtils.ASTERISK_STR).append(removeAccents(inputValue)).append(StorageUtils.ASTERISK_STR);
        esExp.append(")");
      }
      if ( index != settings.size()- 1 ) esExp.append(" AND ") ;
      index += 1 ;
    }
    esExp.append(" )");
    return esExp.toString();
  }

  private static String removeAccents(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return string;
  }
}
