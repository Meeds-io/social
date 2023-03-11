<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
  String activityId = rcontext.getRequest().getParameter("id");
  Space space = SpaceUtils.getSpaceByContext();
  responseWrapper.addHeader("Link", "</portal/rest/v1/social/spaces/" + space.getId() + "?expand=managers,redactors>; rel=preload; as=fetch; crossorigin=use-credentials", false);
%>
<div class="VuetifyApp">
  <div id="spaceInfosApp" class="uiBox">
    <v-cacheable-dom-app cache-id="spaceInfosApp_<%=space.getId()%>"></v-cacheable-dom-app>
  </div>
</div>
