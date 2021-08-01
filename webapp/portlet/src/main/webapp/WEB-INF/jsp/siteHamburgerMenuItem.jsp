<%@page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%
  UserPortalConfigService portalConfigService = CommonsUtils.getService(UserPortalConfigService.class);
  String homeLink = portalConfigService.getUserHomePage(request.getRemoteUser());
  String jsModule = ((String[])request.getAttribute("jsModule"))[0];
%>
<script type="text/javascript" defer="defer">
eXo.env.portal.homeLink = '<%=homeLink%>';
eXo.env.portal.onLoadCallbacks.push(() => require(['<%=jsModule%>']));
</script>
