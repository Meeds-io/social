<%@ page import="org.exoplatform.portal.mop.service.LayoutService" %>
<%@ page import="org.exoplatform.portal.config.model.PortalConfig" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext" %>
<%@ page import="org.exoplatform.web.application.RequestContext" %>

<%
  PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());
  LayoutService layoutService = CommonsUtils.getService(LayoutService.class);
  PortalConfig sitePortalConfig = layoutService.getPortalConfig(requestContext.getSiteType().getName(), requestContext.getSiteName());
  String siteId = sitePortalConfig.getStorageId().split("_")[1];
%>
<div class="VuetifyApp">
  <div id="verticalMenu">
    <script type="text/javascript">
      eXo.env.portal.siteId = '<%=siteId%>';
      require(['PORTLET/social-portlet/VerticalMenu'], app => app.init());
    </script>
  </div>
</div>