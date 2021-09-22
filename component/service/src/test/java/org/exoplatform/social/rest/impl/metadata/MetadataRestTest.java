package org.exoplatform.social.rest.impl.metadata;

import java.util.Collections;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.*;
import org.exoplatform.social.rest.entity.MetadataItemEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class MetadataRestTest extends AbstractResourceTest {

  private Identity               rootIdentity;

  private Identity               johnIdentity;

  private Identity               maryIdentity;

  private Identity               demoIdentity;

  private Identity               ghostIdentity;

  private Identity               raulIdentity;

  private Identity               jameIdentity;

  private Identity               paulIdentity;

  private IdentityManager        identityManager;

  private MetadataService        metadataService;

  private UserOnlyMetadataPlugin userMetadataTypePlugin = new UserOnlyMetadataPlugin();

  private MetadataRest           metadataRest;

  private MetadataDAO            metadataDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    try {
      metadataService.addMetadataTypePlugin(userMetadataTypePlugin);
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }

    metadataRest = new MetadataRest(metadataService);
    registry(metadataRest);

    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    demoIdentity = identityManager.getOrCreateUserIdentity("demo");
    ghostIdentity = identityManager.getOrCreateUserIdentity("ghost");
    raulIdentity = identityManager.getOrCreateUserIdentity("raul");
    jameIdentity = identityManager.getOrCreateUserIdentity("jame");
    paulIdentity = identityManager.getOrCreateUserIdentity("paul");
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(jameIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);

    metadataDAO.deleteAll();
    super.tearDown();
  }

  public void testCreateMetadataItem() throws Exception {
    MetadataItemEntity metadataItemEntity = new MetadataItemEntity(0,
                                                                   "name",
                                                                   "objectType",
                                                                   "objectId",
                                                                   "parentObjectId",
                                                                   Long.parseLong(johnIdentity.getId()),
                                                                   Long.parseLong(johnIdentity.getId()),
                                                                   Collections.singletonMap("testMetadataItem1Key",
                                                                                            "testMetadataItem1Value"));

    startSessionAs(johnIdentity.getRemoteId());
    String metadataItemEntityJson = toJsonString(metadataItemEntity);
    ContainerResponse response = getResponse("POST",
                                             getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName()),
                                             metadataItemEntityJson);
    assertEquals(200, response.getStatus());
    MetadataItemEntity entity = (MetadataItemEntity) response.getEntity();
    assertNotNull(entity);
    assertTrue(entity.getId() > 0);
    assertEquals(metadataItemEntity.getObjectType(), entity.getObjectType());
    assertEquals(metadataItemEntity.getObjectId(), entity.getObjectId());
    assertEquals(metadataItemEntity.getParentObjectId(), entity.getParentObjectId());
    assertEquals(metadataItemEntity.getName(), entity.getName());
    assertEquals(metadataItemEntity.getCreatorId(), entity.getCreatorId());
    assertEquals(metadataItemEntity.getAudienceId(), entity.getAudienceId());
  }

  public void testCreateDuplicatedMetadataItem() throws Exception {
    MetadataItemEntity metadataItemEntity = new MetadataItemEntity(0,
                                                                   "name",
                                                                   "objectType",
                                                                   "objectId",
                                                                   "parentObjectId",
                                                                   Long.parseLong(johnIdentity.getId()),
                                                                   Long.parseLong(johnIdentity.getId()),
                                                                   Collections.singletonMap("testMetadataItem1Key",
                                                                                            "testMetadataItem1Value"));

    String metadataItemEntityJson = toJsonString(metadataItemEntity);

    startSessionAs(rootIdentity.getRemoteId());
    ContainerResponse response = getResponse("POST",
                                             getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName())
                                                 + "?ignoreWhenExisting=true",
                                             metadataItemEntityJson);
    assertEquals(401, response.getStatus());

    startSessionAs(johnIdentity.getRemoteId());
    response = getResponse("POST",
                           getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName()),
                           metadataItemEntityJson);
    assertEquals(200, response.getStatus());
    MetadataItemEntity entity = (MetadataItemEntity) response.getEntity();
    assertNotNull(entity);
    assertTrue(entity.getId() > 0);
    assertEquals(metadataItemEntity.getObjectType(), entity.getObjectType());
    assertEquals(metadataItemEntity.getObjectId(), entity.getObjectId());
    assertEquals(metadataItemEntity.getParentObjectId(), entity.getParentObjectId());
    assertEquals(metadataItemEntity.getName(), entity.getName());
    assertEquals(metadataItemEntity.getCreatorId(), entity.getCreatorId());
    assertEquals(metadataItemEntity.getAudienceId(), entity.getAudienceId());

    response = getResponse("POST",
                           getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName()),
                           metadataItemEntityJson);
    assertEquals(409, response.getStatus());

    startSessionAs(rootIdentity.getRemoteId());
    response = getResponse("POST",
                           getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName())
                               + "?ignoreWhenExisting=true",
                           metadataItemEntityJson);
    assertEquals(401, response.getStatus());

    startSessionAs(johnIdentity.getRemoteId());
    response = getResponse("POST",
                           getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName())
                               + "?ignoreWhenExisting=true",
                           metadataItemEntityJson);
    assertEquals(200, response.getStatus());
    MetadataItemEntity existingEntity = (MetadataItemEntity) response.getEntity();
    assertNotNull(existingEntity);
    assertEquals(entity.getId(), existingEntity.getId());
    assertEquals(metadataItemEntity.getObjectType(), existingEntity.getObjectType());
    assertEquals(metadataItemEntity.getObjectId(), existingEntity.getObjectId());
    assertEquals(metadataItemEntity.getParentObjectId(), existingEntity.getParentObjectId());
    assertEquals(metadataItemEntity.getName(), existingEntity.getName());
    assertEquals(metadataItemEntity.getCreatorId(), existingEntity.getCreatorId());
    assertEquals(metadataItemEntity.getAudienceId(), existingEntity.getAudienceId());
  }

  public void testDeleteMetadataItem() throws Exception {
    MetadataItemEntity metadataItemEntity = new MetadataItemEntity(0,
                                                                   "name",
                                                                   "objectType",
                                                                   "objectId",
                                                                   "parentObjectId",
                                                                   Long.parseLong(johnIdentity.getId()),
                                                                   Long.parseLong(johnIdentity.getId()),
                                                                   Collections.singletonMap("testMetadataItem1Key",
                                                                                            "testMetadataItem1Value"));

    startSessionAs(johnIdentity.getRemoteId());
    ContainerResponse response = getResponse("POST",
                                             getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName()),
                                             toJsonString(metadataItemEntity));
    assertEquals(200, response.getStatus());
    MetadataItemEntity entity = (MetadataItemEntity) response.getEntity();
    assertNotNull(entity);
    assertTrue(entity.getId() > 0);

    startSessionAs(rootIdentity.getRemoteId());
    response = getResponse("DELETE",
                           getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName() + "/name/"
                               + entity.getId()),
                           null);
    assertEquals(401, response.getStatus());

    startSessionAs(johnIdentity.getRemoteId());
    response = getResponse("DELETE",
                           getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName() + "/name/"
                               + entity.getId()),
                           null);
    assertEquals(200, response.getStatus());
    entity = (MetadataItemEntity) response.getEntity();
    assertNotNull(entity);
    assertTrue(entity.getId() > 0);
    assertEquals(metadataItemEntity.getObjectType(), entity.getObjectType());
    assertEquals(metadataItemEntity.getObjectId(), entity.getObjectId());
    assertEquals(metadataItemEntity.getParentObjectId(), entity.getParentObjectId());
    assertEquals(metadataItemEntity.getName(), entity.getName());
    assertEquals(metadataItemEntity.getCreatorId(), entity.getCreatorId());
    assertEquals(metadataItemEntity.getAudienceId(), entity.getAudienceId());
  }

  public void testDeleteNotExistingMetadataItem() throws Exception {
    startSessionAs(johnIdentity.getRemoteId());
    ContainerResponse response = getResponse("DELETE",
                                             getURLResource("metadatas/user/name/522"),
                                             null);
    assertEquals(404, response.getStatus());

    response = getResponse("DELETE",
                           getURLResource("metadatas/user/name/522") + "?ignoreNotExisting=true",
                           null);
    assertEquals(204, response.getStatus());
  }

  public final class UserOnlyMetadataPlugin extends MetadataTypePlugin {
    public UserOnlyMetadataPlugin() {
      super(new MetadataType(1, "user"));
    }

    @Override
    public boolean canCreateMetadata(Metadata metadata, long userIdentityId) {
      return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canUpdateMetadata(Metadata metadata, long userIdentityId) {
      return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canDeleteMetadata(Metadata metadata, long userIdentityId) {
      return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canCreateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canUpdateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canDeleteMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
    }

    @Override
    public boolean allowMultipleItemsPerObject() {
      return false;
    }
  }

}
