package org.exoplatform.social.core.space.impl;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.storage.EntityConverterUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

public class SpaceRenamedListenerImpl extends SpaceListenerPlugin {

  private static final Log LOG = ExoLogger.getLogger(SpaceRenamedListenerImpl.class);

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    renameGroupLabel(space);
    updateDefaultSpaceAvatar(space);
  }

  private void renameGroupLabel(Space space) {
    try {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group group = groupHandler.findGroupById(space.getGroupId());
      group.setLabel(space.getDisplayName());
      groupHandler.saveGroup(group, true);
    } catch (Exception e) {
      LOG.error("Error while renaming, space group Label, ignore minor change and keep the old Group Label", e);
    } finally {
      RequestLifeCycle.end();
    }
  }

  private void updateDefaultSpaceAvatar(Space space) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    FileItem spaceAvatar = identityManager.getAvatarFile(spaceIdentity);
    if (spaceAvatar != null && spaceAvatar.getFileInfo().getId() != null
        && EntityConverterUtils.DEFAULT_AVATAR.equals(spaceAvatar.getFileInfo().getName())) {
      Profile profile = spaceIdentity.getProfile();
      profile.removeProperty(Profile.AVATAR);
      profile.setAvatarUrl(null);
      profile.setAvatarLastUpdated(null);
      profile.setProperty(Profile.FULL_NAME, space.getDisplayName());
      space.setAvatarAttachment(null);
      space.setAvatarLastUpdated(System.currentTimeMillis());
      identityManager.updateProfile(profile);
      FileService fileService = CommonsUtils.getService(FileService.class);
      fileService.deleteFile(spaceAvatar.getFileInfo().getId());
    }
  }

}
