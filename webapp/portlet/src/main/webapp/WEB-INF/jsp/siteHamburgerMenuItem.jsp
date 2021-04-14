<%@page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%
  UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
  String homeLink = portalConfigService.getUserHomePage(request.getRemoteUser());
  String jsModule = ((String[])request.getAttribute("jsModule"))[0];
%>
<script type="text/javascript">
eXo.env.portal.homeLink = '<%=homeLink%>';
require(['<%=jsModule%>']);
</script>
