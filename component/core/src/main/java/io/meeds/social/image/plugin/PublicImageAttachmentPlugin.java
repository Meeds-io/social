/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.image.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentPlugin;

public class PublicImageAttachmentPlugin extends AttachmentPlugin {

  private final UserACL       userAcl;

  private static final String OBJECT_TYPE = "public";

  public PublicImageAttachmentPlugin(UserACL userACL) {
    this.userAcl = userACL;
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(Identity userIdentity, String entityId) throws ObjectNotFoundException {
    return true;
  }

  @Override
  public boolean hasEditPermission(Identity userIdentity, String entityId) throws ObjectNotFoundException {
    return userIdentity != null && userAcl.isAdministrator(userIdentity);
  }

  @Override
  public long getAudienceId(String objectId) throws ObjectNotFoundException {
    return 0;
  }

  @Override
  public long getSpaceId(String objectId) throws ObjectNotFoundException {
    return 0;
  }

  @Override
  public boolean canUpdateAttachmentList() {
    return false;
  }
}
