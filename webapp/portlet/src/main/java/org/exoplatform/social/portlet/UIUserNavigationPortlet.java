/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.portlet;

import java.util.*;

import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.user.*;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.NavigationURLUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.UIBannerAvatarUploader;
import org.exoplatform.social.webui.UIBannerUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * @author <a href="fbradai@exoplatform.com">Fbradai</a>
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class,

    template = "app:/groovy/social/portlet/UIUserNavigationPortlet.gtmpl", events = {
        @EventConfig(listeners = UIUserNavigationPortlet.RemoveBannerActionListener.class)
    })
public class UIUserNavigationPortlet extends UIPortletApplication {

  private static final Log       LOG                       = ExoLogger.getLogger(UIUserNavigationPortlet.class);

  public static final String     ACTIVITIES_URI            = "activities";

  public static final String     PROFILE_URI               = "profile";

  public static final String     CONNEXIONS_URI            = "connections";

  public static final String     WIKI_URI                  = "wiki";

  public static final String     WALLET_FEATURE_NAME       = "wallet";

  public static final String     WALLET_URI                = "wallet";

  public static final String     GAMIFICATION_FEATURE_NAME = "gamification";

  public static final String     GAMIFICATION_URI          = "achievements";

  private static final String    INVISIBLE                 = "invisible";

  public static String           DEFAULT_TAB_NAME          = "Tab_Default";

  private static final String    USER                      = "/user/";

  private static final String    WIKI_HOME                 = "/WikiHome";

  private static final String    WIKI_REF                  = "wiki";

  private static final String    NOTIF_REF                 = "notifications";

  private static final String    NOTIFICATION_SETTINGS     = "NotificationSettingsPortlet";

  private static final String    EDIT_PROFILE_NODE         = "edit-profile";

  public static final String     OFFLINE_STATUS            = "offline";

  public static final String     OFFLINE_TITLE             = "UIUserNavigationPortlet.label.offline";

  public static final String     USER_STATUS_TITLE         = "UIUserNavigationPortlet.label.";

  private UserNodeFilterConfig   toolbarFilterConfig;

  private UIBannerUploader       uiBanner                  = null;

  private UIBannerAvatarUploader uiAvatarBanner            = null;

  private Map<String, String>    userNodes;

  public UIUserNavigationPortlet() throws Exception {
    UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
    builder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL).withTemporalCheck();
    toolbarFilterConfig = builder.build();

    uiBanner = createUIComponent(UIBannerUploader.class, null, null);
    addChild(uiBanner);
    uiAvatarBanner = createUIComponent(UIBannerAvatarUploader.class, null, null);
    addChild(uiAvatarBanner);

    UIRelationshipAction uiAction = createUIComponent(UIRelationshipAction.class, null, null);
    addChild(uiAction);
  }

  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    uiBanner.setRendered(isProfileOwner());
    uiAvatarBanner.setRenderUpload(isProfileOwner());
    super.processRender(app, context);
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    uiBanner.setRendered(isProfileOwner());
    uiAvatarBanner.setRenderUpload(isProfileOwner());
    super.processRender(context);
  }

  public boolean isSelectedUserNavigation(String nav) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    UserNode selectedNode = uiPortal.getSelectedUserNode();
    if (selectedNode.getURI().contains(nav)) {
      return true;
    }
    if (NOTIFICATION_SETTINGS.equals(nav) && "notifications".equals(selectedNode.getURI())) {
      return true;
    }
    // case dashboard : user navigation nodes are used for dashboard
    String requestUrl = Util.getPortalRequestContext().getRequest().getRequestURL().toString();
    if (requestUrl.contains("/u/")) {
      return true;
    }
    //
    return false;
  }

  public boolean isProfileOwner() {
    return Utils.isOwner();
  }

  public Profile getOwnerProfile() {
    return Utils.getOwnerIdentity(true).getProfile();
  }

  public String getAvatarURL(Profile profile) {
    String ownerAvatar = profile.getAvatarUrl();
    if (ownerAvatar == null || ownerAvatar.isEmpty()) {
      ownerAvatar = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
    }
    return ownerAvatar;
  }

  protected UserNavigation getSelectedNode() throws Exception {
    UserNode node = Util.getUIPortal().getSelectedUserNode();
    UserNavigation nav = getUserPortal().getNavigation(node.getNavigation().getKey());
    return nav;
  }

  private static UserPortal getUserPortal() {
    UserPortalConfig portalConfig = Util.getPortalRequestContext().getUserPortalConfig();
    return portalConfig.getUserPortal();
  }

  protected boolean isEditProfilePage() throws Exception {
    String uri = Util.getUIPortal().getSelectedUserNode().getURI();
    if (uri.endsWith(EDIT_PROFILE_NODE)) {
      return true;
    }

    return false;
  }

  public Map<String, String> getUserNodes() throws Exception {
    if (userNodes == null) {
      userNodes = new LinkedHashMap<>();
      userNodes.put(PROFILE_URI, getProfileLink());
      userNodes.put(ACTIVITIES_URI, getactivitesURL());
      userNodes.put(CONNEXIONS_URI, getrelationURL());
      if (ExoContainer.hasProfile("wiki")) {
        userNodes.put(WIKI_URI, getWikiURL());
      }
      if (CommonsUtils.isFeatureActive(WALLET_FEATURE_NAME, Utils.getViewerRemoteId())) {
        userNodes.put(WALLET_URI, getWalletURL());
      }
      if (CommonsUtils.isFeatureActive(GAMIFICATION_FEATURE_NAME, Utils.getViewerRemoteId())) {
        userNodes.put(GAMIFICATION_URI, getGamificationURL());
      }
      if (CommonsUtils.isFeatureActive(NotificationUtils.FEATURE_NAME)) {
        userNodes.put(NOTIFICATION_SETTINGS, getNotificationsURL());
      }
    }
    return userNodes;
  }

  //////////////////////////////////////////////////////////
  /**/ /**/
  /**/ // GET URL METHOD// /**/
  /**/ /**/
  //////////////////////////////////////////////////////////

  public String getNotificationsURL() {
    return LinkProvider.getUserNotificationSettingUri(Utils.getOwnerRemoteId());
  }

  public String getactivitesURL() {
    return LinkProvider.getUserActivityUri(Utils.getOwnerRemoteId());
  }

  public String getrelationURL() {
    return LinkProvider.getUserConnectionsYoursUri(Utils.getOwnerRemoteId());
  }

  public String getWalletURL() {
    return NavigationURLUtils.getURLInCurrentPortal(WALLET_URI);
  }

  public String getGamificationURL() {
    return NavigationURLUtils.getURLInCurrentPortal(GAMIFICATION_URI);
  }

  public String getWikiURL() {
    return NavigationURLUtils.getURLInCurrentPortal(WIKI_REF) + USER + Utils.getOwnerRemoteId() + WIKI_HOME;
  }

  protected StatusInfo getStatusInfo() {
    Profile currentProfile = getOwnerProfile();
    StatusInfo si = new StatusInfo();
    ResourceBundle rb = PortalRequestContext.getCurrentInstance().getApplicationResourceBundle();
    UserStateService stateService = getApplicationComponent(UserStateService.class);
    boolean isOnline = stateService.isOnline(currentProfile.getIdentity().getRemoteId());
    if (isOnline) {
      String status = stateService.getUserState(currentProfile.getIdentity().getRemoteId()).getStatus();
      if (!INVISIBLE.equals(status)) {
        si.setCssName(StatusIconCss.getIconCss(status));
        si.setTitle(rb.getString(USER_STATUS_TITLE + status));
        return si;
      }
    }
    // user status is offline or invisible: label is offline
    si.setCssName(StatusIconCss.getIconCss(OFFLINE_STATUS));
    si.setTitle(rb.getString(OFFLINE_TITLE));
    return si;
  }

  enum StatusIconCss {
    DEFAULT("", ""),
    ONLINE("online", "uiIconUserOnline"),
    OFFLINE("offline", "uiIconUserOffline"),
    AVAILABLE("available", "uiIconUserAvailable"),
    INVISIBLE("invisible", "uiIconUserInvisible"),
    AWAY("away", "uiIconUserAway"),
    DONOTDISTURB("donotdisturb", "uiIconUserDonotdisturb");

    private final String key;

    private final String iconCss;

    StatusIconCss(String key, String iconCss) {
      this.key = key;
      this.iconCss = iconCss;
    }

    String getKey() {
      return this.key;
    }

    public String getIconCss() {
      return iconCss;
    }

    public static String getIconCss(String key) {
      for (StatusIconCss iconClass : StatusIconCss.values()) {
        if (iconClass.getKey().equals(key)) {
          return iconClass.getIconCss();
        }
      }
      return DEFAULT.getIconCss();
    }
  }

  class StatusInfo {
    private String title;

    private String cssName;

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getCssName() {
      return cssName;
    }

    public void setCssName(String cssName) {
      this.cssName = cssName;
    }
  }

  public String getProfileLink() {
    return LinkProvider.getUserProfileUri(Utils.getOwnerRemoteId());
  }

  public static class RemoveBannerActionListener extends EventListener<UIUserNavigationPortlet> {

    @Override
    public void execute(Event<UIUserNavigationPortlet> event) throws Exception {
      UIUserNavigationPortlet portlet = event.getSource();
      portlet.removeProfileBanner();

      event.getRequestContext().addUIComponentToUpdateByAjax(portlet);
    }
  }

  private void removeProfileBanner() throws MessageException {
    Profile p = Utils.getOwnerIdentity().getProfile();
    p.removeProperty(Profile.BANNER);
    p.setBannerUrl(null);
    p.setListUpdateTypes(Arrays.asList(Profile.UpdateType.BANNER));

    Utils.getIdentityManager().updateProfile(p);
  }

}
