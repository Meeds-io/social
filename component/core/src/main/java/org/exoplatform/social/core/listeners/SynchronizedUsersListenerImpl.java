package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import java.util.Calendar;

public class SynchronizedUsersListenerImpl extends Listener<IDMExternalStoreImportService, User> {

  public void onEvent(Event<IDMExternalStoreImportService, User> event) throws Exception {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    User user = event.getData();
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName());
    Profile profile = userIdentity.getProfile();
    profile.setSynchronizedDate(String.valueOf(Calendar.getInstance().getTimeInMillis()));
    identityManager.updateProfile(profile, true);
  }
}
