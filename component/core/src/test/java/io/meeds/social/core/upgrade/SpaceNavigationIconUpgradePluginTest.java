package io.meeds.social.core.upgrade;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradePluginExecutionContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
    //
    assertTrue(proceedToUpgrade);
    UpgradePluginExecutionContext upgradePluginExecutionContext = new UpgradePluginExecutionContext("6.4.0", 0);
    proceedToUpgrade = spaceNavigationIconUpgradePlugin.shouldProceedToUpgrade("6.5.0", "6.4.0", upgradePluginExecutionContext);
    //
    assertTrue(proceedToUpgrade);
    spaceNavigationIconUpgradePlugin.processUpgrade("oldVersion", "newVersion");
    // Capture the argument passed to createNativeQuery
    ArgumentCaptor<String> sqlStatementCaptor = ArgumentCaptor.forClass(String.class);
    verify(entityManager).createNativeQuery(sqlStatementCaptor.capture());

    // Get the captured SQL statement
    String actualSQLStatement = sqlStatementCaptor.getValue();

    // Expected SQL statement
    String expectedSql = "  UPDATE PORTAL_NAVIGATION_NODES\n" +
            "  SET ICON =\n" +
            "    CASE\n" +
            "      WHEN PARENT_ID IN (SELECT NODE_ID FROM (SELECT * FROM PORTAL_NAVIGATION_NODES WHERE NAME LIKE 'default') AS PARENT_NAVIGATION) THEN TRIM('fas fa-stream')\n" +
            "         WHEN NAME in ('settings') THEN TRIM('fas fa-cog')\n   WHEN NAME in ('members') THEN TRIM('fas fa-users')\n\n" +
            "    END\n" +
            "  WHERE ICON IS NULL\n" +
            "  AND EXISTS (SELECT * FROM PORTAL_PAGES p INNER JOIN PORTAL_SITES s ON s.ID = p.SITE_ID WHERE PAGE_ID = p.ID AND s.TYPE = 1 AND s.NAME LIKE '/spaces/%')\n";
    // Assert the captured SQL statement is equal to expected SQL statement
    assertEquals(expectedSql.trim(),actualSQLStatement.trim());
    verify(query).executeUpdate();
    // Verify the result
    assertEquals(2, spaceNavigationIconUpgradePlugin.getMigratedSpaceNodeIcons());
    // Verify no more interactions
    verifyNoMoreInteractions(entityManager, query);
  }
}