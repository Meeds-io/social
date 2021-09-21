package org.exoplatform.social.rest.impl.metadata;

import java.util.Collections;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
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

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
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
    String metadataItemEntityJson = toJsonString(metadataItemEntity);
    ContainerResponse response = getResponse("POST",
                                             getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName()),
                                             metadataItemEntityJson);
    assertEquals(200, response.getStatus());
    MetadataItemEntity entity = (MetadataItemEntity) response.getEntity();
    assertNotNull(entity);
    assertTrue(entity.getId() > 0);

    response = getResponse("DELETE",
                           getURLResource("metadatas/" + userMetadataTypePlugin.getMetadataType().getName() + "/name/"
                               + entity.getId()),
                           metadataItemEntityJson);
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
  }

}
