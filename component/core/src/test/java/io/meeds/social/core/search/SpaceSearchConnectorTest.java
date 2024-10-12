/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.core.search;

import static io.meeds.social.core.search.SpaceSearchConnector.SEARCH_COUNT_FILE_PATH_PARAM;
import static io.meeds.social.core.search.SpaceSearchConnector.SEARCH_QUERY_FILE_PATH_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.JsonNode;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;

import io.meeds.social.core.search.model.SpaceSearchFilter;
import io.meeds.social.core.search.model.SpaceSearchResult;
import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.util.JsonUtils;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class SpaceSearchConnectorTest {

  private static final String PERMISSIONS_FIELD = "permissions";

  private static final String TERM              = "term";

  private static final String PHRASE            = "term1 term2";

  private static final long   ID                = 461l;

  private static final String SEARCH_RESULT     = String.format("""
        {
           "hits":{
              "hits":[
                 {
                    "_id":"%s",
                    "_score":2.6705298,
                    "highlight":{
                       "displayName":[
                          "<span class='searchMatchExcerpt'>Test</span> space"
                       ],
                       "description":[
                          "<span class='searchMatchExcerpt'>Test</span> space #TestTag"
                       ]
                    }
                 }
              ]
           }
        }
      """, ID);

  private static final String USER_NAME         = "testuser";

  String                      index             = "_space";

  String                      searchPath        = "spaces-search-query.json";

  String                      countPath         = "spaces-count-query.json";

  @Mock
  ConfigurationManager        configurationManager;

  @Mock
  ElasticSearchingClient      client;

  @Mock
  InitParams                  initParams;

  @Mock
  PropertiesParam             param;

  SpaceSearchConnector        spaceSearchConnector;

  @Before
  @SneakyThrows
  @SuppressWarnings("resource")
  public void setup() {
    when(initParams.getPropertiesParam("constructor.params")).thenReturn(param);
    when(param.getProperty("index")).thenReturn(index);

    ValueParam valueParam = mock(ValueParam.class);
    when(initParams.getValueParam(SEARCH_QUERY_FILE_PATH_PARAM)).thenReturn(valueParam);
    when(valueParam.getValue()).thenReturn(searchPath);

    valueParam = mock(ValueParam.class);
    when(initParams.getValueParam(SEARCH_COUNT_FILE_PATH_PARAM)).thenReturn(valueParam);
    when(valueParam.getValue()).thenReturn(countPath);
    when(configurationManager.getInputStream(anyString())).thenAnswer(invocation -> getClass().getClassLoader()
                                                                                              .getResourceAsStream(invocation.getArgument(0)));

    spaceSearchConnector = new SpaceSearchConnector(configurationManager, client, initParams);
  }

  @Test
  public void testSearchFavorites() {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME, null, true, null, null);
    when(client.sendRequest(argThat(esQuery -> hasUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery, "member", 1, USER_NAME)),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  @Test
  public void testNotSearchWhenNotUnifiedSearch() {
    assertThrows(IllegalArgumentException.class,
                 () -> spaceSearchConnector.search(new SpaceSearchFilter(USER_NAME,
                                                                         null,
                                                                         false,
                                                                         null,
                                                                         SpaceMembershipStatus.MEMBER),
                                                   0,
                                                   10));
  }

  @Test
  public void testSearchMembers() {
    checkPermissionField(SpaceMembershipStatus.MEMBER, "member");
  }

  @Test
  public void testSearchManagers() {
    checkPermissionField(SpaceMembershipStatus.MANAGER, "manager");
  }

  @Test
  public void testSearchPending() {
    checkPermissionField(SpaceMembershipStatus.PENDING, "pending");
  }

  @Test
  public void testSearchInvited() {
    checkPermissionField(SpaceMembershipStatus.INVITED, "invited");
  }

  @Test
  public void testSearchPublisher() {
    checkPermissionField(SpaceMembershipStatus.PUBLISHER, "publisher");
  }

  @Test
  public void testSearchRedactor() {
    checkPermissionField(SpaceMembershipStatus.REDACTOR, "redactor");
  }

  @Test
  public void testSearchSpaceNoMembership() {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME, PHRASE, false, null, null);
    when(client.sendRequest(argThat(esQuery -> hasNotUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery,
                                                                         PERMISSIONS_FIELD,
                                                                         2,
                                                                         "all",
                                                                         USER_NAME)),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  @Test
  public void testSearchTags() {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME, null, false, Arrays.asList("tag1", "tag2"), null);
    when(client.sendRequest(argThat(esQuery -> hasNotUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery, PERMISSIONS_FIELD, 2, "all", USER_NAME)
                                               && hasTagsQueryPart(esQuery, 2, "tag1", "tag2")),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  private void checkPermissionField(SpaceMembershipStatus status, String fieldName) {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME, TERM, false, null, status);
    when(client.sendRequest(argThat(esQuery -> hasNotUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery, fieldName, 1, USER_NAME)),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  @SneakyThrows
  private boolean hasUserFavoriteQueryPart(String esQuery) {
    JsonNode favorite = getFilterNode(esQuery, "metadatas.favorites.metadataName.keyword");
    return favorite != null && favorite.size() == 1 && USER_NAME.equals(favorite.get(0).asText());
  }

  @SneakyThrows
  private boolean hasNotUserFavoriteQueryPart(String esQuery) {
    JsonNode favorite = getFilterNode(esQuery, "metadatas.favorites.metadataName.keyword");
    return favorite == null;
  }

  @SneakyThrows
  private boolean hasPermissionQueryPart(String esQuery, String permissionField, int size, String... permissionValues) {
    JsonNode permission = getFilterNode(esQuery, permissionField);
    return permission != null
           && permission.size() == size
           && StreamSupport.stream(permission.spliterator(), false)
                           .allMatch(e -> Stream.of(permissionValues)
                                                .anyMatch(v -> v.equals(e.asText())));
  }

  @SneakyThrows
  private boolean hasTagsQueryPart(String esQuery, int size, String... tagValues) {
    return Stream.of(tagValues).allMatch(v -> hasTagsNode(esQuery, v));
  }

  @SneakyThrows
  private JsonNode getFilterNode(String esQuery, String filterName) {
    JsonNode jsonNode = JsonUtils.OBJECT_MAPPER.readTree(esQuery);
    JsonNode filter = jsonNode.get("query")
                              .get("bool")
                              .get("filter");
    if (filter == null || filter.size() == 0) {
      return null;
    }
    Iterator<JsonNode> filterElements = filter.elements();
    JsonNode filterNode = null;
    while (filterElements.hasNext() && filterNode == null) {
      JsonNode filterElement = filterElements.next();
      JsonNode terms = filterElement.get("terms");
      filterNode = terms == null ? null : terms.get(filterName);
    }
    return filterNode;
  }

  @SneakyThrows
  private boolean hasTagsNode(String esQuery, String tagValue) {
    JsonNode jsonNode = JsonUtils.OBJECT_MAPPER.readTree(esQuery);
    JsonNode filter = jsonNode.get("query")
                              .get("bool")
                              .get("should");
    if (filter == null || filter.size() == 0) {
      return false;
    }
    return StreamSupport.stream(filter.spliterator(), false)
                        .anyMatch(element -> {
                          JsonNode term = element.get("term");
                          JsonNode tagElement = term == null ? null : term.get("metadatas.tags.metadataName.keyword");
                          return tagElement != null && tagValue.equals(tagElement.get("value").asText());
                        });
  }

}
