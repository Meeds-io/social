package org.exoplatform.social.rest.impl.relationship;

import java.util.List;

import org.apache.commons.codec.binary.StringUtils;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.RelationshipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class RelationshipsRestResourcesTest extends AbstractResourceTest {

  private IdentityManager     identityManager;

  private RelationshipManager relationshipManager;

  private Identity            rootIdentity;

  private Identity            johnIdentity;

  private Identity            maryIdentity;

  private Identity            demoIdentity;

  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);

    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    demoIdentity = identityManager.getOrCreateUserIdentity("demo");

    assertNotNull(rootIdentity);
    assertNotNull(johnIdentity);
    assertNotNull(maryIdentity);
    assertNotNull(demoIdentity);
    addResource(RelationshipsRestResources.class, null);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(RelationshipsRestResources.class);
  }

  public void testGetRelationships() throws Exception {
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship relationship1 = relationshipManager.confirm(rootIdentity, demoIdentity);
    Relationship relationship2 = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    relationshipManager.inviteToConnect(rootIdentity, maryIdentity);
    Relationship relationship3 = relationshipManager.confirm(rootIdentity, maryIdentity);

    startSessionAs("root");
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/relationships", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    List<? extends DataEntity> relationships = collections.getEntities();
    assertEquals(3, relationships.size());
    assertTrue(relationships.stream()
                            .anyMatch(relationship -> StringUtils.equals(relationship1.getId(),
                                                                         String.valueOf(relationship.get(RestProperties.ID)))));
    assertTrue(relationships.stream()
                            .anyMatch(relationship -> StringUtils.equals(relationship2.getId(),
                                                                         String.valueOf(relationship.get(RestProperties.ID)))));
    assertTrue(relationships.stream()
                            .anyMatch(relationship -> StringUtils.equals(relationship3.getId(),
                                                                         String.valueOf(relationship.get(RestProperties.ID)))));
  }

  public void testCreateRelationship() throws Exception {
    startSessionAs("root");
    //
    String input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"CONFIRMED\"}";
    ContainerResponse response = getResponse("POST", "/" + VersionResources.VERSION_ONE + "/social/relationships/", input);
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"IGNORED\"}";
    response = getResponse("POST", "/" + VersionResources.VERSION_ONE + "/social/relationships/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"PENDING\"}";
    response = getResponse("POST", "/" + VersionResources.VERSION_ONE + "/social/relationships/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    startSessionAs("demo");
    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"PENDING\"}";
    response = getResponse("POST", "/" + VersionResources.VERSION_ONE + "/social/relationships/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"CONFIRMED\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    RelationshipEntity result = getBaseEntity(response.getEntity(), RelationshipEntity.class);
    assertEquals("/portal/rest/" + VersionResources.VERSION_ONE + "/social/users/root", result.getSender());
    assertEquals("/portal/rest/" + VersionResources.VERSION_ONE + "/social/users/demo", result.getReceiver());
    assertEquals("CONFIRMED", result.getStatus());

    Relationship relationship = relationshipManager.get(result.getId());
    assertNotNull(relationship);
    assertEquals("root", relationship.getSender().getRemoteId());
    assertEquals("demo", relationship.getReceiver().getRemoteId());
    assertEquals("CONFIRMED", relationship.getStatus().name());
  }

  public void testGetRelationshipById() throws Exception {
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship relationship = relationshipManager.confirm(rootIdentity, demoIdentity);

    startSessionAs("root");
    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationship.getId(),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    RelationshipEntity result = getBaseEntity(response.getEntity(), RelationshipEntity.class);
    assertEquals(Type.CONFIRMED.name(), result.getStatus());
  }

  public void testDeleteRelationshipById() throws Exception {
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship relationship = relationshipManager.confirm(rootIdentity, demoIdentity);

    startSessionAs("root");
    ContainerResponse response = service("DELETE",
                                         "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationship.getId(),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(204, response.getStatus());
  }

  public void testUpdateRelationshipById() throws Exception { // NOSONAR
    Relationship relationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);

    startSessionAs("root");
    String input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"CONFIRMED\"}";
    String relationshipId = relationship.getId();
    ContainerResponse response = getResponse("PUT",
                                             "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId,
                                             input);
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"IGNORED\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId, input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    relationship = relationshipManager.get(relationshipId);
    assertNotNull(relationship);
    assertEquals(Type.IGNORED, relationship.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"CONFIRMED\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId, input);
    assertNotNull(response);
    assertEquals(401, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(relationship);
    assertEquals(Type.IGNORED, relationship.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"IGNORED\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(relationship);
    assertEquals(Type.IGNORED, relationship.getStatus());
    relationshipId = relationship.getId();

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"CONFIRMED\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId, input);
    assertNotNull(response);
    assertEquals(401, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(relationship);
    assertEquals(Type.IGNORED, relationship.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"PENDING\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId, input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(relationship);
    assertEquals(Type.PENDING, relationship.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"CONFIRMED\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId, input);
    assertNotNull(response);
    assertEquals(401, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(relationship);
    assertEquals(Type.PENDING, relationship.getStatus());

    startSessionAs("demo");
    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"PENDING\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId, input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(relationship);
    assertEquals(Type.PENDING, relationship.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"CONFIRMED\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId, input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(relationship);
    assertEquals(Type.CONFIRMED, relationship.getStatus());

    input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"IGNORED\"}";
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationshipId, input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(relationship);
    assertEquals(Type.IGNORED, relationship.getStatus());
  }

}
