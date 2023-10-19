<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext" %>
<%@ page import="org.exoplatform.web.application.RequestContext" %>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%
    PortalRequestContext requestContext = ((PortalRequestContext) RequestContext.getCurrentInstance());
    UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
    String path = portalConfigService.computePortalSitePath("administration", requestContext.getRequest());
%>
<div class="VuetifyApp">
    <div id="platformSettings">
        <script type="text/javascript">
            let url = `<%=StringUtils.isBlank(path) ? "" : path%>`;
            require(['PORTLET/social-portlet/PlatformSettings'], app => app.init(url));
        </script>
    </div>
</div>
