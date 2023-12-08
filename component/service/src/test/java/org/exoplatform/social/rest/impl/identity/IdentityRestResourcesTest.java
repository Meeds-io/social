package org.exoplatform.social.rest.impl.identity;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.DummyContainerResponseWriter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    addResource(IdentityRestResourcesV1.class, null);
    createIdentity("root");
    createIdentity("john");
    createIdentity("mary");
    createIdentity("demo");
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(IdentityRestResourcesV1.class);
  }

  public void testGetIdentities() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());
  }
  
  
  public void testGetIdentityByProviderIdAndRemoteId() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/organization/root", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    DataEntity identityEntity = (DataEntity) response.getEntity();
    assertNotNull(identityEntity);
    assertEquals("root", identityEntity.get("remoteId"));
    assertEquals("organization", identityEntity.get("providerId"));
    assertNotNull(identityEntity.get("id"));
  }

  public void testGetCommonConnectionsWithIdentity() throws Exception {
    startSessionAs("root");
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    relationshipManager.inviteToConnect(johnIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, rootIdentity);
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + maryIdentity.getId() + "/commonConnections?returnSize=true", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }

  public void testGetIdentityById() throws Exception {
    startSessionAs("root");
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("If-None-Match", "\"" + eTag.getValue() + "\"");
    response =
             service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    // a relationship has added, the cache identity should be cleared
    relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, rootIdentity);
    headers = new MultivaluedMapImpl();
    headers.putSingle("If-None-Match", "\"" + eTag.getValue() + "\"");
    response =
             service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    // a relationship has removed, the cache identity should be cleared
    Relationship relationship = relationshipManager.get(rootIdentity, johnIdentity);
    relationshipManager.delete(relationship);
    headers = new MultivaluedMapImpl();
    headers.putSingle("If-None-Match", "\"" + eTag.getValue() + "\"");
    response =
             service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testGetIdentityCache() throws Exception {
    startSessionAs("root");
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    EntityTag eTagRoot = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTagRoot);

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("If-None-Match", "\"" + eTagRoot.getValue() + "\"");
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    startSessionAs("john");

    headers.putSingle("If-None-Match", "\"" + eTagRoot.getValue() + "\"");
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    EntityTag eTagJohn = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTagJohn);

    headers.putSingle("If-None-Match", "\"" + eTagJohn.getValue() + "\"");
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    startSessionAs("root");

    headers.putSingle("If-None-Match", "\"" + eTagJohn.getValue() + "\"");
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    headers.putSingle("If-None-Match", "\"" + eTagRoot.getValue() + "\"");
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());
  }

  public void testCacheWhenUserJoinsSpace() throws Exception {
    startSessionAs("root");
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("If-None-Match", "\"" + eTag.getValue() + "\"");
    response =
             service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    // a user has been added to a space, the cache identity should be cleared
    SpaceService spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);

    Space newSpace = new Space();
    newSpace.setDisplayName("space");
    newSpace.setPrettyName("space");
    newSpace.setManagers(new String[] { "root" });
    newSpace.setRegistration(Space.OPEN);
    newSpace.setVisibility(Space.PRIVATE);
    spaceService.createSpace(newSpace, "root");

    spaceService.addMember(newSpace, johnIdentity.getRemoteId());

    response =
             service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + johnIdentity.getId(), "", headers, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }
}
