package io.meeds.social.upgrade;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.jdbc.entity.PageEntity;
import org.exoplatform.portal.jdbc.entity.WindowEntity;
import org.exoplatform.portal.mop.dao.PageDAO;
import org.exoplatform.portal.mop.dao.WindowDAO;
import org.exoplatform.portal.mop.storage.PageStorage;
import org.exoplatform.portal.pom.data.ModelData;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SpaceBannerHomePageUpgradePlugin extends UpgradeProductPlugin {

  private static final Log log = ExoLogger.getLogger(SpaceBannerHomePageUpgradePlugin.class);

  private final EntityManagerService entityManagerService;

  private final PageStorage pageStorage;

  private final PageDAO     pageDAO;

  private final WindowDAO   windowDAO;

  public SpaceBannerHomePageUpgradePlugin(SettingService settingService,
                                          InitParams initParams,
                                          EntityManagerService entityManagerService,
                                          PageStorage pageStorage,
                                          PageDAO pageDAO,
                                          WindowDAO windowDAO) {
    super(settingService, initParams);
    this.entityManagerService = entityManagerService;
    this.pageStorage = pageStorage;
    this.pageDAO = pageDAO;
    this.windowDAO = windowDAO;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long start = System.currentTimeMillis();
    log.info("Start:: Update Space Home Page banner content id");

    List<Long> spaceHomePagesId = getSpaceHomePages();
    spaceHomePagesId.forEach(pageId -> {

      PageEntity page = pageDAO.find(pageId);
      JSONArray jsonArray = new JSONArray(page.getPageBody());
      for (int i = 0; i < jsonArray.length(); i++) {
        checkChildren(jsonArray.getJSONObject(i));
      }
    });

    log.info("End:: Update Space Home Page banner content id in {} ms", System.currentTimeMillis() - start);
  }

  private void checkChildren(JSONObject element) {
    long id = element.getLong("id");
    String type = element.getString("type");
    if (type.equals("WINDOW")) {
      WindowEntity windowEntity = windowDAO.find(id);
      if (windowEntity.getContentId().equals("social-portlet/SpaceMenuPortlet")) {
        updateWindow(id);
      }
    } else {
      if (element.has("children")) {
        JSONArray children = element.getJSONArray("children");
        for (int i = 0; i < children.length(); i++) {
          checkChildren(children.getJSONObject(i));
        }
      }
    }
  }

  private List<Long> getSpaceHomePages() {
    RequestLifeCycle.begin(this.entityManagerService);
    EntityManager entityManager = this.entityManagerService.getEntityManager();
    List<Long> results = new ArrayList<>();
    try {
      String sqlString = "SELECT pnn.PAGE_ID FROM PORTAL_NAVIGATION_NODES pnn WHERE pnn.PARENT_ID in ("
          + "SELECT pn.NODE_ID FROM PORTAL_NAVIGATIONS pn WHERE pn.SITE_ID in ("
          + "SELECT ps.ID FROM PORTAL_SITES ps WHERE ps.TYPE=1"
          + ")"
          + ")";
      Query query = entityManager.createNativeQuery(sqlString, Long.class);
      results = query.getResultList();
    } catch (Exception e) {
      log.error("Error when getting Space Home Pages", e);
    } finally {
      RequestLifeCycle.end();
    }
    return results;
  }

  private void updateWindow(long id) {
    RequestLifeCycle.begin(this.entityManagerService);
    EntityManager entityManager = this.entityManagerService.getEntityManager();
    if (!entityManager.getTransaction().isActive()) {
      entityManager.getTransaction().begin();
    }
    String sqlString = "UPDATE PORTAL_WINDOWS SET CONTENT_ID='social/SpaceBannerPortlet' WHERE ID=" + id;
    try {
      Query nativeQuery = entityManager.createNativeQuery(sqlString);
      nativeQuery.executeUpdate();
    } finally {
      RequestLifeCycle.end();
    }
  }
}
