package org.exoplatform.social.rest.impl.metadata;

import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class FavoriteRestTest extends AbstractResourceTest {

  private Identity        johnIdentity;

  private IdentityManager identityManager;

  private FavoriteRest    metadataRest;

  private FavoriteService favoriteService;

  private MetadataService metadataService;

  private MetadataDAO     metadataDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    favoriteService = getContainer().getComponentInstanceOfType(FavoriteService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);

    try {
      InitParams params = new InitParams();
      ObjectParameter parameter = new ObjectParameter();
      parameter.setName("metadataType");
      parameter.setObject(FavoriteService.METADATA_TYPE);
      params.addParameter(parameter);
      metadataService.addMetadataTypePlugin(new MetadataTypePlugin(params));
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }

    metadataRest = new FavoriteRest(favoriteService);
    registry(metadataRest);

    johnIdentity = identityManager.getOrCreateUserIdentity("john");
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(johnIdentity);
    metadataDAO.deleteAll();

    super.tearDown();
  }

  public void testCreateFavorites() throws Exception {
    String objectType = "objectType";
    String objectId = "objectId";
    String parentObjectId = "parentObjectId";
    long userIdentityId = Long.parseLong(johnIdentity.getId());

    startSessionAs(johnIdentity.getRemoteId());
    ContainerResponse response = getResponse("POST",
                                             getURLResource("favorites/" + objectType + "/" + objectId) + "?parentObjectId="
                                                 + parentObjectId,
                                             null);
    assertEquals(204, response.getStatus());

    response = getResponse("POST",
                           getURLResource("favorites/" + objectType + "/" + objectId),
                           null);
    assertEquals(409, response.getStatus());

    response = getResponse("POST",
                           getURLResource("favorites/" + objectType + "/" + objectId)
                               + "?ignoreWhenExisting=true",
                           null);
    assertEquals(204, response.getStatus());

    response = getResponse("POST",
                           getURLResource("favorites/" + objectType + "/" + objectId)
                               + "?parentObjectId=" + parentObjectId + "&ignoreWhenExisting=true",
                           null);
    assertEquals(204, response.getStatus());

    List<MetadataItem> metadataItemsByObject = metadataService.getMetadataItemsByObject(objectType, objectId);
    assertEquals(1, metadataItemsByObject.size());
    MetadataItem metadataItem = metadataItemsByObject.get(0);
    assertEquals(userIdentityId, metadataItem.getCreatorId());
    assertEquals(objectId, metadataItem.getObjectId());
    assertEquals(objectType, metadataItem.getObjectType());
    assertEquals(parentObjectId, metadataItem.getParentObjectId());
  }

  public void testDeleteFavorite() throws Exception {
    String objectType = "objectType";
    String objectId = "objectId";

    startSessionAs(johnIdentity.getRemoteId());
    ContainerResponse response = getResponse("POST",
                                             getURLResource("favorites/" + objectType + "/" + objectId),
                                             null);
    assertEquals(204, response.getStatus());

    startSessionAs(johnIdentity.getRemoteId());
    response = getResponse("DELETE",
                           getURLResource("favorites/" + objectType + "/" + objectId),
                           null);
    assertEquals(204, response.getStatus());

    response = getResponse("DELETE",
                           getURLResource("favorites/user/name"),
                           null);
    assertEquals(404, response.getStatus());

    response = getResponse("DELETE",
                           getURLResource("favorites/user/name") + "?ignoreNotExisting=true",
                           null);
    assertEquals(204, response.getStatus());
  }

}
