package org.exoplatform.social.core.upgrade;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.*;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.gatein.api.Portal;
import org.gatein.api.site.Site;
import org.gatein.api.common.Filter;

import org.gatein.api.site.SiteQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;

import static org.exoplatform.social.core.space.SpaceUtils.getGroupNavigationContext;
@RunWith(MockitoJUnitRunner.class)
public class SpaceNodeIconsPluginTest extends AbstractKernelTest {

  protected PortalContainer      container;

  protected SpaceService         spaceService;

  protected LayoutService        layoutService;

  protected NavigationService    navigationService;

  protected IdentityRegistry     identityRegistry;

  protected EntityManagerService entityManagerService;


  protected SettingService       settingService;

  private SpaceNodeIconsPlugin   spaceNodeIconsPlugin;

  @Before
  public void setUp() {
    container = PortalContainer.getInstance();
    entityManagerService = container.getComponentInstanceOfType(EntityManagerService.class);
    spaceService = container.getComponentInstanceOfType(SpaceService.class);
    layoutService = container.getComponentInstanceOfType(LayoutService.class);
    navigationService = container.getComponentInstanceOfType(NavigationService.class);
    begin();
    RequestLifeCycle.begin(container);
    InitParams initParams = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("product.group.id");
    valueParam.setValue("org.exoplatform.platform");
    ValueParam navNodeNamesvalueParam = new ValueParam();
    navNodeNamesvalueParam.setName("space.node.names");
    navNodeNamesvalueParam.setValue("settings;members;notes;tasks,TaskManagementApplication,Tasks;SpaceWallet");
    ValueParam navNodeIconsvalueParam = new ValueParam();
    navNodeIconsvalueParam.setName("space.node.icons");
    navNodeIconsvalueParam.setValue("fas fa-cog;fas fa-users;fas fa-clipboard;fas fa-tasks;fas fa-wallet");
    initParams.addParameter(valueParam);
    initParams.addParameter(navNodeNamesvalueParam);
    initParams.addParameter(navNodeIconsvalueParam);
    spaceNodeIconsPlugin = new SpaceNodeIconsPlugin(container, entityManagerService, initParams);
  }

  @After
  public void tearDown() throws Exception {
    RequestLifeCycle.end();
  }
  protected void begin() {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
  }
  @Test
  public void processUpgrade() throws Exception {
    HashSet<MembershipEntry> memberships = new HashSet<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/users", "*"));
    memberships.add(new MembershipEntry("/platform/administrators", "*"));
    Identity root = new Identity("root", memberships);
    IdentityRegistry identityRegistry1 = CommonsUtils.getService(IdentityRegistry.class);
    identityRegistry1.register(root);
    ConversationState conversationState = new ConversationState(root);
    ConversationState.setCurrent(conversationState);
    Space space = new Space();
    space.setDisplayName("testspace");
    space.setPrettyName(space.getDisplayName());
    String shortName = SpaceUtils.cleanString(space.getDisplayName());
    space.setGroupId("/spaces/" + shortName);
    space.setUrl(shortName);
    space.setEditor("root");
    space.setTemplate("communication");
    space.setVisibility("public");
    space.setRegistration("validation");
    space.setPriority("2");
    String[] manager = new String[] { "root" };
    String[] members = new String[] { "root", "john" };
    space.setManagers(manager);
    space.setMembers(members);

    space = spaceService.createSpace(space, "root");
    List<UserNode> spaceUserNodeChildren= SpaceUtils.getSpaceUserNodeChildren(space);
    String icon = spaceUserNodeChildren.get(1).getIcon();
    assertNull(icon);
    spaceNodeIconsPlugin.processUpgrade(null, null);
    spaceUserNodeChildren= SpaceUtils.getSpaceUserNodeChildren(space);
    icon = spaceUserNodeChildren.get(1).getIcon();
    assertEquals(icon, "fas fa-cog");


  }

}
