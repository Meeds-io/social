package org.exoplatform.social.core.upgrade;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class SpaceNodeIconsPlugin extends UpgradeProductPlugin {
  private static final Log           LOG                  = ExoLogger.getExoLogger(SpaceNodeIconsPlugin.class);

  private static final String        NAVIGATION_NODE_NAME = "nav.node.name";

  private static final String        NAVIGATION_NODE_ICON = "nav.node.icon";

  private final PortalContainer      container;

  private final EntityManagerService entityManagerService;

  private final Map<String, String>  navigationNodes      = new HashMap<>();

  private String[]                   nodeNames;

  private String[]                   nodeIcons;

  public SpaceNodeIconsPlugin(PortalContainer container, EntityManagerService entityManagerService, InitParams initParams) {
    super(initParams);
    this.container = container;
    this.entityManagerService = entityManagerService;

    if (initParams.containsKey(NAVIGATION_NODE_NAME)) {
      nodeNames = initParams.getValueParam(NAVIGATION_NODE_NAME).getValue().split(";");
    }
    if (initParams.containsKey(NAVIGATION_NODE_ICON)) {
      nodeIcons = initParams.getValueParam(NAVIGATION_NODE_ICON).getValue().split(";");
    }
    for (int i = 0; i < nodeNames.length; i++) {
      navigationNodes.put(nodeNames[i], nodeIcons[i]);
    }
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    if (navigationNodes.isEmpty()) {
      LOG.error("Couldn't process upgrade, the parameter '{}' is mandatory", NAVIGATION_NODE_NAME);
      return;
    }
    long startupTime = System.currentTimeMillis();

    ExoContainerContext.setCurrentContainer(container);
    boolean transactionStarted = false;
    Set<Map.Entry<String, String>> navigationNodesEntrySet = navigationNodes.entrySet();
    LOG.info("Start upgrade of navigation node icons");
    RequestLifeCycle.begin(this.entityManagerService);
    EntityManager entityManager = this.entityManagerService.getEntityManager();
    try {
      if (!entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().begin();
        transactionStarted = true;
      }
      StringBuilder sqlString =
                              new StringBuilder("""
                                  UPDATE jpa.PORTAL_NAVIGATION_NODES AS n
                                  INNER JOIN jpa.PORTAL_PAGES AS p ON n.PAGE_ID = p.ID
                                  INNER JOIN jpa.PORTAL_SITES AS s ON s.ID = p.SITE_ID
                                  LEFT JOIN jpa.PORTAL_NAVIGATION_NODES AS parent_nodes ON n.PARENT_ID = parent_nodes.NODE_ID
                                  SET n.ICON =\s
                                      CASE\s
                                          WHEN (parent_nodes.NAME = 'default' AND s.TYPE = 1 AND s.NAME LIKE '/spaces/%' AND parent_nodes.ICON IS NULL) THEN 'fas fa-stream'
                                  """);
      for (Map.Entry<String, String> navNode : navigationNodesEntrySet) {
        List<String> allNames = Stream.of(navNode.getKey().split(",")).map(name -> "'" + name + "'").toList();
        sqlString.append("WHEN (n.NAME in (")
                 .append(String.join(",", allNames))
                 .append(") AND s.TYPE = 1 AND s.NAME LIKE '/spaces/%' AND parent_nodes.ICON IS NULL) THEN '")
                 .append(navNode.getValue())
                 .append("'\n");
      }
      sqlString.append("    END;\n");
      Query nativeQuery = entityManager.createNativeQuery(sqlString.toString());
      int update = nativeQuery.executeUpdate();
      LOG.info("End upgrade of '{}' navigation node icons. It took {} ms", update, (System.currentTimeMillis() - startupTime));
      if (transactionStarted && entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().commit();
        entityManager.clear();
        entityManager.flush();
      }
    } catch (Exception e) {
      if (transactionStarted && entityManager.getTransaction().isActive() && entityManager.getTransaction().getRollbackOnly()) {
        entityManager.getTransaction().rollback();
      }
    } finally {
      RequestLifeCycle.end();
    }
  }
}
