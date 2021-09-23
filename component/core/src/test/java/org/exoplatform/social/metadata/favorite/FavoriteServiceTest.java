package org.exoplatform.social.metadata.favorite;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.MetadataItem;

public class FavoriteServiceTest extends AbstractCoreTest {

  private Identity        johnIdentity;

  private IdentityManager identityManager;

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

  public void testCreateFavorite() throws ObjectAlreadyExistsException {
    String objectType = "type";
    String objectId = "1";

    List<MetadataItem> metadataItemsByObject = metadataService.getMetadataItemsByObject(objectType, objectId);
    assertTrue(CollectionUtils.isEmpty(metadataItemsByObject));

    favoriteService.createFavorite(objectType, objectId, Long.parseLong(johnIdentity.getId()));

    metadataItemsByObject = metadataService.getMetadataItemsByObject(objectType, objectId);
    assertEquals(1, metadataItemsByObject.size());

    try {
      favoriteService.createFavorite(objectType, objectId, Long.parseLong(johnIdentity.getId()));
      fail();
    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }
  }

  public void testDeleteFavorite() throws ObjectAlreadyExistsException, ObjectNotFoundException {
    String objectType = "type";
    String objectId = "1";

    List<MetadataItem> metadataItemsByObject = metadataService.getMetadataItemsByObject(objectType, objectId);
    assertTrue(CollectionUtils.isEmpty(metadataItemsByObject));

    long userIdentityId = Long.parseLong(johnIdentity.getId());
    favoriteService.createFavorite(objectType, objectId, userIdentityId);

    metadataItemsByObject = metadataService.getMetadataItemsByObject(objectType, objectId);
    assertEquals(1, metadataItemsByObject.size());

    favoriteService.deleteFavorite(objectType, objectId, userIdentityId);

    metadataItemsByObject = metadataService.getMetadataItemsByObject(objectType, objectId);
    assertEquals(0, metadataItemsByObject.size());

    try {
      favoriteService.deleteFavorite(objectType, objectId, userIdentityId);
      fail();
    } catch (ObjectNotFoundException e) {
      // Expected
    }
  }

}
