package org.exoplatform.social.core.jpa.search.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;

public class IDMProfileESListenerImpl extends UserProfileEventListener {

  private static final Log LOG = ExoLogger.getLogger(IDMProfileESListenerImpl.class);

  @Override
  public void postSave(UserProfile user, boolean isNew) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName());

      if (identity != null) {
        LOG.debug("Notifying indexing service for IDM user profile update id={}", identity.getId());
        indexingService.reindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
      }
    } finally {
      RequestLifeCycle.end();
    }
  }

}
