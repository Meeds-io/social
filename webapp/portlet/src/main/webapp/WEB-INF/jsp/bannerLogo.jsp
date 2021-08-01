<%@ page import="org.exoplatform.social.core.space.model.Space"%>
<%@ page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.web.application.RequestContext"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.portal.branding.Branding"%>
<%@ page import="org.exoplatform.portal.branding.Logo"%>
<%
  String logoPath = null;
  String logoTitle = null;
  String portalPath = null;
  String titleClass = "";
  String imageClass = "";

  Space space = SpaceUtils.getSpaceByContext();
  PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());

  if (space == null) {
    BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
    Branding branding = brandingService.getBrandingInformation();
    Logo logo = branding.getLogo();
    logoPath = logo == null ? null : "/portal/rest/v1/platform/branding/logo?lastModified=" + logo.getUpdatedDate();
    logoTitle = branding.getCompanyName();

    UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
    portalPath = portalConfigService.getUserHomePage(request.getRemoteUser());
    if (portalPath == null) {
      portalPath = "/portal/" + requestContext.getPortalOwner();
    }
    titleClass = "company";
  } else {
    logoPath = space.getAvatarUrl();
    logoTitle = space.getDisplayName();
    String permanentSpaceName = space.getGroupId().split("/")[2];
    portalPath = "/portal/g/:spaces:" + permanentSpaceName + "/" + space.getPrettyName();
    imageClass="spaceAvatar";
    titleClass = "space";
  }

  String directionVuetifyClass = requestContext.getOrientation().isRT() ? "v-application--is-rtl" : "v-application--is-ltr";
%>
<script type="text/javascript" defer="defer">
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
          <% if (logoPath != null) { %>
          <a id="UserHomePortalLink" href="<%=portalPath%>" class="pe-3 logoContainer">
            <img src="<%=logoPath%>" class="<%=imageClass%>" alt="<%= logoTitle%>">
          </a>
          <% } %>
          <a href="<%=portalPath%>" title="<%=logoTitle%>" class="ps-2 align-self-center brandingContainer <%=titleClass%>">
            <div class="logoTitle subtitle-2 font-weight-bold text-truncate">
              <%= logoTitle%>
            </div>
          </a>
          <a id="SpaceTitleActionComponnetsContainer"></a>
        </div>
      </div>
    </div>
  </div>
</div>
