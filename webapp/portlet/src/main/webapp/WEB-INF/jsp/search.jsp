<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.HashSet"%>
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
  Set<String> skinURLs = new HashSet<>();
  for (SearchConnector connector : connectors) {
    SkinConfig skinConfig = null;
    if (connector.getCssModule() != null) {
      skinConfig = skinService.getSkin(connector.getCssModule(), null);
    }
    if (skinConfig != null) {
      SkinURL url = skinConfig.createURL(controllerContext);
      url.setOrientation(orientation);
      skinURLs.add(url.toString());
    } 
  }
  String skinUrlsString = "[\"" + StringUtils.join(skinURLs, "\",\"") + "\"]";
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SearchApplication">
    <div class="v-application--wrap">
      <button
        type="button"
        class="transparent v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default"
        onclick="Vue.startApp('PORTLET/social-portlet/Search', 'init')">
        <span class="v-btn__content">
          <i class="uiIconPLF24x24Search position-static d-flex"></i>
        </span>
      </button>
    </div>
    <textarea id="searchConnectorsDefaultValue" class="hidden"><%= jsonSearchConnectors%></textarea>
    <textarea id="searchSkinUrlsDefaultValue" class="hidden"><%= skinUrlsString%></textarea>
    <% if (rcontext.getRequestURI().endsWith("/search")) { %>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/Search'], app => app.init());
    </script>
    <% } %>
  </div>
</div>
