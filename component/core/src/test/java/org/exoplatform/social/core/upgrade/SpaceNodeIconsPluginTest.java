package org.exoplatform.social.core.upgrade;

import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.dao.NodeDAO;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration-local.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/exoplatform/portal/config/conf/configuration.xml"), })
public class SpaceNodeIconsPluginTest extends AbstractKernelTest {

  protected PortalContainer      container;

  protected SpaceService         spaceService;

  protected NavigationService    navigationService;

  protected IdentityRegistry     identityRegistry;

  protected EntityManagerService entityManagerService;

  protected SettingService       settingService;

  private SpaceNodeIconsPlugin   spaceNodeIconsPlugin;

  private NodeDAO                nodeDao;

  @Before
  public void setUp() {
    container = PortalContainer.getInstance();
    entityManagerService = container.getComponentInstanceOfType(EntityManagerService.class);
    spaceService = container.getComponentInstanceOfType(SpaceService.class);
    navigationService = container.getComponentInstanceOfType(NavigationService.class);
    nodeDao = container.getComponentInstanceOfType(NodeDAO.class);
    identityRegistry = container.getComponentInstanceOfType(IdentityRegistry.class);
    begin();
    InitParams initParams = new InitParams();
    ValueParam productGroupIdValueParam = new ValueParam();
    productGroupIdValueParam.setName("product.group.id");
    productGroupIdValueParam.setValue("org.exoplatform.platform");
    ValueParam spaceNodeNamesValueParam = new ValueParam();
    spaceNodeNamesValueParam.setName("space.node.names");
    spaceNodeNamesValueParam.setValue("settings;members");
    ValueParam spaceNodeIconsValueParam = new ValueParam();
    spaceNodeIconsValueParam.setName("space.node.icons");
    spaceNodeIconsValueParam.setValue("fas fa-cog;fas fa-users");
    initParams.addParameter(productGroupIdValueParam);
    initParams.addParameter(spaceNodeNamesValueParam);
    initParams.addParameter(spaceNodeIconsValueParam);
    spaceNodeIconsPlugin = new SpaceNodeIconsPlugin(entityManagerService, initParams);
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
  public void testProcessUpgrade() throws Exception {
    HashSet<MembershipEntry> memberships = new HashSet<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/users", "*"));
    memberships.add(new MembershipEntry("/platform/administrators", "*"));
    Identity root = new Identity("root", memberships);
    identityRegistry.register(root);
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
    List<UserNode> spaceUserNodeChildren = SpaceUtils.getSpaceUserNodeChildren(space);
    
    long streamNodeId = Long.parseLong(navigationService.loadNode(SiteKey.group(space.getGroupId())).getNode(0).getId());
    assertNotNull(nodeDao.find(streamNodeId));
    assertNull(nodeDao.find(streamNodeId).getIcon());
    long dashboardNodeId = Long.parseLong(spaceUserNodeChildren.get(0).getId());
    assertNotNull(nodeDao.find(dashboardNodeId));
    assertNull(nodeDao.find(dashboardNodeId).getIcon());
    long settingsNodeId = Long.parseLong(spaceUserNodeChildren.get(1).getId());
    assertNotNull(nodeDao.find(settingsNodeId));
    assertNull(nodeDao.find(settingsNodeId).getIcon());
    long membersNodeId = Long.parseLong(spaceUserNodeChildren.get(2).getId());
    assertNotNull(nodeDao.find(membersNodeId));
    assertNull(nodeDao.find(membersNodeId).getIcon());

    spaceNodeIconsPlugin.processUpgrade(null, null);
    restartTransaction();
    assertEquals("fas fa-stream", nodeDao.find(streamNodeId).getIcon());
    assertNull(nodeDao.find(dashboardNodeId).getIcon());
    assertEquals("fas fa-cog", nodeDao.find(settingsNodeId).getIcon());
    assertEquals("fas fa-users", nodeDao.find(membersNodeId).getIcon());
  }
}
