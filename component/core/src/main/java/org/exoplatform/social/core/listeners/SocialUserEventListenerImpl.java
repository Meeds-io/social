/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2022 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * Listens to user updating events. Created by hanh.vi@exoplatform.com Jan 17,
 * 2011
 * 
 * @since 1.2.0-GA
 */
public class SocialUserEventListenerImpl extends UserEventListener {

  private static final Log LOG = ExoLogger.getLogger(SocialUserEventListenerImpl.class);

  private ExoContainer container;

  public SocialUserEventListenerImpl(ExoContainer container) {
    this.container = container;
  }

  @Override
  @ExoTransactional
  public void preSave(User user, boolean isNew) throws Exception {
    IdentityStorage ids = container.getComponentInstanceOfType(IdentityStorage.class);
    Identity identity = ids.findIdentity(OrganizationIdentityProvider.NAME, user.getUserName());
    if (isNew && identity != null && identity.isDeleted()) {
      throw new ObjectAlreadyExistsException(identity, "Unable to create a previously deleted user : " + user.getUserName());
    }
  }

  @ExoTransactional
  @Override
  public void postSave(User user, boolean isNew) throws Exception {// NOSONAR
    //
    if (user == null) {
      throw new IllegalArgumentException("User parameter is mandatory");
    }
    IdentityManager identityManager = container.getComponentInstanceOfType(IdentityManager.class);
    Identity identity = identityManager.getOrCreateUserIdentity(user.getUserName());
    if (identity == null) {
      IdentityStorage storage = container.getComponentInstanceOfType(IdentityStorage.class);
      identity = storage.findIdentity(OrganizationIdentityProvider.NAME, user.getUserName());
    }
    if (identity == null) {
      LOG.debug("Can't find identity for user {}, ignore updating profile", user.getUserName());
      return;
    }
    //
    Profile profile = identity.getProfile();
    //
    boolean hasUpdated = false;

    if (!isNew) {
      String uFirstName = user.getFirstName();
      String uLastName = user.getLastName();
      String uDisplayName = user.getDisplayName();
      String uEmail = user.getEmail();

      //
      String pFirstName = (String) profile.getProperty(Profile.FIRST_NAME);
      String pLastName = (String) profile.getProperty(Profile.LAST_NAME);
      String pFullName = (String) profile.getProperty(Profile.FULL_NAME);
      String pEmail = (String) profile.getProperty(Profile.EMAIL);

      if ((pFirstName == null) || (!pFirstName.equals(uFirstName))) {
        profile.setProperty(Profile.FIRST_NAME, uFirstName);
        hasUpdated = true;
      }

      if ((pLastName == null) || (!pLastName.equals(uLastName))) {
        profile.setProperty(Profile.LAST_NAME, uLastName);
        hasUpdated = true;
      }

      if (uDisplayName == null || StringUtils.isEmpty(uDisplayName)) {
        uDisplayName = uFirstName + " " + uLastName;
      }

      if (!uDisplayName.equals(pFullName)) {
        profile.setProperty(Profile.FULL_NAME, uDisplayName);
      }

      if ((pEmail == null) || (!pEmail.equals(uEmail))) {
        profile.setProperty(Profile.EMAIL, uEmail);
        hasUpdated = true;
      }

    }

    if (hasUpdated) {
      profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    } else {
      profile.setListUpdateTypes(Collections.emptyList());
    }
    identityManager.updateProfile(profile);
  }

  @Override
  @ExoTransactional
  public void postDelete(final User user) throws Exception {
    IdentityStorage storage = container.getComponentInstanceOfType(IdentityStorage.class);
    Identity identity = storage.findIdentity(OrganizationIdentityProvider.NAME, user.getUserName());
    try {
      if (identity != null) {
        storage.hardDeleteIdentity(identity);
      }
    } catch (Exception e) {
      LOG.warn("Problem occurred when deleting user named " + identity.getRemoteId(), e);
    }
  }

  @Override
  @ExoTransactional
  public void postSetEnabled(User user) throws Exception {
    // Makes sure the user has been synchronized in LDAP, then to enable/disable
    // it.
    IdentityStorage storage = container.getComponentInstanceOfType(IdentityStorage.class);
    Identity identity = storage.findIdentity(OrganizationIdentityProvider.NAME, user.getUserName());
    if (identity != null) {
      IdentityManager idm = container.getComponentInstanceOfType(IdentityManager.class);
      idm.processEnabledIdentity(user.getUserName(), user.isEnabled());
    } else {
      LOG.warn(String.format("Social's Identity(%s) not found!", user.getUserName()));
    }
  }
}
