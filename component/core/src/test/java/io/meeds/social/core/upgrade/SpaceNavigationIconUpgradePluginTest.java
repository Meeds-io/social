package io.meeds.social.core.upgrade;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradePluginExecutionContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@RunWith(MockitoJUnitRunner.class)
public class SpaceNavigationIconUpgradePluginTest {
  private SpaceNavigationIconUpgradePlugin spaceNavigationIconUpgradePlugin;
  @Mock
  private EntityManagerService entityManagerService;

  InitParams initParams = new InitParams();

  @Before
  public void setUp() {
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
    this.spaceNavigationIconUpgradePlugin = new SpaceNavigationIconUpgradePlugin(entityManagerService, initParams);
  }

  @Test
  public void testProcessUpgrade() {
    EntityManager entityManager = mock(EntityManager.class);
    when(entityManagerService.getEntityManager()).thenReturn(entityManager);
    Query query = mock(Query.class);
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.executeUpdate()).thenReturn(2);
    boolean proceedToUpgrade = spaceNavigationIconUpgradePlugin.shouldProceedToUpgrade(null, null);
    assertTrue(proceedToUpgrade);
    UpgradePluginExecutionContext upgradePluginExecutionContext = new UpgradePluginExecutionContext("6.4.0", 0);
    proceedToUpgrade = spaceNavigationIconUpgradePlugin.shouldProceedToUpgrade("6.5.0", "6.4.0", upgradePluginExecutionContext);
    assertTrue(proceedToUpgrade);
    spaceNavigationIconUpgradePlugin.processUpgrade("oldVersion", "newVersion");
    assertEquals(2, spaceNavigationIconUpgradePlugin.getMigratedSpaceNodeIcons());
  }
}