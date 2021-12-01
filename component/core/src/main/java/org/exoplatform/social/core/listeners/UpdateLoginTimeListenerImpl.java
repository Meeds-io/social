package org.exoplatform.social.core.listeners;

import java.util.Calendar;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.CacheUserProfileFilter;

@Asynchronous
public class UpdateLoginTimeListenerImpl extends Listener<ConversationRegistry, ConversationState> {
  public void onEvent(Event<ConversationRegistry, ConversationState> event) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    ConversationState state = event.getData();
    User user = (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                event.getData().getIdentity().getUserId());
    Profile profile = userIdentity.getProfile();
    if (profile != null) {
      profile.setProperty(Profile.LAST_LOGIN_TIME, user != null ? user.getLastLoginTime() : Calendar.getInstance().getTimeInMillis());
      identityManager.updateProfile(profile, false);
      IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, userIdentity.getId());
    }
  }
}
