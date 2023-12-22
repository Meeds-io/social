<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.exoplatform.commons.api.notification.model.UserSetting"%>
<%@page import="org.exoplatform.commons.api.notification.service.setting.UserSettingService"%>
<%@ page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.spi.SpaceService"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.services.security.ConversationState"%>
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
  String logoPath = null;
  String logoTitle = null;
  String portalPath = null;
  String defaultHomePath = "";
  String titleClass = "";
  String imageClass = "";
  String homePath= "";
  int membersNumber= 0;
  boolean isFavorite= false;
  boolean muted= false;
  boolean isMember =false;
  String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
  List<Profile> managers = new ArrayList<>();
  String spaceDescription= "";
  Space space = SpaceUtils.getSpaceByContext();
  PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
  UserSettingService userSettingService = CommonsUtils.getService(UserSettingService.class);
  UserSetting userSetting = userSettingService.get(authenticatedUser);

  defaultHomePath = "/portal/" + requestContext.getPortalOwner();
  if (space == null) {
    BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
    logoPath = brandingService.getLogoPath();
    logoTitle = brandingService.getCompanyName();

    if (StringUtils.equals(requestContext.getPortalOwner(), "public")) {
      portalPath = "/portal/public";
    } else {
      portalPath = portalConfigService.getUserHomePage(request.getRemoteUser());
      if (portalPath == null) {
        portalPath = portalConfigService.computePortalPath(requestContext.getRequest());
        if (portalPath == null) {
          portalPath = defaultHomePath;
        }
      }
    }
    titleClass = "company";
  } else {
    FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
    Identity userIdentity = identityManager.getOrCreateUserIdentity(authenticatedUser);
    spaceId = space.getId();
    SpaceService spaceService = ExoContainerContext.getService(SpaceService.class);
    isMember = spaceService.isMember(space, authenticatedUser);
    isFavorite = favoriteService.isFavorite(new Favorite(space.DEFAULT_SPACE_METADATA_OBJECT_TYPE, space.getId(), null, Long.parseLong(userIdentity.getId())));
    muted = userSetting.isSpaceMuted(Long.parseLong(spaceId));
    logoPath = space.getAvatarUrl();
    logoTitle = space.getDisplayName();
    String permanentSpaceName = space.getGroupId().split("/")[2];
    portalPath = "/portal/g/:spaces:" + permanentSpaceName + "/" + space.getPrettyName();
    membersNumber = space.getMembers().length;
    spaceDescription = Optional.ofNullable(space.getDescription()).orElse("");
    for(String username : space.getManagers()) {
      Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username).getProfile();
      managers.add(profile);
    }
    homePath = Optional.ofNullable(portalConfigService.getUserHomePage(request.getRemoteUser())).orElse(defaultHomePath);
  }

  String directionVuetifyClass = requestContext.getOrientation().isRT() ? "v-application--is-rtl" : "v-application--is-ltr";
%>
<script type="text/javascript" defer="defer">
  let topbarLogoManagers = new Array();
  <%
   for (int i =0 ; i < managers.size(); i++) { %>
   topbarLogoManagers.push({
    id: `<%=managers.get(i).getId()%>`,
    userName: `<%=managers.get(i).getIdentity().getRemoteId()%>`,
    fullName: `<%=managers.get(i).getFullName()%>`,
    avatar: `<%=managers.get(i).getAvatarUrl()%>`,
  });
  <%} %>
  let params = {
    id: `<%=spaceId%>`,
    isFavorite: `<%=isFavorite%>`,
    muted: `<%=muted%>`,
    isMember: `<%=isMember%>`,
    logoPath: `<%=logoPath%>`,
    portalPath: `<%=portalPath%>`,
    logoTitle: `<%=logoTitle%>`,
    membersNumber: `<%=membersNumber%>`,
    spaceDescription: `<%=spaceDescription%>`,
    managers: topbarLogoManagers,
    homePath: `<%=homePath%>`
  };
  document.addEventListener('spaceDetailUpdated', event => {
    const space = event && event.detail;
    if (space && space.displayName) {
      document.querySelector('.logoTitle').innerText = space.displayName;
      document.querySelector('.logoContainer .spaceAvatar').src = space.avatarUrl;
    }
  });
</script>
<div class="VuetifyApp">
  <div data-app="true"
       class="v-application border-box-sizing <%= directionVuetifyClass %> theme--light"
       id="brandingTopBar" flat="">
    <div class="v-application--wrap">
      <div class="container pa-0 ps-3">
        <div class="d-flex mx-0 pa-0">
          <% if (space == null) { %>
          <% if (logoPath != null) { %>
          <a id="UserHomePortalLink" href="<%=portalPath%>" class="pe-3 logoContainer">
            <img src="<%=logoPath%>" class="<%=imageClass%>" alt="<%= logoTitle%>">
          </a>
          <% } %>
          <a href="<%=portalPath%>" title="<%=logoTitle%>"
             class="ps-2 align-self-center brandingContainer <%=titleClass%>">
            <div class="logoTitle subtitle-2 font-weight-bold text-truncate">
              <%= logoTitle%>
            </div>
          </a>
          <% } else { %>
          <div app-data="true" id="SpaceTopBannerLogo">
            <script type="text/javascript">
              require(["SHARED/spaceBannerLogoPopover"], app => app.init(params));
            </script>
          </div>
          <% } %>
        </div>
      </div>
    </div>
  </div>
</div>