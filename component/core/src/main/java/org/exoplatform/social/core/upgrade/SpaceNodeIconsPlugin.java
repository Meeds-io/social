package org.exoplatform.social.core.upgrade;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.api.Portal;
import org.gatein.api.common.Filter;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteQuery;
import org.gatein.api.site.SiteType;

import java.util.List;

public class SpaceNodeIconsPlugin extends UpgradeProductPlugin {
  private static final Log LOG = ExoLogger.getExoLogger(UserPasswordHashMigration.class);

  private final Portal     portal;

  public SpaceNodeIconsPlugin(Portal portal, InitParams initParams) {
    super(initParams);
    this.portal = portal;
  }

  @Override
  public void processUpgrade(String s, String s1) {
    LOG.info("Start upgrade of space node icons");
    try {
      Filter<Site> filter = site -> site.getName().startsWith("/spaces/");
      SiteQuery pageQuery = new SiteQuery.Builder().withSiteTypes(SiteType.SPACE).withFilter(filter).build();
      List<Site> spaceSites = portal.findSites(pageQuery);
      spaceSites.forEach(this::setDefaultIcons);
    } catch (Exception e) {
      throw new IllegalStateException("SpaceNodeIconsPlugin upgrade failed due to previous errors");
    } finally {
      RequestLifeCycle.end();
    }
  }

  public void setDefaultIcons(Site site) {
    Navigation navigation = portal.getNavigation(site.getId());
    Node node = navigation.getRootNode(Nodes.visitAll()).getChild(0);
    if (node.getIconName() == null) {
      node.setIconName("fas fa-stream");
    }
    navigation.saveNode(node);
    for (int i = 0; i < node.getChildCount(); i++) {
      Node childNode = node.getChild(i);
      if (childNode.getIconName() == null) {
        switch (childNode.getName()) {
        case "documents" -> childNode.setIconName("fas fa-folder-open");
        case "agenda" -> childNode.setIconName("fas fa-calendar-alt");
        case "settings" -> childNode.setIconName("fas fa-cog");
        case "members" -> childNode.setIconName("fas fa-users");
        case "SpaceWallet" -> childNode.setIconName("fas fa-wallet");
        case "notes" -> childNode.setIconName("fas fa-clipboard");
        case "tasks" -> childNode.setIconName("fas fa-tasks");
        }
        navigation.saveNode(childNode);
      }
    }
  }
}
