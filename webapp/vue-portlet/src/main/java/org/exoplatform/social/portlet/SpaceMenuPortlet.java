package org.exoplatform.social.portlet;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.portlet.*;

import org.apache.commons.codec.binary.StringUtils;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.State;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.page.*;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.webui.util.NavigationURLUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class SpaceMenuPortlet extends GenericPortlet {

  private static final Log        LOG               = ExoLogger.getLogger(SpaceMenuPortlet.class);

  private static final String     VIEW_JSP          = "/WEB-INF/jsp/spaceMenu.jsp";

  private static final String     PORTAL_PAGE_TITLE = "portal:requestTitle";

  private static final String     SPACE_SETTINGS    = "settings";

  private ResourceBundleService   resourceBundleService;

  private SpaceService            spaceService;

  private PageService             pageService;

  private UserPortalConfigService userPortalConfigService;

  private String[]                sharedResources;

  @Override
  public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    Space space = SpaceUtils.getSpaceByContext();
    if (space == null) {
      return;
    }

    Locale locale = request.getLocale();
    String currentUser = request.getRemoteUser();
    try {
      List<UserNode> navigations = getSpaceNavigations(space,
                                                       locale,
                                                       currentUser);
      request.setAttribute("navigations", navigations);

      Map<String, String> navigationMap = navigations.stream()
                                                     .collect(Collectors.toMap(UserNode::getId,
                                                                               userNode -> getUri(userNode)));
      request.setAttribute("navigationsUri", navigationMap);

      UserNode selectedUserNode = setPageTitle(navigations, request, space, locale);
      request.setAttribute("selectedUri", navigationMap.get(selectedUserNode.getId()));

      PortletRequestDispatcher requestDispatcher = getPortletContext().getRequestDispatcher(VIEW_JSP);
      requestDispatcher.include(request, response);
    } catch (Exception e) {
      LOG.warn("Error displaying space menu", e);
    }
  }

  private UserNode setPageTitle(List<UserNode> navigations, RenderRequest request, Space space, Locale locale) throws Exception {
    UserNode selectedUserNode = Util.getUIPortal().getSelectedUserNode();
    String selectedUserNodeId = selectedUserNode.getId();
    String selectedUserNodeLabel = navigations.stream()
                                              .filter(node -> StringUtils.equals(node.getId(), selectedUserNodeId))
                                              .map(UserNode::getResolvedLabel)
                                              .findFirst()
                                              .orElse(null);
    if (selectedUserNodeLabel == null) {
      ResourceBundle resourceBundle = getSharedResourceBundle(locale);
      selectedUserNodeLabel = getResolvedAppLabel(selectedUserNode, resourceBundle, locale);
    }
    request.setAttribute(PORTAL_PAGE_TITLE, space.getDisplayName() + " - " + selectedUserNodeLabel);
    return selectedUserNode;
  }

  private List<UserNode> getSpaceNavigations(Space space, Locale locale, String currentuser) {
    List<UserNode> navigations = new ArrayList<>();
    try {
      UserNodeFilterConfig.Builder filterConfigBuilder = UserNodeFilterConfig.builder();
      filterConfigBuilder.withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      filterConfigBuilder.withTemporalCheck();
      UserNodeFilterConfig filter = filterConfigBuilder.build();
      UserNode parentNavigation = SpaceUtils.getSpaceUserNode(space, filter);
      UserNode settingNavigation = parentNavigation.getChild(SPACE_SETTINGS);
      if (!hasSettingPermission(space, currentuser) && (settingNavigation != null)) {
        parentNavigation.removeChild(settingNavigation.getName());
      }
      navigations.add(parentNavigation);
      navigations.addAll(parentNavigation.getChildren());
      filterUnreachablePages(navigations);
      computeNavigationLabels(navigations, locale);
      computeNavigationIcons(navigations);
    } catch (Exception e) {
      LOG.warn("Get UserNode of Space failed.");
    }
    return navigations;
  }

  private String getUri(UserNode userNode) {
    String uri = userNode.getURI();
    if (!uri.startsWith("/" + PortalContainer.getCurrentPortalContainerName())) {
      uri = NavigationURLUtils.getURL(userNode);
    }
    return uri;
  }

  private void computeNavigationLabels(List<UserNode> navigations, Locale locale) {
    ResourceBundle resourceBundle = getSharedResourceBundle(locale);
    for (UserNode node : navigations) {
      if (node == null) {
        continue;
      }
      String nodeTitle = getResolvedAppLabel(node, resourceBundle, locale);
      node.setResolvedLabel(nodeTitle);
    }
  }

  private void computeNavigationIcons(List<UserNode> navigations) {
    for (UserNode node : navigations) {
      if (node == null) {
        continue;
      }
      String nodeIcon = getIconClass(node);
      node.setIcon(nodeIcon);
    }
  }

  private boolean hasSettingPermission(Space space, String username) {
    return getSpaceService().hasSettingPermission(space, username);
  }

  private String getIconClass(UserNode node) {
    if (node == null) {
      return null;
    }
    if (node.getParent() == null || StringUtils.equals(node.getParent().getName(), "default")
        || node.getParent().getParent() == null) {
      return "uiIconAppSpaceHomePage uiIconDefaultApp";
    }
    String appName = node.getPageRef().getName();
    return "uiIconApp" + node.getName() + " uiIconApp" + appName + " uiIconDefaultApp";
  }

  private String getResolvedAppLabel(UserNode userNode, ResourceBundle resourceBundle, Locale locale) {
    if (userNode == null) {
      return null;
    }
    if (userNode.getParent() == null || StringUtils.equals(userNode.getParent().getName(), "default")
        || userNode.getParent().getParent() == null) {
      return resourceBundle.getString("UISpaceMenu.label.SpaceHomePage");
    }
    String id = userNode.getId();
    String nodeLabel = userNode.getLabel();
    if (nodeLabel != null) {
      return ExpressionUtil.getExpressionValue(resourceBundle, nodeLabel);
    } else if (id != null) {
      DescriptionService descriptionService = getUserPortalConfigService().getDescriptionService();
      State description = descriptionService.resolveDescription(id, locale);
      if (description != null) {
        return description.getName();
      }
    }
    String labelKey = userNode.getPageRef().getName() + ".label.name";
    if (resourceBundle.containsKey(labelKey)) {
      return resourceBundle.getString(labelKey);
    }
    return userNode.getName();
  }

  private void filterUnreachablePages(List<UserNode> nodes) {
    List<UserNode> nonePageNodes = new ArrayList<>();
    UserACL userACL = SpaceUtils.getUserACL();
    for (UserNode node : nodes) {
      PageKey currentPage = node.getPageRef();
      if (currentPage == null) {
        nonePageNodes.add(node);
      } else {
        PageContext currentPageContext = getPageService().loadPage(currentPage);
        if (currentPageContext == null || !userACL.hasPermission(currentPageContext)) {
          nonePageNodes.add(node);
        }
      }
    }

    nodes.removeAll(nonePageNodes);
  }

  private UserPortalConfigService getUserPortalConfigService() {
    if (userPortalConfigService == null) {
      userPortalConfigService = ExoContainerContext.getService(UserPortalConfigService.class);
    }
    return userPortalConfigService;
  }

  private PageService getPageService() {
    if (pageService == null) {
      pageService = ExoContainerContext.getService(PageService.class);
    }
    return pageService;
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }

  private ResourceBundle getSharedResourceBundle(Locale locale) {
    return getResourceBundleService().getResourceBundle(getSharedResources(), locale);
  }

  private ResourceBundleService getResourceBundleService() {
    if (resourceBundleService == null) {
      resourceBundleService = ExoContainerContext.getService(ResourceBundleService.class);
    }
    return resourceBundleService;
  }

  private String[] getSharedResources() {
    if (this.sharedResources == null) {
      this.sharedResources = getResourceBundleService().getSharedResourceBundleNames();
    }
    return sharedResources;
  }
}
