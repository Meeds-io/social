<%@page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.web.application.RequestContext"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.portal.branding.Branding"%>
<%@ page import="org.exoplatform.portal.branding.Logo"%>
<%
  BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
  Branding branding = brandingService.getBrandingInformation();
  Logo logo = branding.getLogo();
  String logoPath = "/portal/rest/v1/platform/branding/logo?lastModified=" + logo.getUpdatedDate();

  UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
  String portalPath = portalConfigService.getUserHomePage(request.getRemoteUser());
  if (portalPath == null) {
    portalPath = "/portal/" + ((PortalRequestContext) RequestContext.getCurrentInstance()).getPortalOwner();
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
            <img src="<%=logoPath%>" alt="Logo">
          </a>
          <a href="<%=portalPath%>" class="pl-2 align-self-center brandingContainer">
            <span class="subtitle-2 font-weight-bold">
              <%= branding.getCompanyName()%>
            </span>
          </a>
        </div>
      </div>
    </div>
  </div>
</div>