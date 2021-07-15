package org.exoplatform.social.core.listeners;

import java.util.*;
import java.util.stream.Collectors;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.CacheUserProfileFilter;

@Asynchronous
public class UpdateLoginTimeListenerImpl extends Listener<ConversationRegistry, ConversationState> {

  private static final Log LOG = ExoLogger.getLogger(UpdateLoginTimeListenerImpl.class);

  public void onEvent(Event<ConversationRegistry, ConversationState> event) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    ConversationState state = event.getData();
    User user = (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                event.getData().getIdentity().getUserId());
    Profile profile = userIdentity.getProfile();
    if (profile != null) {
      profile.setProperty(Profile.LAST_LOGIN_TIME, user != null ? user.getLastLoginTime() : Calendar.getInstance().getTimeInMillis());
      if(profile.getProperty(Profile.GROUPS_IDS) == null && user != null) {
        try {
          List<String> userGroups = organizationService.getGroupHandler().findGroupsOfUser(user.getUserName()).stream().map(g -> g.getId()).collect(Collectors.toList());
          profile.setProperty(Profile.GROUPS_IDS, String.join(",", userGroups));
        } catch (Exception e) {
          LOG.error("Can't get the groups of user '" + user.getUserName(), e);
        }
      }
      identityManager.updateProfile(profile, true);
    }
  }
}
