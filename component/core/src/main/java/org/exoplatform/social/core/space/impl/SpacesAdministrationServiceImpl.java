package org.exoplatform.social.core.space.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import jakarta.servlet.ServletContext;

/**
 * Service to manage administration of spaces
 */
@ManagedBy(SpacesAdministrationServiceManagerBean.class)
public class SpacesAdministrationServiceImpl implements Startable, SpacesAdministrationService {

  private static final String   SPACES_ADMINISTRATORS_PARAM       = "social.spaces.administrators";

  private static final String   SPACES_ADMINISTRATORS_SETTING_KEY = "social.spaces.administrators";

  public static final PageKey   SPACES_ADMINISTRATION_PAGE_KEY    = PageKey.parse("group::/platform/users::spacesAdministration");

  private LayoutService         layoutService;

  private SettingService        settingService;

  private UserACL               userACL;

  private InitParams            initParams;

  private List<MembershipEntry> spacesAdministratorsMemberships   = new ArrayList<>();

  public SpacesAdministrationServiceImpl(SettingService settingService,
                                         LayoutService layoutService,
                                         UserACL userACL,
                                         InitParams initParams) {
    this.settingService = settingService;
    this.layoutService = layoutService;
    this.userACL = userACL;
    this.initParams = initParams;
  }

  @Override
  public void start() {
    loadSettings(initParams);

    // update Spaces administration at startup in case the configuration has
    // changed
    PortalContainer.addInitTask(PortalContainer.getInstance().getPortalContext(),
                                new RootContainer.PortalContainerPostInitTask() {
                                  @Override
                                  public void execute(ServletContext context, PortalContainer portalContainer) {
                                    List<MembershipEntry> superManagersMemberships =
                                                                                   SpacesAdministrationServiceImpl.this.getSpacesAdministratorsMemberships();
                                    updateSpacesAdministrationPagePermissions(superManagersMemberships);
                                  }
                                });
  }

  @Override
  public void updateSpacesAdministratorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if (permissionsExpressions == null) {
      throw new IllegalArgumentException("Permission expressions list couldn't be null");
    }

    this.spacesAdministratorsMemberships = permissionsExpressions;

    settingService.set(Context.GLOBAL,
                       Scope.GLOBAL,
                       SPACES_ADMINISTRATORS_SETTING_KEY,
                       SettingValue.create(StringUtils.join(this.spacesAdministratorsMemberships, ",")));

    updateSpacesAdministrationPagePermissions(this.spacesAdministratorsMemberships);
  }

  @Override
  public List<MembershipEntry> getSpacesAdministratorsMemberships() {
    return Collections.unmodifiableList(spacesAdministratorsMemberships);
  }

  @Override
  public boolean isSuperManager(String username) {
    if (StringUtils.isBlank(username)
        || IdentityConstants.ANONIM.equals(username)
        || IdentityConstants.SYSTEM.equals(username)) {
      return false;
    } else if (username.equals(userACL.getSuperUser())) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = userACL.getUserIdentity(username);
    return identity != null && (identity.isMemberOf(userACL.getAdminGroups())
                                || getSpacesAdministratorsMemberships().stream()
                                                                       .anyMatch(identity::isMemberOf));
  }

  /**
   * Load Spaces Administration settings For Spaces Administrators setting, it
   * uses the value stored in the settings if any, otherwise it uses the value
   * from the configuration
   * 
   * @param initParams Service init parameters
   */
  @SuppressWarnings("unchecked")
  protected void loadSettings(InitParams initParams) {
    SettingValue<String> administrators = (SettingValue<String>) settingService.get(Context.GLOBAL,
                                                                                    Scope.GLOBAL,
                                                                                    SPACES_ADMINISTRATORS_SETTING_KEY);
    if (administrators != null && !StringUtils.isBlank(administrators.getValue())) {
      String[] administratorsArray = administrators.getValue().split(",");
      addSpacesAdministratorsMemberships(administratorsArray);
    } else if (initParams != null) {
      ValueParam spacesAdministratorsParam = initParams.getValueParam(SPACES_ADMINISTRATORS_PARAM);
      if (spacesAdministratorsParam != null) {
        String memberships = spacesAdministratorsParam.getValue();
        if (StringUtils.isNotBlank(memberships)) {
          String[] spacesAdministratorsMembershipsArray = memberships.split(",");
          addSpacesAdministratorsMemberships(spacesAdministratorsMembershipsArray);
        }
      }
    }
  }

  private void addSpacesAdministratorsMemberships(String[] administratorsArray) {
    for (String administrator : administratorsArray) {
      if (StringUtils.isBlank(administrator)) {
        continue;
      }
      if (!administrator.contains(":/")) {
        this.spacesAdministratorsMemberships.add(new MembershipEntry(administrator));
      } else {
        String[] membershipParts = administrator.split(":");
        this.spacesAdministratorsMemberships.add(new MembershipEntry(membershipParts[1], membershipParts[0]));
      }
    }
  }

  /**
   * Update permissions of the Spaces Administration page
   * 
   * @param superManagersMemberships New memberships to apply as read
   *          permissions on the page
   */
  private void updateSpacesAdministrationPagePermissions(List<MembershipEntry> superManagersMemberships) {
    if (superManagersMemberships != null) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        PageContext pageContext = layoutService.getPageContext(SPACES_ADMINISTRATION_PAGE_KEY);
        if (pageContext != null) {
          PageState page = pageContext.getState();
          PageState pageState = new PageState(page.getDisplayName(),
                                              page.getDescription(),
                                              page.getShowMaxWindow(),
                                              page.getFactoryId(),
                                              superManagersMemberships.stream()
                                                                      .map(membership -> membership.getMembershipType() + ":" +
                                                                          membership.getGroup())
                                                                      .toList(),
                                              page.getEditPermission(),
                                              null,
                                              null);
          layoutService.save(new PageContext(SPACES_ADMINISTRATION_PAGE_KEY, pageState));
        }
      } finally {
        RequestLifeCycle.end();
      }
    }
  }
}
