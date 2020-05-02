<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.web.application.RequestContext"%>
<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.portal.branding.Branding"%>
<%@ page import="org.exoplatform.portal.branding.Logo"%>
<%
  BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
  Branding branding = brandingService.getBrandingInformation();
  Logo logo = branding.getLogo();
  String logoPath = "/portal/rest/v1/platform/branding/logo?lastModified=" + logo.getUpdatedDate();

  String portalPath = "/portal/" + ((PortalRequestContext) RequestContext.getCurrentInstance()).getPortalOwner();

  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  Identity identity = Utils.getViewerIdentity();
  if (identity != null && identity.getProfile().getHomePage() != null) {
    portalPath += "/" + identity.getProfile().getHomePage();
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