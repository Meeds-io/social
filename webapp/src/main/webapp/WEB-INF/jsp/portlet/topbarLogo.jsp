<%@page import="org.apache.commons.collections4.CollectionUtils"%>
<%@page import="io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin"%>
<%@page import="io.meeds.social.navigation.constant.SidebarItemType"%>
<%@page import="io.meeds.social.navigation.model.SidebarItem"%>
<%@page import="io.meeds.social.navigation.constant.SidebarMode"%>
<%@page import="io.meeds.social.navigation.model.SidebarConfiguration"%>
<%@page import="io.meeds.social.navigation.model.NavigationConfiguration"%>
<%@page import="io.meeds.social.navigation.model.TopbarConfiguration"%>
<%@page import="io.meeds.social.navigation.service.NavigationConfigurationService"%>
<%@ page import="org.exoplatform.services.security.IdentityConstants"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="org.apache.commons.lang3.StringUtils"%>
<%@ page import="org.exoplatform.commons.api.notification.model.UserSetting"%>
<%@ page import="org.exoplatform.commons.api.notification.service.setting.UserSettingService"%>
<%@ page import="org.exoplatform.social.core.space.model.Space"%>
<%@ page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.services.security.ConversationState"%>
<%@ page import="org.exoplatform.social.metadata.favorite.FavoriteService"%>
<%@ page import="org.exoplatform.social.metadata.favorite.model.Favorite"%>
<%@ page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@ page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.web.application.RequestContext"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.social.core.identity.model.Profile" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.exoplatform.social.core.manager.IdentityManager" %>
<%@ page import="org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider" %>
<%@ page import="java.util.Optional" %>
<%
  String spaceId = null;
  String portalPath = null;
  String titleClass = "";
  String imageClass = "";

  int membersNumber= 0;
  boolean isFavorite= false;
  boolean muted= false;
  boolean isMember =false;
  boolean canRedactOnSpace =false;
  String authenticatedUser = request.getRemoteUser();
  List<Profile> managers = new ArrayList<>();
  String spaceDescription= "";
  Space space = SpaceUtils.getSpaceByContext();
  PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
  UserSettingService userSettingService = CommonsUtils.getService(UserSettingService.class);
  UserSetting userSetting = authenticatedUser == null ? null : userSettingService.get(authenticatedUser);
  NavigationConfigurationService navigationConfigurationService = ExoContainerContext.getService(NavigationConfigurationService.class);
  TopbarConfiguration topbarConfiguration = navigationConfigurationService.getTopbarConfiguration(request.getRemoteUser(), request.getLocale());
  SidebarConfiguration sidebarConfiguration = navigationConfigurationService.getSidebarConfiguration(request.getRemoteUser(), request.getLocale());

  String defaultHomePath = "/portal/" + requestContext.getPortalOwner();

  BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
  String logoPath = brandingService.getLogoPath();
  String logoTitle = brandingService.getCompanyName();
  if (StringUtils.equals(requestContext.getPortalOwner(), "public")) {
    portalPath = "/portal/public";
  } else {
    if (sidebarConfiguration.isAllowUserCustomHome()) {
      portalPath = portalConfigService.getUserHomePage(request.getRemoteUser());
    }
    if (portalPath == null) {
      portalPath = portalConfigService.computePortalPath(requestContext.getRequest());
      if (portalPath == null) {
        portalPath = defaultHomePath;
      }
    }
  }
  titleClass = "company";

  String spaceLogoPath = null;
  String spaceLogoTitle = null;
  String spacePortalPath = null;
  if (space != null) {
    spaceLogoPath = space.getAvatarUrl();
    spaceLogoTitle = space.getDisplayName();
    spacePortalPath = "/portal/s/" + space.getId();

    FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
    Identity userIdentity = identityManager.getOrCreateUserIdentity(authenticatedUser);
    spaceId = space.getId();
    SpaceService spaceService = ExoContainerContext.getService(SpaceService.class);
    isMember = authenticatedUser == null ? false : spaceService.isMember(space, authenticatedUser);
    canRedactOnSpace = authenticatedUser == null ? false : spaceService.canRedactOnSpace(space, authenticatedUser);
    isFavorite = authenticatedUser == null ? false : favoriteService.isFavorite(new Favorite(space.DEFAULT_SPACE_METADATA_OBJECT_TYPE, space.getId(), null, Long.parseLong(userIdentity.getId())));
    muted = authenticatedUser == null ? false : userSetting.isSpaceMuted(Long.parseLong(spaceId));
    String permanentSpaceName = space.getGroupId().split("/")[2];
    membersNumber = space.getMembers().length;
    spaceDescription = Optional.ofNullable(space.getDescription()).orElse("");
    if (authenticatedUser != null) {
      for(String username : space.getManagers()) {
        Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username).getProfile();
        managers.add(profile);
      }
    }
  }

  String directionVuetifyClass = requestContext.getOrientation().isRT() ? "v-application--is-rtl" : "v-application--is-ltr";
  boolean displayCompanyName = topbarConfiguration.isDisplayCompanyName();
  boolean displayMobileCompanyLogo = topbarConfiguration.isDisplayMobileCompanyLogo();
  boolean displaySiteName = topbarConfiguration.isDisplaySiteName();
  SidebarMode sidebarMode = sidebarConfiguration.getUserMode();
  SidebarItem sidebarItem = space == null ? sidebarConfiguration.getItems().stream().filter(item -> item.getUrl() != null
      && item.getType() == SidebarItemType.SITE
      && requestContext.getRequest().getRequestURI().toString().startsWith(item.getUrl()))
    .findFirst()
    .orElse(null)
    : null;
  if (sidebarItem != null
    && StringUtils.equals(sidebarItem.getProperties().get(AbstractLayoutSidebarPlugin.SITE_EXPAND_PAGES_PROP_NAME), "true")
    && CollectionUtils.isNotEmpty(sidebarItem.getItems())) {
    sidebarItem = sidebarItem.getItems().stream().filter(item -> item.getUrl() != null
        && item.getType() == SidebarItemType.PAGE
        && requestContext.getRequest().getRequestURI().toString().startsWith(item.getUrl()))
      .findFirst()
      .orElse(null);
  }
%>
<div class="VuetifyApp full-height">
  <div
    data-app="true"
    class="v-application border-box-sizing full-height <%= directionVuetifyClass %> theme--light"
    id="brandingTopBar"
    flat="">
    <div class="v-application--wrap full-height">
      <script type="text/javascript">
        window.topbarLogoManagers = new Array();
        <% for (int i =0 ; i < managers.size(); i++) { %>
        window.topbarLogoManagers.push({
          id: `<%=managers.get(i).getId()%>`,
          userName: `<%=managers.get(i).getIdentity().getRemoteId()%>`,
          fullName: `<%=managers.get(i).getFullName()%>`,
          avatar: `<%=managers.get(i).getAvatarUrl()%>`,
        });
        <% } %>
        require(["PORTLET/social/TopBarLogo"], app => app.init({
          id: `<%=spaceId == null ? "" : spaceId%>`,
          isFavorite: `<%=isFavorite%>`,
          muted: `<%=muted%>`,
          isMember: `<%=isMember%>`,
          portalPath: `<%=portalPath%>`,
          logoPath: `<%=logoPath%>`,
          logoTitle: `<%=URLEncoder.encode(logoTitle.replace(" ", "._.")).replace("._.", " ")%>`,
          spacePortalPath: `<%=spacePortalPath == null ? "": spacePortalPath%>`,
          spaceLogoPath: `<%=spaceLogoPath == null ? "": spaceLogoPath%>`,
          spaceLogoTitle: `<%=spaceLogoTitle == null ? "" : URLEncoder.encode(spaceLogoTitle.replace(" ", "._.")).replace("._.", " ")%>`,
          membersNumber: `<%=membersNumber%>`,
          spaceDescription: `<%=URLEncoder.encode(spaceDescription.replace(" ", "._.")).replace("._.", " ")%>`,
          managers: window.topbarLogoManagers,
          canRedactOnSpace: <%=canRedactOnSpace%>,
          displayMobileCompanyLogo: <%=displayMobileCompanyLogo%>,
          displayCompanyName: <%=displayCompanyName%>,
          displaySiteName: <%=displaySiteName%>,
          sidebarMode: '<%=sidebarMode%>',
          siteTitle: '<%=sidebarItem == null ? "" : sidebarItem.getName()%>',
          siteHomePath: '<%=sidebarItem == null ? "" : sidebarItem.getUrl()%>',
          siteIcon: '<%=sidebarItem == null || sidebarItem.getIcon() == null ? "" : sidebarItem.getIcon()%>',
        }));
      </script>
    </div>
  </div>
</div>