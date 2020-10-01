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
    renameNavigationAndGroup(space);
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

  /**
   * Rename navigation and group
   *
   * @param space
   * @return
   */
  public static void renameNavigationAndGroup(Space space) {
    try {

      String oldSpaceName = space.getGroupId().split("/spaces/")[1];
      if (!oldSpaceName.equals(space.getDisplayName())) {
        String cleanedString = SpaceUtils.cleanString(space.getDisplayName());
        UserNode node = renamePageNode(cleanedString, space);
        OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
        GroupHandler groupHandler = organizationService.getGroupHandler();
        Group group = groupHandler.findGroupById(space.getGroupId());
        group.setLabel(space.getDisplayName());
        groupHandler.saveGroup(group, true);
        if (node != null) {
          PortalRequestContext prContext = Util.getPortalRequestContext();
          prContext.createURL(NodeURL.TYPE).setNode(node);
          space.setUrl(getSpaceURL(node));
        }
      }
    } catch (Exception e) {
      LOG.warn(e.getMessage(), e);
    } finally {
      RequestLifeCycle.end();
    }
  }

  /**
   * Rename page node.
   *
   * @param newNodeLabel
   * @param space
   * @return
   */
  public static UserNode renamePageNode(String newNodeLabel, Space space) {

    try {

      DataStorage dataService = CommonsUtils.getService(DataStorage.class);
      UserNode renamedNode = SpaceUtils.getSpaceUserNode(space);
      UserNode parentNode = renamedNode.getParent();
      String newNodeName = SpaceUtils.cleanString(newNodeLabel);
      //
      renamedNode.setLabel(newNodeLabel);
      renamedNode.setName(newNodeName);

      Page page = dataService.getPage(renamedNode.getPageRef().format());
      if (page != null) {
        page.setTitle(newNodeLabel);
        dataService.save(page);
      }

      SpaceUtils.getUserPortal().saveNode(parentNode, null);

      space.setUrl(newNodeName);
      SpaceUtils.changeSpaceUrlPreference(renamedNode, space, newNodeLabel);
      SpaceUtils.changeAppPageTitle(renamedNode, newNodeLabel);

      List<UserNode> userNodes = new ArrayList<>(renamedNode.getChildren());
      for (UserNode childNode : userNodes) {
        SpaceUtils.changeSpaceUrlPreference(childNode, space, newNodeLabel);
        SpaceUtils.changeAppPageTitle(childNode, newNodeLabel);
      }
      return renamedNode;
    } catch (Exception e) {
      LOG.warn(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Get the space url.
   *
   * @param node
   * @return
   */
  public static String getSpaceURL(UserNode node) {
    RequestContext ctx = RequestContext.getCurrentInstance();
    NodeURL nodeURL = ctx.createURL(NodeURL.TYPE);
    return nodeURL.setNode(node).toString();
  }

}
