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
  String imageClass = "";

  Space space = SpaceUtils.getSpaceByContext();

  if (space == null) {
    BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
    Branding branding = brandingService.getBrandingInformation();
    Logo logo = branding.getLogo();
    logoPath = "/portal/rest/v1/platform/branding/logo?lastModified=" + logo.getUpdatedDate();
    logoTitle = branding.getCompanyName();

    UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
    portalPath = portalConfigService.getUserHomePage(request.getRemoteUser());
    if (portalPath == null) {
      portalPath = "/portal/" + ((PortalRequestContext) RequestContext.getCurrentInstance()).getPortalOwner();
    }
  } else {
    logoPath = space.getAvatarUrl();
    logoTitle = space.getDisplayName();
    portalPath = space.getUrl();
    imageClass="spaceAvatar";
  }

%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application border-box-sizing v-application--is-ltr theme--light"
    id="brandingTopBar" flat="">
    <div class="v-application--wrap">
      <div class="container pa-0 pl-3">
        <div class="d-flex mx-0 pa-0">
          <a id="UserHomePortalLink" href="<%=portalPath%>" class="pr-3 logoContainer">
            <img src="<%=logoPath%>" class="<%=imageClass%>" alt="Logo">
          </a>
          <a href="<%=portalPath%>" class="pl-2 align-self-center brandingContainer">
            <div title="<%=logoTitle%>" class="logoTitle subtitle-2 font-weight-bold text-truncate">
              <%= logoTitle%>
            </div>
          </a>
        </div>
      </div>
    </div>
  </div>
</div>