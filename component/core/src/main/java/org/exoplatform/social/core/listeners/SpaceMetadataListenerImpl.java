package org.exoplatform.social.core.listeners;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.metadata.MetadataService;

/**
 * A listener to delete associated metadata to a deleted Space
 */
public class SpaceMetadataListenerImpl extends SpaceListenerPlugin {

  private MetadataService metadataService;
  private IdentityManager identityManager;

  public SpaceMetadataListenerImpl(MetadataService metadataService,
                                   IdentityManager identityManager) {
    this.metadataService = metadataService;
    this.identityManager = identityManager;
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    long spaceId = Long.parseLong(event.getSpace().getId());
    this.metadataService.deleteMetadataBySpaceId(spaceId);
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    long spaceId = Long.parseLong(event.getSpace().getId());
    String username = event.getSource();
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    if (identity != null) {
      long userId = Long.parseLong(identity.getId());
      metadataService.deleteMetadataBySpaceIdAndAudienceId(spaceId, userId);
    }
  }

}
