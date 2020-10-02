package org.exoplatform.social.core.space.impl;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.container.PortalContainer;

import java.util.ArrayList;
import java.util.List;

public class SpaceRenamedListenerImpl extends SpaceListenerPlugin {

  private static final Log LOG = ExoLogger.getExoLogger(SpaceRenamedListenerImpl.class);

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
