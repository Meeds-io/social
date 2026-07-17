/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.cms.storage.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;

import io.meeds.social.search.model.PageSearchResult;

@RunWith(MockitoJUnitRunner.class)
public class PageContentSearchConnectorTest {

  private static final String        QUERY_TEMPLATE = "{\"q\":\"@term@\",\"filter\":@permissions_filter@,"
      + "\"from\":\"@offset@\",\"size\":\"@limit@\"}";

  @Mock
  private ConfigurationManager       configurationManager;

  @Mock
  private ElasticSearchingClient     client;

  @Mock
  private LayoutService              layoutService;

  @Mock
  private ResourceBundleManager      resourceBundleManager;

  @Mock
  private FavoriteService            favoriteService;

  @Mock
  private IdentityManager            identityManager;

  private PageContentSearchConnector connector;

  @Before
  public void setup() throws Exception {
    when(configurationManager.getInputStream(anyString())).thenAnswer(invocation -> toStream(QUERY_TEMPLATE));
    InitParams params = new InitParams();
    ValueParam queryFileParam = new ValueParam();
    queryFileParam.setName("query.file.path");
    queryFileParam.setValue("query.json");
    params.addParameter(queryFileParam);
    connector = new PageContentSearchConnector(configurationManager,
                                               client,
                                               layoutService,
                                               resourceBundleManager,
                                               favoriteService,
                                               identityManager,
                                               params);
  }

  @After
  public void tearDown() {
    ConversationState.setCurrent(null);
  }

  @Test
  public void shouldThrowWhenTermIsBlank() {
    try {
      connector.search("", 0, 10, Locale.ENGLISH);
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void shouldOnlyFilterOnEveryoneWhenNoCurrentIdentity() {
    when(client.sendRequest(any(), any())).thenAnswer(invocation -> {
      String query = invocation.getArgument(0);
      assertTrue(query.contains("\"term\": {\"permissions\": \"Everyone\"}"));
      return emptyResponse();
    });

    connector.search("term", 0, 10, Locale.ENGLISH);
  }

  @Test
  public void shouldFilterOnUsernameEveryoneAndMembershipsWhenIdentityPresent() {
    Identity identity = new Identity("john", Arrays.asList(new MembershipEntry("/spaces/x", "manager")));
    ConversationState.setCurrent(new ConversationState(identity));

    when(client.sendRequest(any(), any())).thenAnswer(invocation -> {
      String query = invocation.getArgument(0);
      assertTrue(query.contains("\"term\": {\"permissions\": \"john\"}"));
      assertTrue(query.contains("\"term\": {\"permissions\": \"Everyone\"}"));
      assertTrue(query.contains("\"regexp\": {\"permissions\": \"manager:/spaces/x|*:/spaces/x\"}"));
      return emptyResponse();
    });

    connector.search("term", 0, 10, Locale.ENGLISH);
  }

  @Test
  public void shouldReplaceOffsetAndLimitAndEscapedTerm() {
    when(client.sendRequest(any(), any())).thenAnswer(invocation -> {
      String query = invocation.getArgument(0);
      assertTrue(query.contains("\"from\":\"5\""));
      assertTrue(query.contains("\"size\":\"15\""));
      assertTrue(query.contains("\"q\":\"my term\""));
      return emptyResponse();
    });

    connector.search("my+term", 5, 15, Locale.ENGLISH);
  }

  @Test
  public void shouldParseHitsIntoPageSearchResults() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "siteName": "site",
                  "pageName": "page",
                  "pageTitle": "My Page",
                  "pagePath": "/portal/site/page",
                  "author": "john",
                  "lastUpdatedDate": 1234567890
                },
                "highlight": {
                  "content": ["Hello <span class='searchMatchExcerpt'>world</span>"]
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("world", 0, 10, Locale.ENGLISH);

    assertEquals(1, results.size());
    PageSearchResult result = results.get(0);
    assertEquals("page_139", result.getId());
    assertEquals("site", result.getSiteName());
    assertEquals("page", result.getPageName());
    assertEquals("My Page", result.getPageTitle());
    assertEquals("/portal/site/page", result.getPagePath());
    assertEquals("john", result.getAuthor());
    assertEquals(1234567890L, result.getDate());
    assertEquals(1, result.getExcerpts().size());
    assertTrue(result.getExcerpts().get(0).contains("world"));
  }

  @Test
  public void shouldOnlyReturnExcerptInSearchingUserLanguage() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "content": "default language content",
                  "content-fr": "contenu en francais"
                },
                "highlight": {
                  "content": ["default language excerpt"],
                  "content-fr": ["extrait en francais"]
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("term", 0, 10, Locale.FRENCH);

    assertEquals(1, results.get(0).getExcerpts().size());
    assertEquals("extrait en francais", results.get(0).getExcerpts().get(0));
  }

  @Test
  public void shouldExcludeResultWhenTranslationExistsButOnlyDefaultContentMatched() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "content": "default content",
                  "content-fr": "fr content"
                },
                "highlight": {
                  "content": ["<span>default</span> content"]
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("default", 0, 10, Locale.FRENCH);

    assertTrue(results.isEmpty());
  }

  @Test
  public void shouldKeepResultWithoutExcerptWhenMatchedThroughTitleOnly() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "pageTitle": "Default meeting notes",
                  "content": "default content",
                  "content-fr": "fr content"
                },
                "highlight": {
                  "pageTitle": ["<span>Default</span> meeting notes"]
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("default", 0, 10, Locale.FRENCH);

    assertEquals(1, results.size());
    assertTrue(results.get(0).getExcerpts().isEmpty());
  }

  @Test
  public void shouldExcludeResultWhenOnlyAnUnrelatedLanguageMatchedAndNoTranslationForUser() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "content": "the original english content",
                  "content-de": "der deutsche inhalt mit dem begriff"
                },
                "highlight": {
                  "content-de": ["der deutsche inhalt mit dem <span>begriff</span>"]
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("begriff", 0, 10, Locale.FRENCH);

    assertTrue(results.isEmpty());
  }

  @Test
  public void shouldFallBackToDefaultLanguageExcerptWhenNoTranslationForUserLanguage() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {},
                "highlight": {
                  "content": ["default language excerpt"]
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("term", 0, 10, Locale.FRENCH);

    assertEquals(1, results.get(0).getExcerpts().size());
    assertEquals("default language excerpt", results.get(0).getExcerpts().get(0));
  }

  @Test
  public void shouldReturnPlainSiteLabelWhenNotAnExpression() {
    PortalConfig portalConfig = mock(PortalConfig.class);
    when(portalConfig.getLabel()).thenReturn("My Workspace");
    when(layoutService.getPortalConfig(new SiteKey("portal", "myworkspace"))).thenReturn(portalConfig);

    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "siteType": "portal",
                  "siteName": "myworkspace"
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("world", 0, 10, Locale.ENGLISH);

    assertEquals("My Workspace", results.get(0).getSiteName());
  }

  @Test
  public void shouldResolveExpressionBoundSiteLabelInSearchingUserLocale() {
    PortalConfig portalConfig = mock(PortalConfig.class);
    when(portalConfig.getLabel()).thenReturn("#{portal.myworkspace.name}");
    when(layoutService.getPortalConfig(new SiteKey("portal", "myworkspace"))).thenReturn(portalConfig);
    ResourceBundle bundle = mock(ResourceBundle.class);
    when(bundle.getString("portal.myworkspace.name")).thenReturn("My Workspace FR");
    when(resourceBundleManager.getNavigationResourceBundle("fr", "portal", "myworkspace")).thenReturn(bundle);

    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "siteType": "portal",
                  "siteName": "myworkspace"
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("world", 0, 10, Locale.FRENCH);

    assertEquals("My Workspace FR", results.get(0).getSiteName());
  }

  @Test
  public void shouldFallBackToRawSiteNameWhenSiteNotFound() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "siteType": "portal",
                  "siteName": "unknown"
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("world", 0, 10, Locale.ENGLISH);

    assertEquals("unknown", results.get(0).getSiteName());
  }

  @Test
  public void shouldMarkResultAsFavoriteWhenAlreadyFavorited() {
    Identity identity = new Identity("john", Arrays.asList());
    ConversationState.setCurrent(new ConversationState(identity));
    org.exoplatform.social.core.identity.model.Identity socialIdentity =
                                                                        mock(org.exoplatform.social.core.identity.model.Identity.class);
    when(socialIdentity.getId()).thenReturn("42");
    when(identityManager.getOrCreateUserIdentity("john")).thenReturn(socialIdentity);
    when(favoriteService.isFavorite(new Favorite("page", "page_139", null, 42L))).thenReturn(true);

    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "pageTitle": "test",
                  "content": "hello world"
                },
                "highlight": {
                  "content": ["hello <span>world</span>"]
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("world", 0, 10, Locale.ENGLISH);

    assertTrue(results.get(0).isFavorite());
  }

  @Test
  public void shouldMarkResultAsNotFavoriteWhenNoCurrentIdentity() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "pageTitle": "test",
                  "content": "hello world"
                },
                "highlight": {
                  "content": ["hello <span>world</span>"]
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    List<PageSearchResult> results = connector.search("world", 0, 10, Locale.ENGLISH);

    assertTrue(!results.get(0).isFavorite());
  }

  @Test
  public void shouldGetByIdWithPlainSnippetInUserLanguage() {
    String response = """
        {
          "hits": {
            "hits": [
              {
                "_id": "page_139",
                "_source": {
                  "siteName": "site",
                  "pageName": "page",
                  "pageTitle": "My Page",
                  "pagePath": "/portal/site/page",
                  "author": "john",
                  "lastUpdatedDate": 1234567890,
                  "content": "default content",
                  "content-fr": "contenu francais"
                }
              }
            ]
          }
        }
        """;
    when(client.sendRequest(any(), any())).thenReturn(response);

    PageSearchResult result = connector.getById("page_139", Locale.FRENCH);

    assertEquals("page_139", result.getId());
    assertEquals("My Page", result.getPageTitle());
    assertEquals(1, result.getExcerpts().size());
    assertEquals("contenu francais", result.getExcerpts().get(0));
  }

  @Test
  public void shouldGetByIdReturnNullWhenNotFound() {
    when(client.sendRequest(any(), any())).thenReturn(emptyResponse());

    assertEquals(null, connector.getById("page_139", Locale.ENGLISH));
  }

  private String emptyResponse() {
    return "{\"hits\": {\"hits\": []}}";
  }

  private InputStream toStream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

}
