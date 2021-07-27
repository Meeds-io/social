<%@page import="org.gatein.portal.controller.resource.ResourceRequestHandler"%>
<%@page import="org.exoplatform.portal.resource.SkinService"%>
<%@page import="org.exoplatform.services.resources.Orientation"%>
<%@page import="org.exoplatform.portal.resource.SkinURL"%>
<%@page import="org.exoplatform.portal.resource.SkinConfig"%>
<%@page import="org.exoplatform.web.ControllerContext"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="org.exoplatform.social.core.search.SearchConnector"%>
<%@page import="java.util.Set"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.social.core.search.SearchService"%>
<%
  SearchService searchService = ExoContainerContext.getService(SearchService.class);
  SkinService skinService = ExoContainerContext.getService(SkinService.class);
  Set<SearchConnector> connectors = searchService.getEnabledConnectors();
  String jsonSearchConnectors = EntityBuilder.toJsonString(connectors);
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  ControllerContext controllerContext = rcontext.getControllerContext();
  Orientation orientation = rcontext.getOrientation();
%>
<div class="VuetifyApp">
  <% for (SearchConnector connector : connectors) {
    SkinConfig skinConfig = null;
    if (connector.getCssModule() != null) {
      skinConfig = skinService.getSkin(connector.getCssModule(), null);
    }
    if (skinConfig != null) {
      SkinURL url = skinConfig.createURL(controllerContext);
      url.setOrientation(orientation); %>
  <link id="<%=skinConfig.getId()%>" rel="stylesheet" type="text/css" href="<%=url%>" />
  <%  } %>
  <% } %>
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SearchApplication">
    <v-cacheable-dom-app cache-id="SearchApplication"></v-cacheable-dom-app>
    <textarea id="searchConnectorsDefaultValue" class="hidden"><%= jsonSearchConnectors%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/Search'], app => app.init(JSON.parse(document.getElementById('searchConnectorsDefaultValue').value)));
    </script>
  </div>
</div>
