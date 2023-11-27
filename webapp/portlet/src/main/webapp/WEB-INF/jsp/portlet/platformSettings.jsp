<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext" %>
<%@ page import="org.exoplatform.portal.mop.service.LayoutService" %>
<%@ page import="org.exoplatform.portal.config.model.PortalConfig" %>
<%@ page import="org.exoplatform.web.application.RequestContext" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%
  PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());
  UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
  String path = portalConfigService.computePortalSitePath("administration", requestContext.getRequest());
  LayoutService layoutService = CommonsUtils.getService(LayoutService.class);
  PortalConfig administrationSite = layoutService.getPortalConfig("PORTAL", "administration");
  
%>
<% if (!administrationSite.isDisplayed() && path != null) { %>
  <div class="VuetifyApp">
    <div id="platformSettings">
      <script type="text/javascript">
        let url = `<%=path%>`;
        require(['PORTLET/social-portlet/PlatformSettings'], app => app.init(url));
      </script>
    </div>
  </div>
<% } %>
