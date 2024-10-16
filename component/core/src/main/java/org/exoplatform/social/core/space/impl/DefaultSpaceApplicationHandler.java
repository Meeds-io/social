/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.space.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.module.ModuleRegistry;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.NodeState.Builder;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.portal.mop.storage.PageStorage;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.Application;
import org.exoplatform.social.core.space.SpaceApplication;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceApplicationHandler;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Default implementation for working with space applications.
 *
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since OCt 17, 2008
 */

public class DefaultSpaceApplicationHandler implements SpaceApplicationHandler {
  protected static final Log     LOG                          = ExoLogger.getLogger(DefaultSpaceApplicationHandler.class);

  public static final String     NAME                         = "classic";

  protected static final String  TEMPLATE_NAME_PARAM          = "templateName";

  public static final String     SPACE_TEMPLATE_PAGE_ID       = "portal::classic::spacetemplate";

  public static final String     APPLICATION_CONTAINER        = "Application";

  /**
   * The {groupId} preference value pattern
   *
   * @since 1.2.0-GA
   */
  protected static final String  GROUP_ID_PREFERENCE          = "{groupId}";

  /**
   * The {modifiedGroupId} preference value pattern
   *
   * @since 1.2.0-GA
   */
  protected static final String  MODIFIED_GROUP_ID_PREFERENCE = "{modifiedGroupId}";

  /**
   * The {pageName} preference value pattern
   *
   * @since 1.2.0-GA
   */
  protected static final String  PAGE_NAME_PREFERENCE         = "{pageName}";

  /**
   * The {pageUrl} preference value pattern
   *
   * @since 1.2.0-GA
   */
  protected static final String  PAGE_URL_PREFERENCE          = "{pageUrl}";

  protected LayoutService        layoutService;

  protected NavigationService    navigationService;

  protected PageStorage          pageStorage;

  protected SpaceService         spaceService;

  protected ModuleRegistry       moduleRegistry;

  protected SpaceTemplateService spaceTemplateService;

  protected String               templateName;

  public DefaultSpaceApplicationHandler(InitParams params,
                                        LayoutService layoutService,
                                        NavigationService navigationService,
                                        PageStorage pageService,
                                        SpaceTemplateService spaceTemplateService) {
    this.layoutService = layoutService;
    this.navigationService = navigationService;
    this.pageStorage = pageService;
    this.spaceTemplateService = spaceTemplateService;
    if (params == null) {
      templateName = NAME;
    } else {
      templateName = params.getValueParam(TEMPLATE_NAME_PARAM).getValue();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void initApps(Space space, SpaceTemplate spaceTemplate) throws SpaceException {
    try {
      NavigationContext navContext = SpaceUtils.createGroupNavigation(space.getGroupId());
      NodeContext<NodeContext<?>> parentNodeCtx = navigationService.loadNode(NodeModel.SELF_MODEL,
                                                                             navContext,
                                                                             Scope.CHILDREN,
                                                                             null);

      //
      SpaceApplication homeApplication = spaceTemplate.getSpaceHomeApplication();
      if (homeApplication == null) {
        throw new IllegalStateException(String.format("Could not find space home application for template %s. Could not init space apps",
                                                      spaceTemplate.getName()));
      }
      NodeContext<NodeContext<?>> homeNodeCtx = createPageNodeFromApplication(navContext,
                                                                              parentNodeCtx,
                                                                              space,
                                                                              homeApplication,
                                                                              null,
                                                                              true);
      List<SpaceApplication> spaceApplications = spaceTemplate.getSpaceApplicationList();
      if (spaceApplications != null) {
        for (SpaceApplication spaceApplication : spaceApplications) {
          try {
            createPageNodeFromApplication(navContext, homeNodeCtx, space, spaceApplication, null, false);
            getSpaceService().installApplication(space, spaceApplication.getPortletName());
          } catch (Exception e) {
            LOG.warn("Can't install application {} for space {}, ignore adding dedicated page",
                     spaceApplication.getPortletName(),
                     space.getDisplayName());
          }
        }
      }
      navigationService.saveNode(parentNodeCtx, null);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_INIT_APP, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deInitApp(Space space) throws SpaceException {
    try {
      String groupId = space.getGroupId();
      NavigationContext spaceNavCtx = SpaceUtils.getGroupNavigationContext(groupId);
      // return in case group navigation was removed by portal SOC-548
      if (spaceNavCtx == null) {
        return;
      }
      NodeContext<NodeContext<?>> homeNodeCtx = SpaceUtils.getHomeNodeWithChildren(spaceNavCtx, groupId);

      for (NodeContext<?> child : homeNodeCtx.getNodes()) {
        @SuppressWarnings("unchecked")
        NodeContext<NodeContext<?>> childNode = (NodeContext<NodeContext<?>>) child;
        Page page = layoutService.getPage(childNode.getState().getPageRef().format());
        layoutService.remove(page);
      }

      SpaceUtils.removeGroupNavigation(groupId);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_DEINIT_APP, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void activateApplication(Space space, String appId, String appName) {
    NavigationService navService = ExoContainerContext.getService(NavigationService.class);
    NavigationContext navContext;
    NodeContext<NodeContext<?>> homeNodeCtx = null;

    try {
      navContext = SpaceUtils.getGroupNavigationContext(space.getGroupId());
      homeNodeCtx = SpaceUtils.getHomeNodeWithChildren(navContext, null);
      homeNodeCtx = homeNodeCtx.get(0);
    } catch (Exception e) {
      LOG.warn("space navigation not found.", e);
      return;
    }
    SpaceApplication spaceApplication = null;

    for (SpaceTemplate spaceTemplate : spaceTemplateService.getSpaceTemplates()) {
      for (SpaceApplication application : spaceTemplate.getSpaceApplicationList()) {
        if (appId.equals(application.getPortletName()) && !SpaceUtils.isInstalledApp(space, appId)) {
          spaceApplication = application;
          break;
        }
      }
    }

    if (spaceApplication == null) {
      spaceApplication = new SpaceApplication();
      spaceApplication.setPortletName(appId);
    }
    createPageNodeFromApplication(navContext, homeNodeCtx, space, spaceApplication, appName, false);
    navService.saveNode(homeNodeCtx, null);
  }

  /**
   * {@inheritDoc}
   */
  public void deactiveApplication(Space space, String appId) throws SpaceException {
    deactivateApplicationClassic(space, appId);
  }

  /**
   * {@inheritDoc}
   */
  public void installApplication(Space space, String appId) throws SpaceException {

  }

  /**
   * {@inheritDoc}
   */
  public void removeApplication(Space space, String appId, String appName) throws SpaceException {
    removeApplicationClassic(space, appId, appName);
  }

  /**
   * {@inheritDoc}
   */
  public void removeApplications(Space space) throws SpaceException {
    try {
      String[] apps = space.getApp().split(",");
      String[] appPart = null;
      for (int i = 0; i < apps.length; i++) {
        appPart = apps[i].split(":");
        removeApplication(space, appPart[0], appPart[1]);
      }
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_APPLICATIONS, e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void moveApplication(Space space, String appId, int transition) throws Exception {
    String apps = space.getApp();
    if (transition == 0 || apps == null || !StringUtils.contains(apps, appId)) {
      return;
    }
    List<String> appPartsList = new ArrayList<>(Arrays.asList(apps.split(",")));
    String appPartToChange = appPartsList.stream()
                                         .filter(appPart -> appPart != null && StringUtils.startsWith(appPart, appId + ":"))
                                         .findFirst()
                                         .orElse(null);
    if (StringUtils.isBlank(appPartToChange)) {
      return;
    }
    String[] appParts = appPartToChange.split(":");
    NodeContext<NodeContext<?>> spaceUserNode = getSpaceUserNode(space).get(0);
    NodeContext<NodeContext<?>> toMoveNode = null;
    Iterator<NodeContext<?>> nodes = spaceUserNode.getNodes().iterator();
    int nodeIndex = 0;
    while (toMoveNode == null && nodes.hasNext()) {
      NodeContext<NodeContext<?>> userNode = (NodeContext<NodeContext<?>>) nodes.next();
      String appName = appParts[1];
      if (StringUtils.equals(userNode.getName(), appId)
          || StringUtils.equals(userNode.getName(), appName)
          || (userNode.getState().getPageRef() != null
              && (StringUtils.equals(userNode.getState().getPageRef().getName(), appId)
                  || StringUtils.equals(userNode.getState().getPageRef().getName(), appName)))) {
        toMoveNode = userNode;
        break;
      }
      nodeIndex++;
    }
    if (toMoveNode != null) {
      if (transition < 0) {
        if (nodeIndex > 0) {
          NodeContext<NodeContext<?>> previousNode = spaceUserNode.get(nodeIndex - 1);
          spaceUserNode.removeNode(previousNode.getName());
          spaceUserNode.add(nodeIndex, previousNode);
        }
      } else if (transition > 0) {
        int newIndex = nodeIndex + 1;
        if (newIndex < spaceUserNode.getNodeCount()) {
          spaceUserNode.removeNode(toMoveNode.getName());
          spaceUserNode.add(newIndex, toMoveNode);
        }
      }
    }

    ExoContainerContext.getService(NavigationService.class).saveNode(spaceUserNode, null);
  }

  @Override
  public void restoreApplicationLayout(Space space, String appId) throws Exception {
    NavigationService navService = ExoContainerContext.getService(NavigationService.class);
    NavigationContext navContext = SpaceUtils.getGroupNavigationContext(space.getGroupId());

    NodeContext<NodeContext<?>> parentNodeCtx = navService.loadNode(NodeModel.SELF_MODEL, navContext, Scope.ALL, null);

    String spaceTemplateName = space.getTemplate();
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(spaceTemplateName);
    if (spaceTemplate == null) {
      throw new IllegalStateException("Space template with name " + spaceTemplateName + " wasn't found");
    }

    if (StringUtils.equalsIgnoreCase(appId, "home")) {
      SpaceApplication homeApplication = spaceTemplate.getSpaceHomeApplication();
      if (homeApplication == null) {
        throw new IllegalStateException("Could not find space home application for template " + spaceTemplate.getName() +
            ". Could not init space apps");
      }

      String originalHomePageName = parentNodeCtx.get(0).getState().getPageRef().getName();
      createPageNodeFromApplication(navContext, parentNodeCtx, space, homeApplication, originalHomePageName, true);
    } else {
      SpaceApplication spaceApplication = getSpaceApplication(spaceTemplate, appId);
      NodeContext<NodeContext<?>> homeNodeCtx = parentNodeCtx.get(0);
      createPageNodeFromApplication(navContext, homeNodeCtx, space, spaceApplication, null, false);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return templateName;
  }

  @Override
  public void setName(String s) {

  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void setDescription(String s) {

  }

  /**
   * Deactivates an application in a space
   *
   * @param space
   * @param appId
   */
  protected void deactivateApplicationClassic(Space space, String appId) {

  }

  /**
   * Removes an classic-type application from a space
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  protected void removeApplicationClassic(Space space, String appId, String appName) throws SpaceException {
    try {

      UserNode spaceUserNode = SpaceUtils.getSpaceUserNode(space);
      UserNode removedNode = spaceUserNode.getChild(appName);
      if (removedNode == null) {
        // Try to get the node by the pageRef name and the appId
        for (UserNode node : spaceUserNode.getChildren()) {
          if (appId.equals(node.getPageRef().getName())) {
            removedNode = spaceUserNode.getChild(node.getName());
            break;
          }
        }
      }
      if (removedNode == null) {
        // In case of cannot find the removed node, try one more time
        String spaceTemplateName = space.getTemplate();
        SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(spaceTemplateName);
        if (spaceTemplate == null) {
          throw new IllegalStateException("Space template with name " + spaceTemplateName + " wasn't found");
        }
        List<SpaceApplication> spaceApplications = spaceTemplate.getSpaceApplicationList();
        for (SpaceApplication spaceApplication : spaceApplications) {
          if (appId.equals(spaceApplication.getPortletName())) {
            removedNode = spaceUserNode.getChild(spaceApplication.getUri());
          }
        }
      }

      if (removedNode != null) {
        spaceUserNode.removeChild(removedNode.getName());
      } else {
        return;
      }

      // remove page
      if (removedNode != null) {
        PageKey pageRef = removedNode.getPageRef();
        if (pageRef.format() != null && pageRef.format().length() > 0) {
          // only clear UI caching when it's in UI context
          if (WebuiRequestContext.getCurrentInstance() != null) {
            UIPortal uiPortal = Util.getUIPortal();
            // Remove from cache
            uiPortal.setUIPage(pageRef.format(), null);
          }
          layoutService.remove(pageRef);
        }
      }

      SpaceUtils.getUserPortal().saveNode(spaceUserNode, null);

    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_APPLICATION, e);
    }

  }

  /**
   * Creates page node from application. - Creates Application instance from
   * appId. <br>
   * - Creates Page instance and set the newly-created application for that
   * page; adds application to container. <br>
   * - Creates PageNode instance and returns that pageNode.
   *
   * @param space
   * @param spaceApplication
   * @param isRoot
   * @return
   * @since 1.2.0-GA
   */
  protected NodeContext<NodeContext<?>> createPageNodeFromApplication(NavigationContext navContext,
                                                                      NodeContext<NodeContext<?>> nodeCtx,
                                                                      Space space,
                                                                      SpaceApplication spaceApplication,
                                                                      String appName,
                                                                      boolean isRoot) {
    String appId = spaceApplication.getPortletName();
    String potletFullId = spaceApplication.getPortletApp() + "/" + spaceApplication.getPortletName();
    if (!isPortletActive(potletFullId)) {
      return null;
    }

    Application app;
    try {
      app = getApplication(appId);
    } catch (Exception e) {
      if (StringUtils.isBlank(spaceApplication.getPortletApp())) {
        throw new IllegalStateException("An error occurred while getting application " + appId + " from registry." +
            " In fact, the application isn't configured with its application name (WAR webapp name)." +
            " This may be the cause of the problem", e);
      } else {
        throw new IllegalStateException("An error occurred while getting application " + appId + " from registry.", e);
      }
    }
    String contentId = app.getContentId();
    String appInstanceId = PortalConfig.GROUP_TYPE + "#" + space.getGroupId() + ":/" + contentId + "/" +
        app.getApplicationName() + System.currentTimeMillis();
    org.exoplatform.portal.config.model.Application<Portlet> portletApplication = createPortletApplication(appInstanceId,
                                                                                                           space,
                                                                                                           isRoot);
    portletApplication.setAccessPermissions(new String[] { "*:" + space.getGroupId() });
    portletApplication.setShowInfoBar(false);

    String pageTitle = space.getDisplayName() + " - " + app.getDisplayName();
    String pageName = app.getApplicationName();
    // is the application installed?
    if (SpaceUtils.isInstalledApp(space, appId) && (appName != null)) {
      pageName = appName;
    }
    UserPortalConfigService userPortalConfigService = getUserPortalConfigService();
    Page page = null;
    try {
      page = createSpacePage(userPortalConfigService, space, app, portletApplication, isRoot);
      page.setName(pageName);
      page.setTitle(pageTitle);

      // set permission for page
      String visibility = space.getVisibility();
      if (CollectionUtils.isNotEmpty(spaceApplication.getRoles())) {
        page.setAccessPermissions(spaceApplication.getRoles()
                                                  .stream()
                                                  .map(r -> r + ":" + space.getGroupId())
                                                  .toArray(String[]::new));
      } else if (StringUtils.equals(visibility, Space.PUBLIC)) {
        page.setAccessPermissions(new String[] { UserACL.EVERYONE });
      } else {
        page.setAccessPermissions(new String[] { "*:" + space.getGroupId() });
      }
      page.setEditPermission("manager:" + space.getGroupId());
      page.setProfiles(spaceApplication.getProfiles());

      SiteKey siteKey = navContext.getKey();
      PageKey pageKey = new PageKey(siteKey, page.getName());
      PageState pageState = new PageState(page.getTitle(),
                                          page.getDescription(),
                                          page.isShowMaxWindow(),
                                          page.isHideSharedLayout(),
                                          page.getFactoryId(),
                                          page.getProfiles(),
                                          Arrays.asList(page.getAccessPermissions()),
                                          page.getEditPermission(),
                                          PageType.PAGE.name(),
                                          null);

      pageStorage.savePage(new PageContext(pageKey, pageState));
      layoutService.save(page);
    } catch (Exception e) {
      LOG.warn("Error while creating the Page '{}' for space '{}'",
               pageTitle,
               space.getDisplayName(),
               e);
    }

    if (isRoot) {
      pageName = space.getUrl();
    } else {
      if (spaceApplication.getUri() != null && !spaceApplication.getUri().isEmpty()) {
        pageName = spaceApplication.getUri();
      }

    }
    NodeContext<NodeContext<?>> childNodeCtx = null;
    try {
      childNodeCtx = nodeCtx.add(null, pageName);
    } catch (Exception e) {
      LOG.debug("Tree t={} already in the map", pageName);
      childNodeCtx = nodeCtx.get(pageName);
    }
    Builder nodeStateBuilder = new NodeState.Builder()
                                                      .icon(spaceApplication.getIcon())
                                                      .pageRef(PageKey.parse(page.getPageId()));
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context != null && !context.getApplicationResourceBundle().containsKey(appId + ".label.name")) {
      nodeStateBuilder.label(app.getDisplayName());
    } else {
      nodeStateBuilder.label("#{" + appId + ".label.name}");
    }
    childNodeCtx.setState(nodeStateBuilder.build());
    return childNodeCtx;
  }

  protected Page createSpacePage(UserPortalConfigService userPortalConfigService,
                                 Space space,
                                 Application app,
                                 org.exoplatform.portal.config.model.Application<Portlet> portletApplication,
                                 boolean isRoot) throws Exception {
    Page page;
    if (isRoot) {
      page = userPortalConfigService.createPageTemplate("spaceHomePage",
                                                        PortalConfig.GROUP_TYPE,
                                                        space.getGroupId());
    } else {
      page = userPortalConfigService.createPageTemplate("space",
                                                        PortalConfig.GROUP_TYPE,
                                                        space.getGroupId());
      // setting some data to page.
      setPage(space, app, portletApplication, page);
    }
    return page;
  }

  /**
   * Gets an application by its id.
   *
   * @param appId
   * @return
   * @throws SpaceException
   */
  protected Application getApplication(String appId) throws SpaceException {
    Application application = SpaceUtils.getApplication(appId);
    if (application == null && appId.contains("/")) {
      application = SpaceUtils.getApplication(appId.split("/")[1]);
    }
    if (application == null) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_LIST_AVAILABLE_APPLICATIONS);
    }
    return application;
  }

  protected void setPage(Space space,
                         Application app,
                         org.exoplatform.portal.config.model.Application<Portlet> portletApplication,
                         Page page) {

    ArrayList<ModelObject> pageChilds = page.getChildren();

    //
    Container container = SpaceUtils.findContainerById(pageChilds, SpaceUtils.APPLICATION_CONTAINER);
    ArrayList<ModelObject> children = container.getChildren();

    children.add(portletApplication);

    container.setChildren(children);
    pageChilds = setContainerById(pageChilds, container);
    page.setChildren(pageChilds);
    setPermissionForPage(page.getChildren(), "*:" + space.getGroupId());
  }

  /**
   * Sets permission for page.
   *
   * @param children
   * @param perm
   */
  protected void setPermissionForPage(ArrayList<ModelObject> children, String perm) {
    for (ModelObject modelObject : children) {
      if (modelObject instanceof org.exoplatform.portal.config.model.Application<?>) {
        ((org.exoplatform.portal.config.model.Application) modelObject).setAccessPermissions(new String[] { perm });
      } else if (modelObject instanceof Container) {
        ((Container) modelObject).setAccessPermissions(new String[] { perm });
        setPermissionForPage(((Container) modelObject).getChildren(), perm);
      }
    }
  }

  /**
   * Sets container by Id
   *
   * @param childs
   * @param container
   * @return
   */
  protected ArrayList<ModelObject> setContainerById(ArrayList<ModelObject> childs, Container container) {
    ArrayList<ModelObject> result = childs;
    int index = result.indexOf(container);
    // if container existing and child of the page
    if (index != -1) {
      result.set(index, container);
    } else {
      for (int i = 0; i < result.size(); i++) {
        ModelObject obj = result.get(i);
        if (org.exoplatform.portal.config.model.Application.class.isInstance(obj)) {
          continue;
        }
        Container objContainer = (Container) obj;
        ArrayList<ModelObject> tmp = setContainerById(objContainer.getChildren(), container);
        objContainer.setChildren(tmp);
        result.set(i, objContainer);
      }
    }
    return result;
  }

  /**
   * Creates portlet application from instanceId
   *
   * @param instanceId
   * @return
   */
  protected org.exoplatform.portal.config.model.Application<Portlet> createPortletApplication(String instanceId,
                                                                                              Space space,
                                                                                              boolean isRoot) {
    int i0 = instanceId.indexOf("#");
    int i1 = instanceId.indexOf(":/", i0 + 1);
    String ownerType = instanceId.substring(0, i0);
    String ownerId = instanceId.substring(i0 + 1, i1);
    String persistenceid = instanceId.substring(i1 + 2);
    String[] persistenceChunks = Utils.split("/", persistenceid);
    PortletBuilder pb = new PortletBuilder();

    String spaceTemplateName = space.getTemplate();
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(spaceTemplateName);
    if (spaceTemplate == null) {
      throw new IllegalStateException("Space template with name " + spaceTemplateName + " wasn't found");
    }
    List<SpaceApplication> spaceApplicationList = spaceTemplate.getSpaceApplicationList();
    SpaceApplication spaceApplication = null;
    for (Iterator<SpaceApplication> iterator = spaceApplicationList.iterator(); iterator.hasNext() && spaceApplication == null;) {
      SpaceApplication tmpSpaceApplication = iterator.next();
      if (instanceId.contains(tmpSpaceApplication.getPortletName())) {
        spaceApplication = tmpSpaceApplication;
      }
    }
    if (spaceApplication != null && spaceApplication.getPreferences() != null) {
      Set<Entry<String, String>> entrySet = spaceApplication.getPreferences().entrySet();
      try {
        for (Map.Entry<String, String> preference : entrySet) {
          pb.add(preference.getKey(), getSubstituteValueFromPattern(space, spaceApplication, preference.getValue()));
        }
      } catch (Exception exception) {
        LOG.warn(exception.getMessage(), exception);
      }
    }

    TransientApplicationState<Portlet> portletState = new TransientApplicationState<Portlet>(
                                                                                             persistenceChunks[0] + "/" +
                                                                                                 persistenceChunks[1],
                                                                                             pb.build(),
                                                                                             ownerType,
                                                                                             ownerId);
    org.exoplatform.portal.config.model.Application<Portlet> portletApp =
                                                                        org.exoplatform.portal.config.model.Application.createPortletApplication();
    portletApp.setState(portletState);
    return portletApp;
  }

  protected String getSubstituteValueFromPattern(Space space, SpaceApplication spaceApplication, String pattern) {
    if (!pattern.contains("{") || !pattern.contains("}")) {
      return pattern;
    }

    if (pattern.contains(GROUP_ID_PREFERENCE)) {
      pattern = pattern.replace(GROUP_ID_PREFERENCE, space.getGroupId());
    } else if (pattern.contains(MODIFIED_GROUP_ID_PREFERENCE)) {
      String modifiedGroupId = space.getGroupId().replace("/", ".");
      pattern = pattern.replace(MODIFIED_GROUP_ID_PREFERENCE, modifiedGroupId);
    } else if (pattern.contains(PAGE_NAME_PREFERENCE)) {
      pattern = pattern.replace(PAGE_NAME_PREFERENCE, spaceApplication.getAppTitle());
    } else if (pattern.contains(PAGE_URL_PREFERENCE)) {
      pattern = pattern.replace(PAGE_URL_PREFERENCE, spaceApplication.getUri());
    }
    return pattern;
  }

  /**
   * Gets userPortalConfigService for the usage of creating new page from page
   * template
   *
   * @return
   */
  protected UserPortalConfigService getUserPortalConfigService() {
    return ExoContainerContext.getService(UserPortalConfigService.class);
  }

  protected SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }

  protected boolean isPortletActive(String potletFullId) {
    ModuleRegistry moduleRegistry = getModuleRegistry();
    if (moduleRegistry == null) {
      return true;
    }
    return moduleRegistry.isPortletActive(potletFullId);
  }

  protected ModuleRegistry getModuleRegistry() {
    if (moduleRegistry == null) {
      moduleRegistry = CommonsUtils.getService(ModuleRegistry.class);
    }
    return moduleRegistry;
  }

  protected NodeContext<NodeContext<?>> getSpaceUserNode(Space space) throws Exception {
    NavigationContext spaceNavCtx = SpaceUtils.getGroupNavigationContext(space.getGroupId());
    return ExoContainerContext.getService(NavigationService.class).loadNode(NodeModel.SELF_MODEL, spaceNavCtx, Scope.ALL, null);
  }

  protected SpaceApplication getSpaceApplication(SpaceTemplate spaceTemplate, String appId) throws SpaceException {
    Application application;
    try {
      application = getApplication(appId);
    } catch (SpaceException e) {
      throw e;
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.APPLICATION_NOT_FOUND);
    }
    SpaceApplication spaceApplication =
                                      spaceTemplate.getSpaceApplicationList()
                                                   .stream()
                                                   .filter(app -> StringUtils.equalsIgnoreCase(application.getApplicationName(),
                                                                                               app.getPortletName()))
                                                   .findFirst()
                                                   .orElse(null);
    if (spaceApplication == null) {
      throw new SpaceException(SpaceException.Code.APPLICATION_NOT_FOUND_IN_TEMPLATE);
    }
    return spaceApplication;
  }

}
