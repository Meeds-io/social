/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.profile;

import org.exoplatform.social.common.lifecycle.AbstractLifeCycle;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent.Type;


/**
 * Lifecycle of a Profile.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ProfileLifeCycle extends AbstractLifeCycle<ProfileListener, ProfileLifeCycleEvent> {

  @Override
  protected void dispatchEvent(ProfileListener listener, ProfileLifeCycleEvent event) {
    switch(event.getType()) {
    case ABOUT_ME :
      listener.aboutMeUpdated(event);
      break;
    case AVATAR_UPDATED :
      listener.avatarUpdated(event);
      break;
    case BASIC_UPDATED:
      listener.basicInfoUpdated(event);
      break;
    case CONTACT_UPDATED:
      listener.contactSectionUpdated(event);
     break;
    case EXPERIENCE_UPDATED  :
      listener.experienceSectionUpdated(event);
      break;
    case HEADER_UPDATED:
      listener.headerSectionUpdated(event);
      break;
    case CREATED:
      listener.createProfile(event);
      break;
    case BANNER_UPDATED:
      listener.bannerUpdated(event);
      break;
    case TECHNICAL_UPDATED:
      listener.technicalUpdated(event);
      break;
    default:
      break;
    }
  }
  
  public void aboutMeUpdated(String username, Profile profile, String modifierUsername) {
    broadcast(Type.ABOUT_ME, profile, username, modifierUsername);
  }

  public void avatarUpdated(String username, Profile profile, String modifierUsername) {
    broadcast(Type.AVATAR_UPDATED, profile, username, modifierUsername);
  }

  public void bannerUpdated(String username, Profile profile, String modifierUsername) {
    broadcast(Type.BANNER_UPDATED, profile, username, modifierUsername);
  }

  public void technicalUpdated(String username, Profile profile, String modifierUsername) {
    broadcast(Type.TECHNICAL_UPDATED, profile, username, modifierUsername);
  }

  public void basicUpdated(String username, Profile profile, String modifierUsername) {
    broadcast(Type.BASIC_UPDATED, profile, username, modifierUsername);
  }

  public void contactUpdated(String username, Profile profile, String modifierUsername) {
    broadcast(Type.CONTACT_UPDATED, profile, username, modifierUsername);
  }

  public void experienceUpdated(String username, Profile profile, String modifierUsername) {
    broadcast(Type.EXPERIENCE_UPDATED, profile, username, modifierUsername);
  }

  public void headerUpdated(String username, Profile profile, String modifierUsername) {
    broadcast(Type.HEADER_UPDATED, profile, username, modifierUsername);
  }
  
  public void createProfile(Profile profile) {
    broadcast(Type.CREATED, profile, profile.getIdentity().getRemoteId(), null);
  }

  private void broadcast(Type type, Profile profile, String username, String modifierUsername) {
    broadcast(new ProfileLifeCycleEvent(type, username, profile, modifierUsername));
  }

}
