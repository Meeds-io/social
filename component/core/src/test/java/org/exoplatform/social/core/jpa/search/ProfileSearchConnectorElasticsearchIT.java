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
package org.exoplatform.social.core.jpa.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.meeds.social.identity.permission.service.UserPermissionService;
import io.meeds.social.translation.service.TranslationService;

import org.exoplatform.commons.search.es.client.ElasticIndexingAuditTrail;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;

/**
 * The enrollment filters of the users list, evaluated by Elasticsearch itself:
 * the queries {@link ProfileSearchConnector} builds, sent by the platform's
 * {@link ElasticSearchingClient}, against profiles indexed with the index
 * settings and the mapping the platform creates, as
 * {@link ProfileIndexingServiceConnector} builds their documents. The users
 * list answers through Elasticsearch when a name is searched, and through the
 * database otherwise, as the Pending Users tabs do: with a name, its
 * "not enrolled" and "enrolled" filters of never connected accounts and its
 * "no enrollment possible" filter count and list External accounts like the
 * others, as their database queries do.
 * <p>
 * Opt-in: the default build runs no {@code *IT} class. Run it against an
 * Elasticsearch of the platform's version with
 * {@code mvn -pl component/core -Prun-its verify -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=ProfileSearchConnectorElasticsearchIT -Dfailsafe.failIfNoSpecifiedTests=false -Des.it.url=http://127.0.0.1:9200};
 * it creates an index of its own and deletes it.
 */
public class ProfileSearchConnectorElasticsearchIT {

  private static final String                 ES_URL     = System.getProperty("exo.es.search.server.url",
                                                                              "http://127.0.0.1:9200");

  private static final String                 INDEX      = "profile_it_" + System.currentTimeMillis();

  private static final long                   NOW        = System.currentTimeMillis();

  private static final HttpClient             HTTP       = HttpClient.newHttpClient();

  /** Never invited, never signed in */
  private static final String                 ALICE_INT  = "1001";

  /** External, never invited, never signed in */
  private static final String                 BOB_EXT    = "1002";

  /** External, invited, never signed in */
  private static final String                 CAROL_EXT  = "1003";

  /** External, never invited, signed in */
  private static final String                 DAVE_EXT   = "1004";

  /** Invited, then signed in */
  private static final String                 ERIN_INT   = "1005";

  private static ProfileSearchConnector       searchConnector;

  @BeforeClass
  public static void indexProfiles() throws Exception {
    ProfilePropertyService profilePropertyService = mock(ProfilePropertyService.class);
    when(profilePropertyService.getPropertySettings()).thenReturn(Collections.emptyList());
    IdentityManager identityManager = mock(IdentityManager.class);
    UserPermissionService userPermissionService = mock(UserPermissionService.class);
    when(userPermissionService.getPermissionTokens(anyString())).thenReturn(Collections.emptyList());

    Map<String, Identity> identities = Map.of(ALICE_INT, identity(ALICE_INT, "alice.int", false, null, null),
                                              BOB_EXT, identity(BOB_EXT, "bob.ext", true, null, null),
                                              CAROL_EXT, identity(CAROL_EXT, "carol.ext", true, NOW - 3600000L, null),
                                              DAVE_EXT, identity(DAVE_EXT, "dave.ext", true, null, NOW - 7200000L),
                                              ERIN_INT, identity(ERIN_INT, "erin.int", false, NOW - 86400000L, NOW - 3600000L));
    identities.forEach((id, identity) -> when(identityManager.getIdentity(id)).thenReturn(identity));

    InitParams indexingParams = new InitParams();
    PropertiesParam indexingProperties = new PropertiesParam();
    indexingProperties.setName("constructor.params");
    indexingProperties.setProperty("index_alias", INDEX);
    indexingProperties.setProperty("index_current", INDEX);
    indexingParams.addParam(indexingProperties);
    ProfileIndexingServiceConnector indexingConnector = new ProfileIndexingServiceConnector(indexingParams,
                                                                                            identityManager,
                                                                                            mock(IdentityDAO.class),
                                                                                            mock(ConnectionDAO.class),
                                                                                            profilePropertyService,
                                                                                            mock(TranslationService.class),
                                                                                            mock(UserACL.class),
                                                                                            userPermissionService);

    send("PUT", "/" + INDEX, "{\"settings\": " + indexSettings() + ", \"mappings\": " + indexingConnector.getMapping() + "}");
    for (String id : identities.keySet()) {
      send("PUT", "/" + INDEX + "/_doc/" + id + "?refresh=wait_for", indexingConnector.create(id).toJSON());
    }

    InitParams searchParams = new InitParams();
    PropertiesParam searchProperties = new PropertiesParam();
    searchProperties.setName("constructor.params");
    searchProperties.setProperty("index", INDEX);
    searchParams.addParam(searchProperties);
    searchConnector = new ProfileSearchConnector(searchParams,
                                                 new ElasticSearchingClient(new ElasticIndexingAuditTrail()),
                                                 profilePropertyService,
                                                 userPermissionService);
  }

  @AfterClass
  public static void deleteIndex() throws Exception {
    send("DELETE", "/" + INDEX, null);
  }

  /**
   * Not enrolled and never connected, the Creation tab's filter: not invited,
   * never signed in, an External account included
   */
  @Test
  public void testNotEnrolledNeverConnectedListsExternalAccounts() {
    assertListed(filter("notEnrolled", false), ALICE_INT, BOB_EXT);
  }

  /**
   * Enrolled and never connected, the Invitations tab's filter: invited, never
   * signed in, an External account included
   */
  @Test
  public void testEnrolledNeverConnectedListsExternalAccounts() {
    assertListed(filter("enrolled", false), CAROL_EXT);
  }

  /**
   * No enrollment possible: signed in without an invitation, and no other
   * External account
   */
  @Test
  public void testNoEnrollmentPossibleAddsNoExternalAccount() {
    assertListed(filter("noEnrollmentPossible", null), DAVE_EXT);
  }

  private static void assertListed(ProfileFilter filter, String... expectedIds) {
    List<String> listed = searchConnector.search(null, filter, null, 0, 20);
    assertEquals(List.of(expectedIds).stream().sorted().toList(), listed.stream().sorted().toList());
    assertEquals(expectedIds.length, searchConnector.count(null, filter, null));
  }

  /**
   * The filter the users list builds from {@code enrollmentStatus} and
   * {@code isConnected}, with a name searched, as when the administrator types
   * one in the users table: the list then answers through Elasticsearch
   */
  private static ProfileFilter filter(String enrollmentStatus, Boolean connected) {
    ProfileFilter filter = new ProfileFilter();
    filter.setName("itpending");
    filter.setEnrollmentStatus(enrollmentStatus);
    filter.setConnected(connected);
    return filter;
  }

  private static Identity identity(String id, String username, boolean external, Long enrollmentDate, Long lastLoginTime) {
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, username);
    identity.setId(id);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "Itpending");
    profile.setProperty(Profile.LAST_NAME, username);
    profile.setProperty(Profile.EMAIL, username + "@example.com");
    if (external) {
      profile.setProperty(Profile.EXTERNAL, "true");
    }
    if (enrollmentDate != null) {
      profile.setProperty(Profile.ENROLLMENT_DATE, String.valueOf(enrollmentDate));
    }
    if (lastLoginTime != null) {
      profile.setProperty(Profile.LAST_LOGIN_TIME, String.valueOf(lastLoginTime));
    }
    identity.setProfile(profile);
    return identity;
  }

  /**
   * The platform's index settings (commons-search), its placeholders filled
   * for a single node
   */
  private static String indexSettings() throws IOException {
    try (InputStream settings = ElasticSearchingClient.class.getClassLoader()
                                                            .getResourceAsStream("es-default-index-settings.json")) {
      String json = new String(settings.readAllBytes(), StandardCharsets.UTF_8);
      return json.replace("shard.number", "1").replace("replica.number", "0").replace("max_regex.length", "1000");
    }
  }

  private static String send(String method, String path, String body) throws IOException, InterruptedException {
    HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(ES_URL + path)).header("Content-Type", "application/json");
    request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
    HttpResponse<String> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    assertTrue(method + " " + path + " answered " + response.statusCode() + ": " + response.body(),
               response.statusCode() < 300);
    return response.body();
  }
}
