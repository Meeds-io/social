<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext" %>
<%@ page import="org.exoplatform.portal.mop.service.LayoutService" %>
<%@ page import="org.exoplatform.portal.config.model.PortalConfig" %>
<%@ page import="org.exoplatform.web.application.RequestContext" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%
  PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());
  PortalConfig administrationSite = CommonsUtils.getService(LayoutService.class).getPortalConfig("PORTAL", "administration");
  String path = CommonsUtils.getService(UserPortalConfigService.class).computePortalSitePath("administration", requestContext.getRequest());
%>
<% if (administrationSite != null && !administrationSite.isDisplayed() && path != null) { %>
  <div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="platformSettings">
      <div class="v-application--wrap">
        <a href="/portal/administration/home/general/mainsettings" target="_blank" class="mx-2 d-none d-sm-block v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default">
          <span class="v-btn__content">
            <i aria-hidden="true" role="button" aria-haspopup="true" aria-expanded="false" class="v-icon notranslate icon-default-color fas fa-cog theme--light" style="font-size: 22px;"></i>
          </span>
        </a>
      </div>
      <script type="text/javascript">
        require(['PORTLET/social-portlet/PlatformSettings'], app => app.init(`<%=path%>`));
      </script>
    </div>
  </div>
<% } %>
