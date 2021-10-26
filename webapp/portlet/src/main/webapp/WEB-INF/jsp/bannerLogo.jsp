<%@ page import="org.exoplatform.social.core.space.model.Space"%>
<%@ page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.web.application.RequestContext"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.portal.branding.Branding"%>
<%@ page import="org.exoplatform.portal.branding.Logo"%>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.exoplatform.social.core.identity.model.Profile" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.exoplatform.social.core.manager.IdentityManager" %>
<%@ page import="org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider" %>
<%@ page import="java.util.Optional" %>
<%
  String logoPath = null;
  String logoTitle = null;
  String portalPath = null;
  String defaultHomePath = "";
  String titleClass = "";
  String imageClass = "";
  String homePath= "";
  int membersNumber= 0;
  List<Profile> managers = new ArrayList<>();
  String spaceDescription= "";
  Space space = SpaceUtils.getSpaceByContext();
  PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);

  defaultHomePath = "/portal/" + requestContext.getPortalOwner();
  if (space == null) {
    BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
    Branding branding = brandingService.getBrandingInformation();
    Logo logo = branding.getLogo();
    logoPath = logo == null ? null : "/portal/rest/v1/platform/branding/logo?lastModified=" + logo.getUpdatedDate();
    logoTitle = branding.getCompanyName();

    portalPath = portalConfigService.getUserHomePage(request.getRemoteUser());
    if (portalPath == null) {
      portalPath = defaultHomePath;
    }
    titleClass = "company";
  } else {
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
  let managers = new Array();
  <%
   for (int i =0 ; i < managers.size(); i++) { %>
  managers.push({
    id: `<%=managers.get(i).getId()%>`,
    userName: `<%=managers.get(i).getIdentity().getRemoteId()%>`,
    fullName: `<%=managers.get(i).getFullName()%>`,
    avatar: `<%=managers.get(i).getAvatarUrl()%>`,
  });
  <%} %>
  let params = {
    logoPath: `<%=logoPath%>`,
    portalPath: `<%=portalPath%>`,
    logoTitle: `<%=logoTitle%>`,
    membersNumber: `<%=membersNumber%>`,
    spaceDescription: `<%=spaceDescription%>`,
    managers: managers,
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
            <v-cacheable-dom-app cache-id="SpaceTopBannerLogo"></v-cacheable-dom-app>
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