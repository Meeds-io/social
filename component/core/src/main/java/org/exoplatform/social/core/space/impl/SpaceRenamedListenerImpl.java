package org.exoplatform.social.core.space.impl;

import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

public class SpaceRenamedListenerImpl extends SpaceListenerPlugin {

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
  }

  @Override
  public void spaceRegistrationEdited(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceBannerEdited(SpaceLifeCycleEvent event) {
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {

  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {

  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {

  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {

  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {

  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {

  }

  @Override
  public void left(SpaceLifeCycleEvent event) {

  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    SpaceUtils.renamePageNode(space);
    SpaceUtils.renameGroupLabel(space);
  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {

  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {

  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {

  }

}
