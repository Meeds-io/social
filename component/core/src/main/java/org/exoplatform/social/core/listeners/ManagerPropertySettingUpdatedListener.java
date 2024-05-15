package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;

import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.storage.PageStorage;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import java.util.List;

public class ManagerPropertySettingUpdatedListener extends Listener<ProfilePropertyService, ProfilePropertySetting> {

  public static final List<String> orgChartPages = List.of("portal::mycraft::myteam", "portal::global::organizationalchart");
  private static final List<String> ALL_USERS_PERMISSION = List.of("*:/platform/users");
  private final PageStorage pageStorage;
  private final UserACL userACL;

  public ManagerPropertySettingUpdatedListener(PageStorage pageStorage, UserACL userACL) {
    this.pageStorage = pageStorage;
    this.userACL = userACL;
  }

  @Override
  public void onEvent(Event<ProfilePropertyService, ProfilePropertySetting> event) throws Exception {
    ProfilePropertySetting propertySetting = event.getData();
    if ("manager".equalsIgnoreCase(event.getData().getPropertyName())) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        for(String pageRefKey : orgChartPages) {
          PageKey pageKey = PageKey.parse(pageRefKey);
          PageContext pageContext = pageStorage.loadPage(pageKey);
          if (pageContext != null) {
            PageState page = pageContext.getState();
            PageState pageState = new PageState(page.getDisplayName(),
                    page.getDescription(),
                    page.getShowMaxWindow(),
                    page.getFactoryId(),
                    propertySetting.isActive() ? ALL_USERS_PERMISSION : List.of(userACL.getSuperUser()),
                    page.getEditPermission(),
                    page.getMoveAppsPermissions(),
                    page.getMoveContainersPermissions());
            pageStorage.savePage(new PageContext(pageKey, pageState));
          }
        }
      } finally {
        RequestLifeCycle.end();
      }
    }
  }
}
