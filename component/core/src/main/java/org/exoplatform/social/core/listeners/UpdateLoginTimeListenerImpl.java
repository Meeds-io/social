package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.webui.exception.MessageException;

import java.util.Calendar;

public class UpdateLoginTimeListenerImpl extends Listener<ConversationRegistry, ConversationState> {
  private static final Log LOG = ExoLogger.getLogger(UpdateLoginTimeListenerImpl.class);

  public void onEvent(Event<ConversationRegistry, ConversationState> event) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    ConversationState state = event.getData();
    User user = (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                event.getData().getIdentity().getUserId());
    Profile profile = userIdentity.getProfile();
    profile.setProperty(Profile.LAST_LOGIN_TIME, user != null ? user.getLastLoginTime() : Calendar.getInstance().getTime());
    try {
      identityManager.updateProfile(profile, true);
    } catch (MessageException e) {
      LOG.error("Error while updating the last login time for user profile {}", user.getUserName(), e);
    }
  }
}