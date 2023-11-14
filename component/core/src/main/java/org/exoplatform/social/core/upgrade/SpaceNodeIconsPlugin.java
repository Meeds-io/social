package org.exoplatform.social.core.upgrade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class SpaceNodeIconsPlugin extends UpgradeProductPlugin {

  private static final Log           LOG              = ExoLogger.getExoLogger(SpaceNodeIconsPlugin.class);

  private static final String        SPACE_NODE_NAMES = "space.node.names";

  private static final String        SPACE_NODE_ICONS = "space.node.icons";

  private final EntityManagerService entityManagerService;

  private final Map<String, String>  spaceNodes       = new HashMap<>();

  private int                        migratedSpaceNodeIcons;

  public int getMigratedSpaceNodeIcons() {
    return migratedSpaceNodeIcons;
  }

  public SpaceNodeIconsPlugin(PortalContainer container, EntityManagerService entityManagerService, InitParams initParams) {
    super(initParams);
    this.entityManagerService = entityManagerService;
    if (initParams.containsKey(SPACE_NODE_NAMES) && initParams.containsKey(SPACE_NODE_ICONS)) {
      String[] spaceNodeNames = initParams.getValueParam(SPACE_NODE_NAMES).getValue().split(";");
      String[] spaceNodeIcons = initParams.getValueParam(SPACE_NODE_ICONS).getValue().split(";");
      for (int i = 0; i < spaceNodeNames.length; i++) {
        this.spaceNodes.put(spaceNodeNames[i], spaceNodeIcons[i]);
      }
    }
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    if (spaceNodes == null || spaceNodes.isEmpty()) {
      LOG.error("Couldn't process upgrade, the parameters '{}' and '{}' are mandatory", SPACE_NODE_NAMES, SPACE_NODE_ICONS);
      return;
    }
    long startupTime = System.currentTimeMillis();

    LOG.info("Start:: Upgrade of space node icons");
    Set<Map.Entry<String, String>> spaceNodesEntrySet = spaceNodes.entrySet();
    migratedSpaceNodeIcons = upgradeSpaceNodeIcons(spaceNodesEntrySet);
    LOG.info("End:: Upgrade of '{}' space node icons. It tooks {} ms",
             migratedSpaceNodeIcons,
             (System.currentTimeMillis() - startupTime));
  }

  @ExoTransactional
  public int upgradeSpaceNodeIcons(Set<Map.Entry<String, String>> spaceNodesEntrySet) {
    EntityManager entityManager = entityManagerService.getEntityManager();
    StringBuilder sqlString = new StringBuilder("""
            UPDATE PORTAL_NAVIGATION_NODES AS n
            INNER JOIN PORTAL_PAGES AS p ON n.PAGE_ID = p.ID
            INNER JOIN PORTAL_SITES AS s ON s.ID = p.SITE_ID
            LEFT JOIN PORTAL_NAVIGATION_NODES AS parent_nodes ON n.PARENT_ID = parent_nodes.NODE_ID
        """);
    sqlString.append("""
            SET n.ICON =
              CASE
                WHEN parent_nodes.NAME = 'default' THEN 'fas fa-stream'
        """);

    for (Map.Entry<String, String> spaceNodesEntry : spaceNodesEntrySet) {
      List<String> spaceNodesNames = Stream.of(spaceNodesEntry.getKey().split(","))
                                           .map(spaceNodeName -> "'" + spaceNodeName + "'")
                                           .toList();
      sqlString.append("   WHEN n.NAME in (")
               .append(String.join(",", spaceNodesNames))
               .append(") THEN '")
               .append(spaceNodesEntry.getValue())
               .append("'\n");
    }
    sqlString.append("  END\n");
    sqlString.append("WHERE n.ICON IS NULL AND s.TYPE = 1 AND s.NAME LIKE '/spaces/%'");

    Query query = entityManager.createNativeQuery(sqlString.toString());
    return query.executeUpdate();
  }
}
