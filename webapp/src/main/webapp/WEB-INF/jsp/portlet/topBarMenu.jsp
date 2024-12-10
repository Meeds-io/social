<%@page import="java.util.Objects"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.exoplatform.portal.mop.user.UserNode"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.portal.config.UserPortalConfigService"%>
<%@page import="org.exoplatform.portal.mop.SiteKey"%>
<%
  PortalRequestContext rcontext = PortalRequestContext.getCurrentInstance();
  UserPortalConfigService portalConfigService = ExoContainerContext.getService(UserPortalConfigService.class);
  SiteKey siteKey = rcontext.getSiteKey();

  UserNode userNode = portalConfigService.getSiteRootNode(siteKey.getTypeName(), siteKey.getName(), request.getRemoteUser(), true);
  String parentNodeId = userNode == null || userNode.getChildrenCount() == 0 || userNode.getChild(0).getChildrenCount() == 0 ? null : userNode.getChild(0).getId();
  if (parentNodeId != null) {
    PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
    responseWrapper.addHeader("Link", "</portal/rest/v1/navigations/" + siteKey.getType().name() + "?siteName=" + URLEncoder.encode(siteKey.getName()) + "&scope=ALL&visibility=displayed&visibility=temporal&nodeId=" + parentNodeId + "&expand=true>; rel=preload; as=fetch; crossorigin=use-credentials", false);
  }
  String cacheId = "topBarMenu-" + Objects.hash(siteKey, parentNodeId);
%>
<div class="VuetifyApp">
  <div data-app="true" class="v-application v-application--is-ltr theme--light grey" id="topBarMenu">
    <script type="text/javascript">
      if (!window.topBarMenuLoaded) {
        window.topBarMenuLoaded = true;
        window.topBarMenuHtml = sessionStorage.getItem('<%=cacheId%>');
        if (window.topBarMenuHtml) {
          document.querySelector('#topBarMenu').innerHTML = window.topBarMenuHtml;
        }
        require(['PORTLET/social/TopBarMenu'], app => app.init('<%=cacheId%>', <%=parentNodeId%>));
      }
    </script>
  </div>
</div>